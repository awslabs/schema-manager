package com.amazonaws.schemamanager.repo;

import com.amazonaws.schemamanager.AvroSchemaSM;
import com.amazonaws.schemamanager.properties.IRepoClientConfig;
import com.amazonaws.schemamanager.properties.LocalRepoClientConfig;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchemaMetadata;
import com.amazonaws.schemamanager.utils.ProtobufParser;
import com.amazonaws.schemamanager.repo.datatypes.DefaultSchemaMetadata;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.yaml.snakeyaml.Yaml;
import org.apache.commons.io.FilenameUtils;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchema;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains the logic for how schemas and subjects are built from files. The same logic is used for both repositories and filesystems as both are file based.
 */
public class FileSystemRepoClient implements IRepoClient {

	private LocalRepoClientConfig localConfig;
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileSystemRepoClient.class);
	private Map<String, DefaultSchemaMetadata> defaults;

	/**
	 * Supported Types of Schemas.
	 */
	public enum FILE_EXTENSIONS {
		AVSC, PROTO, JSON, UNKNOWN;
		
		/**
		 * Verifies if the extension passed is a value contained in ENUM. Returns value if is, and UNKOWN if not.
		 * @param extension
		 * @return FILE_EXTENSIONS
		 */
		public static FILE_EXTENSIONS extensionFromString(String extension) {
			for (FILE_EXTENSIONS value : values()) {
				if (value.name().equalsIgnoreCase(extension)) {
					return value;
				}
			}
	
			return UNKNOWN;
		}
	}
	

	@Override
	public void init(IRepoClientConfig config) {
		this.localConfig = (LocalRepoClientConfig) config;
		checkLocalConfig(this.localConfig.getBaseInfo());
	}
	
	@Override
	public List<RepoSchema> getSchemaList(boolean useFilters) throws IOException {
		if (!useFilters) {
			return getSchemaList(localConfig.getBaseInfo());
		}
		
		return null;
	}


	@Override
	public List<RepoSchema> getSchemaList() throws IOException {
		return getSchemaList(false);
	}
	

	/**
	 * Builds a list of Repository Schemas from a given directory. 
	 * @param baseDir
	 * @return List
	 * @throws IOException
	 */
	public List<RepoSchema> getSchemaList(String baseDir) throws IOException {
		
		ArrayList<RepoSchema> repoSchemas = new ArrayList<>();
		
		//populating default metadata for the given appconfiguration
		defaults = RepoUtils.getDefaultMetadata(baseDir);
		
		List<String> filesToProcess = getSchemaFiles(baseDir);
		Map<String, String> failedFiles = null;
		
		Integer successCount = null;
		int totalIterations = 0;
		Map<String, Schema> avroKnownTypes = new HashMap<>();
		ProtobufParser protoParser = new ProtobufParser();
		
		/**
		 * Because references from one schema to another can cause schema parsing failures
		 * we build a map of the failures to process the references in their necessary order.
		 * We do not know the order until we examine each schema and attempt to serialize it. 
		 */
		while (filesToProcess != null && !filesToProcess.isEmpty() && (successCount == null || successCount>0)) {
			failedFiles = new HashMap<>();
			totalIterations++;
			successCount = 0;

			for (String repoSchemaFile : filesToProcess) {
				String fileExtension =  getExtensionByStringHandling(repoSchemaFile);
				
				if(fileExtension != null){
					FILE_EXTENSIONS fileExtensionKnown = FILE_EXTENSIONS.extensionFromString(fileExtension.toString().toUpperCase());
					
					if(fileExtensionKnown != null && fileExtensionKnown != FILE_EXTENSIONS.UNKNOWN){
						//Create the new RepoSchema and build out its members
						RepoSchema repoSchema = new RepoSchema();
						RepoSchemaMetadata repoSchemaMetadata = new RepoSchemaMetadata();
						Path path = Paths.get(repoSchemaFile);
						Path relativePath = Paths.get(baseDir).relativize(path);
						repoSchema.setPath(relativePath.toString());
						
						switch (fileExtensionKnown) {
							case AVSC:
								try {
									InputStream in = Files.newInputStream(path);
									String schemaString = convertStreamToString(in);
									AvroSchemaSM.initParser(avroKnownTypes);
									ParsedSchema parsedSchema = new AvroSchemaSM(schemaString); //Parsed Schema because RepoSchema expects Parsed Schema not Avro/proto etc
//									ParsedSchema parsedSchema = new AvroSchema(schemaString, Collections.emptyList(), avroResolvedRefs, null); //Parsed Schema because RepoSchema expects Parsed Schema not Avro/proto etc
//									ParsedSchema parsedSchema = RepoUtils.parseAvro(schemaString); 
									parsedSchema.validate();
									repoSchema.setSchema(parsedSchema);
									repoSchemaMetadata = getAssociatedFileMetadata(path.toString());

									if (repoSchemaMetadata.getSchemaName() == null) {
										repoSchemaMetadata.setSchemaName(parsedSchema.name());
									}

									repoSchema.setMetadata(repoSchemaMetadata);

									RepoUtils.completeDefaults(repoSchema, defaults);

									avroKnownTypes.put(repoSchemaMetadata.getSchemaName(),
											(Schema) parsedSchema.rawSchema());
								} catch (Exception e) {
									failedFiles.put(repoSchemaFile, e.getMessage());
									continue;
								}
								
							break;
							case PROTO:
							try {
								InputStream in = Files.newInputStream(path);
								String schemaString =  convertStreamToString(in);
//								ParsedSchema parsedSchema = new ProtobufSchema(schemaString);
								repoSchema = protoParser.parseProtoFile(repoSchemaFile, schemaString, repoSchema);
//								ParsedSchema parsedSchema = protoParser.parse(repoSchemaFile, schemaString);
//								repoSchema.setSchema(parsedSchema);
								repoSchemaMetadata = getAssociatedFileMetadata(path.toString());
								repoSchema.getMetadata().merge(repoSchemaMetadata);
								repoSchemaMetadata = repoSchema.getMetadata();
								
								if(repoSchemaMetadata.getSchemaName() == null){
									repoSchemaMetadata.setSchemaName(RepoUtils.getFullName(repoSchema.getSchema()));
								}
								
								RepoUtils.completeDefaults(repoSchema, defaults);
								
							} catch (Exception e) {
								failedFiles.put(repoSchemaFile, e.getMessage());
								continue;
							}
							break;
							case JSON:
							try {
								InputStream in = Files.newInputStream(path);
								String schemaString =  convertStreamToString(in);
								ParsedSchema parsedSchema = new JsonSchema(schemaString);
								repoSchema.setSchema(parsedSchema);
								repoSchemaMetadata = getAssociatedFileMetadata(path.toString());
								
								if(repoSchemaMetadata.getSchemaName() == null){
									repoSchemaMetadata.setSchemaName(parsedSchema.name());
								}
								
								repoSchema.setMetadata(repoSchemaMetadata);
								RepoUtils.completeDefaults(repoSchema, defaults);
								
							} catch (IOException | SchemaParseException e) {
								failedFiles.put(repoSchemaFile, e.getMessage());
								continue;
							}
							break;
							case UNKNOWN:
							
								log.info("File: %s", String.join(" ", repoSchemaFile, "is not a parsable file type."));

								break;
							
						}
						repoSchemas.add(repoSchema);
						successCount++;
					}else log.info(String.format("File: %s", String.join(" ", repoSchemaFile, "is not a Schema File type.")));
				}
			}
			filesToProcess = new LinkedList<>(failedFiles.keySet());
		}
		log.info("Iteractions: " + totalIterations);
		if (failedFiles != null && !failedFiles.isEmpty()) {
			failedFiles.forEach( (ff, err) -> {
				log.warn("Couldn't parse schema file: " + ff + ". Error: " + err);
			});
		}
		
		return repoSchemas;
	}

	@Override
	public RepoSchema getSchema(String schemaName) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public RepoSchema getSchemaUpdates(String schemaName) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public List<String> getSchemaFiles(){
		return getSchemaFiles(localConfig.getBaseInfo());
	}
	/**
	 * Walk the given path's directory building a list of all objects paths'. Returns a List of Strings that corresponds
	 * to each object's location under the parent directory's path
	 * @param path
	 * @return List
	 */
	private List<String> getSchemaFiles(String path){
		if (!Paths.get(path).toFile().exists()) {
			log.info(path + " does not exist.");
			return null;
		}
		List<String> result;
		try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            // We want to find only regular files
            result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

			return result;
        } catch (IOException e) {
            e.printStackTrace();
			return null;
        }
		
		
	}

	/**
	 * Retrieves the extension of the file passed. Returns the string repesentation of the extension. 
	 * Example: .csv would be returned
	 * @param fileName
	 * @return STRING
	 */
	public String getExtensionByStringHandling(String fileName) {
		return FilenameUtils.getExtension(fileName);
	}

	/**
	 * Verifies a given file's extension is an acceptable file extension for a schema. It compares the inputted string against
	 * the ENUM values. If not found, the value UNKNOWN will be returned. 
	 * @param fileExtension
	 * @return ENUM
	 * @throws IllegalArgumentException
	 */
	public FILE_EXTENSIONS checkExtensionsOfFile(String fileExtension) throws IllegalArgumentException {
		String compareString = fileExtension.toUpperCase();
		try {
			FILE_EXTENSIONS fe = FILE_EXTENSIONS.valueOf(compareString);
			return fe;
		}catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
	}
	/**
	 * Converts a stream input into a String and returns. Used for consilidation.
	 * @param in
	 * @return String
	 * @throws IOException
	 */
	public static String convertStreamToString(InputStream in) throws IOException{
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = in.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8").replaceAll("'", "");
	}
	/***
	 * Attempts to get the metadata yaml file assosciated with the same name as the schema file. 
	 * Example : example.avsc and example.avsc.yml
	 * if no file of such name is found or provided, default values will be set. 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private RepoSchemaMetadata getAssociatedFileMetadata(String path) throws IOException{
		
		String yamlPath = path+".yml";
		File metaYamlFile = new File(yamlPath);
		RepoSchemaMetadata repoSchemaMetadata = null;

		if(metaYamlFile.exists()) { 

			try( InputStream in = Files.newInputStream( Paths.get( yamlPath ) ) ) {
				Yaml yaml = new Yaml();
				repoSchemaMetadata = yaml.loadAs(in, RepoSchemaMetadata.class);

			}catch(Exception e) {

				log.error(String.format("Failed to retrieve MetaData for File %s. Defaults will be applied. ", yamlPath), e);
				repoSchemaMetadata = defaults.get(path);

			}
		} else {
			repoSchemaMetadata = new RepoSchemaMetadata();
		}

		return repoSchemaMetadata;
	}
	
	/**
	 * Verifies that the given directory inside of the config file is an actual traversable directory.
	 * If the directory provided is not a walkable path, the error will be logged and the application will continue. 
	 * @param configPath
	 */
	private void checkLocalConfig(String configPath){
		File file  = new File(configPath);
		if(file.isDirectory() && file.isAbsolute()) {
			return;
		}else{
			log.error(String.format("Provided Path is not a working Directory. Please provide a working directory %s", String.join(" " ,configPath)));
		}
	}
}
