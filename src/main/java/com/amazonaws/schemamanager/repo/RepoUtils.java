package com.amazonaws.schemamanager.repo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.yaml.snakeyaml.Yaml;

import com.amazonaws.schemamanager.AvroSchemaSM;
import com.amazonaws.schemamanager.properties.AppConfigHelper;
import com.amazonaws.schemamanager.properties.RepoClientConfig;
import com.amazonaws.schemamanager.repo.datatypes.DefaultSchemaMetadata;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchemaMetadata;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class RepoUtils {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RepoUtils.class);
	
	public static Map<String, CompatibilityLevel> schemaToSubjectNames (RepoSchema schema){
		Map<String, CompatibilityLevel> result = new LinkedHashMap<>();
		
		CompatibilityLevel sysDefaultCompatibility = AppConfigHelper.getDefaultCompatibility();
		RepoSchemaMetadata meta = schema.getMetadata();
		CompatibilityLevel defaultCompatibility = 
				(meta == null || meta.getDefaultCompatibility() == null)
				? sysDefaultCompatibility 
				: CompatibilityLevel.valueOf(meta.getDefaultCompatibility());
		
		// Proto: add schema name as first subject:
		if (meta != null && schema.getSchema() instanceof ProtobufSchema) {
			result.put(meta.getSchemaName(), defaultCompatibility);
		}
		
		ParsedSchema ps = schema.getSchema();
		if (meta == null) {
			String subject = getFullName(ps);
			result.put(subject, defaultCompatibility);
			return result;
		}
		
		Map<String, Map<String, String>> subjects = meta.getSubjects();
		if (subjects == null) {
			String subject = getFullName(ps);
			result.put(subject, defaultCompatibility);
			return result;
		}
		subjects.forEach((s, m) -> {
			CompatibilityLevel cl = defaultCompatibility;
			if (m != null) {
				String comp = m.get(RepoClientConfig.COMPATIBILITY_FIELD);
				if (comp != null) {
					cl = CompatibilityLevel.valueOf(comp);
				}
			}
			result.put(s, cl);
		});
		
		return result;
	}
	
	public static String getFullName(ParsedSchema ps) {
		if (ps == null) return null;
		
		if (ps instanceof AvroSchema || ps instanceof AvroSchemaSM) {
			return ps.name();
		}
		
		if (ps instanceof ProtobufSchema) {
			return ((ProtobufSchema) ps).toDescriptor().getFullName();
		}
		return ps.name();
	}

	/**
	 * Builds a mapping from subject name to the list of schemas. Since schema can be assigned to the multiple subjects, this method actually reverts the map.
	 * @param schema
	 * @param collection
	 * @return mapping from subject names to schemas
	 */
	public static Map<String, List<RepoSchema>> schemaToSubjectMap (RepoSchema schema, Map<String, List<RepoSchema>> collection){
		if (schema == null) return collection;
		
		final Map<String, List<RepoSchema>> subjectMap = new LinkedHashMap<>();
		Map<String, CompatibilityLevel> subjNames = schemaToSubjectNames(schema);
		subjNames.forEach((n, c) -> {
			subjectMap.computeIfAbsent( n, v -> new LinkedList<RepoSchema>()).add(schema);
		});
		
		subjectMap.forEach((s, l) -> {
			collection.computeIfAbsent( s, v -> new LinkedList<RepoSchema>()).add(schema);
		});
		//collection.putAll(subjectMap);
		return collection;
	}

	private static final String SEPARATOR = FileSystems.getDefault().getSeparator();
	private static final String DEAFULTS_FILENAME = "__defaults.yml";
	public static Map<String, DefaultSchemaMetadata> getDefaultMetadata(String path) {
		Map<String, DefaultSchemaMetadata> defaults = new HashMap<>();
		Path basePath = Paths.get(path);
		
		if (!basePath.toFile().exists()) {
			log.info(path + " does not exist.");
			return null;
		}
		
		try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            // We want to find only regular files
            walk.filter(Files::isDirectory)
                    .forEach(x -> {
                    	Path parent = x.getParent();
                    	DefaultSchemaMetadata parentDefaults = defaults.get(parent.toString());
                    	File __defaults = new File(x.toString() + SEPARATOR + DEAFULTS_FILENAME);
                    	DefaultSchemaMetadata nodeDefaults;
                    	if (!__defaults.exists()) {
                    		nodeDefaults = new DefaultSchemaMetadata(parentDefaults);
                    	}else {
                    		Yaml yaml = new Yaml();
                            try( InputStream in = Files.newInputStream( __defaults.toPath() ) ) {
                            	nodeDefaults = yaml.loadAs(in, DefaultSchemaMetadata.class);
                            } catch (IOException e) {
                            	log.error("Couldn't read file " + __defaults.toPath(), e);
                            	nodeDefaults = new DefaultSchemaMetadata(parentDefaults);
							}
                    	}
                    	
                    	defaults.put(basePath.relativize(x.toAbsolutePath()).toString(), nodeDefaults);
                    	nodeDefaults.setParent(parentDefaults);
                    });
        } catch (IOException e) {
        	log.error("ERROR: ", e);
			return null;
        }
		return defaults;
	}
	
	public static void completeDefaults(RepoSchema repoSchema, Map<String, DefaultSchemaMetadata> defaults) {
		if (repoSchema == null) {
			return;
		}
		RepoSchemaMetadata meta = repoSchema.getMetadata();
		
		Path schemaPath = Paths.get(repoSchema.getPath());
		DefaultSchemaMetadata defaultMeta = defaults.get(schemaPath.getParent().toString());
		if (meta == null) {
			repoSchema.setMetadata(defaultMeta);
			return;
		}
		
		if (meta.getDefaultCompatibility() == null) {
			meta.setDefaultCompatibility(defaultMeta.getDefaultCompatibility());
		}
		
		Map<String, Map<String, String>> subjects = meta.getSubjects();
		if (subjects != null && subjects.isEmpty()) {
			//special case to avoid creating subjects for this particular schema.
			return;
		}
		if (subjects == null) {
			subjects = new HashMap<String, Map<String,String>>();
			subjects.computeIfAbsent(getSubjectName(repoSchema), m-> new HashMap<String, String>()).put("compatibility", meta.getDefaultCompatibility());
			repoSchema.getMetadata().setSubjects(subjects);
		}else {
			subjects.forEach((subj, m) -> {
				if (m.get("compatibility") == null) {
					m.put("compatibility", meta.getDefaultCompatibility());
				}
			});
		}
		
	}
	
	//TODO: Implement with Subject naming strategy from configuration
	public static String getSubjectName(RepoSchema repoSchema) {
//		if (ProtobufSchema.TYPE.equals(repoSchema.getSchema().schemaType())) {
//			return null;
//		}
		return getFullName(repoSchema.getSchema());
	}
	
	public static void main(String[] args) {
		
//		Map<String, DefaultSchemaMetadata> defM = getDefaultMetadata("/Users/eberezit/dev_projects/amak-cluster-configurations/schemas");
		Path basePath = Paths.get("/Users/eberezit/dev_projects/amak-cluster-configurations/schemas");
		Path filePath = Paths.get("/Users/eberezit/dev_projects/amak-cluster-configurations/schemas/amak-fmf/entity/EventFieldDto.avsc");
		System.out.println(basePath.relativize(filePath));
	}
	
	
	public static AvroSchema parseAvro(String schemaStr) {
		AvroSchemaSM result = null;
		Map<String, Schema> knownTypes = AvroSchemaSM.getTypes();
		try {
			result = new AvroSchemaSM(schemaStr);
		}catch (Exception e) {
			AvroSchemaSM.initParser(knownTypes);
			throw e;
		}
		return result;
	}
	
	public static boolean equalsIgnoreRefs(ParsedSchema schema1, ParsedSchema schema2) {
		if (schema1 == null && schema2 == null) return true;
		if (schema1 == schema2) return true;
		
		if (schema1 == null || schema2 == null) return false;
		
		if (!schema1.getClass().isInstance(schema2) && !schema2.getClass().isInstance(schema1))  return false;
		// Can't use schemaObj as it doesn't compare field doc, aliases, etc.
		return Objects.equals(schema1.canonicalString(), schema2.canonicalString());
	}
}
