package com.amazonaws.schemamanager.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.schemamanager.properties.RegistryClientConfig;
import com.amazonaws.schemamanager.repo.RepoUtils;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.SchemaProvider;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;

public class SchemaRegistryClient implements IRegistryClient {

	private CachedSchemaRegistryClient cSchemaRegistryclient;
    private String CLUSTER_COMPATIBILITY_CONFIG;
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaRegistryClient.class);
    private  List<SchemaProvider> providers;
    private List<String> urls;
    private final int identityMapCapacity = 3000;
    
    protected Map<String, Subject> resolvedVersions = new LinkedHashMap<>();
    

    @Override
    public void init(RegistryClientConfig config) {
    	String regEndpoint =  config.getRegistryEndPoint();
    	if (regEndpoint == null) {
    		return;
    	}
        buildUrlsList(regEndpoint);
        buildProvidersList();
        Map<String, ?> originals = null;
        CachedSchemaRegistryClient cSchemaRegistryClient = new CachedSchemaRegistryClient(urls, identityMapCapacity, providers, originals);
        this.cSchemaRegistryclient = cSchemaRegistryClient;
        getRegistryClusterDefaultCompatibility();
    }

    /***
     * Need to build list of URLS so that each Provider has a URL associated
     * @param registryEndpointString
     */
    private void buildUrlsList(String registryEndpointString) {
        urls = new LinkedList<>();
        urls.add(registryEndpointString);
    }
    
    /**
     * Must build list of each expected SchemaProviderType 
     * This is to handle each potential ParsedSchema Object from Schema Registry
     */
    private void buildProvidersList() {
        providers = new LinkedList<>();
        providers.add(new ProtobufSchemaProvider());
        providers.add(new JsonSchemaProvider());
        providers.add(new AvroSchemaProvider());

    }

    @Override
    public ArrayList<Subject> getSubjects() throws IOException, RestClientException  {
       Collection<String> stringSubjects = cSchemaRegistryclient.getAllSubjects();
       ArrayList<Subject> subjectList = new ArrayList<>();
       
        for (String subject : stringSubjects) {
            Subject sub = getSubject(subject);
            subjectList.add(sub);
        }
        return subjectList;
    }
    
    private Subject getSubject(String subject) throws IOException, RestClientException {
        Subject sub = new Subject();
        sub.setName(subject);
        List<Integer> versions = getSubjectVersions(subject);
        String compatibility = getSubjectCompatibilityConfig(subject);
        sub.setCompatibility(CompatibilityLevel.forName(compatibility));
        for(Integer i : versions) {
        	sub.addSchema(getSchema(subject,i), i);
        }
        return sub;
    }

    @Override
    public ParsedSchema getSchema(String schemaName) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public List<Integer> getSubjectVersions(String subjectName) throws IOException, RestClientException{
        List<Integer> schemaVersions;
        schemaVersions = this.cSchemaRegistryclient.getAllVersions(subjectName);
        return schemaVersions;
    }
    /**
     * Gets compatibility configuration for the subject that was passed into the method. 
     * @param subject
     * @return String
     * @throws IOException
     * @throws RestClientException
     */
    private String getSubjectCompatibilityConfig(String subject) throws IOException, RestClientException{
        String compatibilityString;
        try {
            compatibilityString = cSchemaRegistryclient.getCompatibility(subject);

        } catch (IOException | RestClientException e) {
            log.warn(String.format("No CompatibilityLevel has been set for topic %s : Will use Schema Registry Default Compatibility Level", String.join(" ", subject)));
            compatibilityString = cSchemaRegistryclient.updateCompatibility(subject, CLUSTER_COMPATIBILITY_CONFIG);
        } 
        return compatibilityString;
    }

    /**
     * Gets Registry default compatibility. Sets this as the default for the operations to follow. 
     * @param
     * @return
     * 
     */
    public void getRegistryClusterDefaultCompatibility(){
        try {
            CLUSTER_COMPATIBILITY_CONFIG = cSchemaRegistryclient.getCompatibility("");
        } catch (IOException | RestClientException e) {
            
            log.error(String.format("Unable to retrieve registry cluster default compatibility config ", String.join(" ", e.getMessage())));
        }
    }

    /**
     * Get schema by subject and version ID
     * @param subject subject name in schema registry
     * @param schemaVersion version Id in schema registry
     */
    @Override public ParsedSchema getSchema(String subject, Integer schemaVersion) throws IOException, RestClientException {
        SchemaMetadata schemaMetadata;
        ParsedSchema parsedSchema;
        schemaMetadata = this.cSchemaRegistryclient.getSchemaMetadata(subject, schemaVersion);
        parsedSchema = this.cSchemaRegistryclient.getSchemaBySubjectAndId(subject, schemaMetadata.getId());
        
        return parsedSchema; 
    }

    @Override
    public void registerSchema(String subject, ParsedSchema schema) throws Exception {
    	this.cSchemaRegistryclient.register(subject, schema);
    }
    
    public void updateSchema(String subject, ParsedSchema schema) throws Exception {
    	this.cSchemaRegistryclient.register(subject, schema);
    }
    
    @Override
    public CachedSchemaRegistryClient getSchemaRegistryClient() {
    	return cSchemaRegistryclient;
    }

	@Override
	public void deployChanges(List<RepoSchema> repoSchemas) {
		deployChanges(repoSchemas, new LinkedList<Subject>());
		
	}
    
	@Override
	public void deployChanges(List<RepoSchema> repoSchemas, List<Subject> registrySubjects) {
		
		List<String> deployErrors = new LinkedList<>();
		
		final Map<String, List<RepoSchema>> repoSubjects = new LinkedHashMap<>();
		repoSchemas.forEach(s -> {
			RepoUtils.schemaToSubjectMap(s, repoSubjects);
		});

		
		Map<String, Subject> regSubjectsMap = new HashMap<>();
		registrySubjects.forEach(regSubj -> {
			regSubjectsMap.put(regSubj.getName(), regSubj);
		});
		
		repoSubjects.forEach((s, l) -> {
			if (l.size() > 1) {
				deployErrors.add(String.format("More ambigous schemas (%d) defined for a subject %s. Schema changes will be ignored.", l.size(), s));
				return;
			}
		});
		
		repoSubjects.entrySet().stream()
			.filter(x -> x.getValue() != null && x.getValue().size() == 1)
			.forEach(x -> {
				String s = x.getKey();
				// this means we already processed this subject/schema as part of possible dependency/reference
				if (resolvedVersions.containsKey(s)) return;
				
				List<String> errors = registerSubject(s, repoSubjects, resolvedVersions, regSubjectsMap);
				if (errors != null) {
					deployErrors.addAll(errors);
				}
		});
		deployErrors.forEach(e -> System.out.println(e));			
	}

	/**
	 * Register a given subject
	 * @param subjectName - subject to register in a registry
	 * @param repoSchema - schema to be associated with the subject
	 * @param repoSubjects - map of all repository subjects. This is used for references' lookups
	 * @param resolvedVersions - all resolved (previously registered/updated) subjects
	 * @param registrySubjects - initial version of all the subjects from a registry
	 * @return - error message
	 */
	private List<String> registerSubject(String subjectName,
			Map<String, List<RepoSchema>> repoSubjects, 
			Map<String, Subject> resolvedVersions, 
			Map<String, Subject> registrySubjects) {
		
		List<String> allErrors = new LinkedList<>();
		if (resolvedVersions.containsKey(subjectName)) return allErrors;

		RepoSchema repoSchema = repoSubjects.get(subjectName).get(0);
		List<String> dependencies = getDependencies(repoSchema, repoSubjects, resolvedVersions, registrySubjects);
		
		// first, register all dependencies, so they will be in the registry
		for (String dependency : dependencies) {
			List<String> errors = registerSubject(dependency, repoSubjects, resolvedVersions, registrySubjects);
			allErrors.addAll(errors);
			updateDependency(repoSchema, dependency, registrySubjects);
		}
		
		Subject regSubj = resolvedVersions.get(subjectName);
		if (regSubj == null) {
			regSubj = registrySubjects.get(subjectName);
		}
		
		// update compatibility, before registering new version of schema:
		if (regSubj != null && !regSubj.getCompatibility().equals(repoSchema.getMetadata().getCompatibilityLevel(subjectName))) {
			try {
				cSchemaRegistryclient.updateCompatibility(subjectName, repoSchema.getMetadata().getCompatibilityLevel(subjectName).toString());
			} catch (Exception e) {
				allErrors.add(
						String.format("Couldn't update compatibility level %s  for subject %s. Exception: %s",
								repoSchema.getMetadata().getCompatibilityLevel(subjectName).toString(),
								subjectName, 
								e.getMessage()));
			}
			// get fresh subject from registry
			try {
				regSubj = getSubject(subjectName);
				// update in cache
				resolvedVersions.put(subjectName, regSubj);
			} catch (Exception e) {
				allErrors.add(String.format("Unexpected exception while fetching of %s. Exception: %s", subjectName, e.getMessage()));
			}
		}
		
		// check if new version needs to be registered:
		if (regSubj != null) {
			ParsedSchema regSubjSchema = regSubj.getLatestSchema();
			if ( repoSchema.getSchema().equals(regSubjSchema)) {
				resolvedVersions.put(subjectName, regSubj);
				return allErrors;
			}
		}
		
		// test compatibility against the level set in repository, because it will be updated in registry
		List<String> compErrors = regSubj == null 
				? null 
				: repoSchema.getSchema().isCompatible(
					repoSchema.getMetadata().getCompatibilityLevel(subjectName), 
					regSubj.getSchemas());
			
		if (compErrors == null || compErrors.isEmpty()) {
			if ( regSubj != null && regSubj.getLatestSchema().equals(repoSchema.getSchema())) {
				// schema already exists and up to date
				return allErrors;
			}else {
				//newVersionFound.put(s, new SchemaPair(repoSchema, regSubj));
				try {
					cSchemaRegistryclient.register(subjectName, repoSchema.getSchema());
				} catch (Exception e) {
					allErrors.add(String.format("Unexpected exception during registration of %s. Exception: %s", subjectName, e.getMessage()));
				}
				// get fresh subject from registry
				try {
					regSubj = getSubject(subjectName);
					// update in cache
					resolvedVersions.put(subjectName, regSubj);
				} catch (Exception e) {
					allErrors.add(String.format("Unexpected exception while fetching %s. Exception: %s", subjectName, e.getMessage()));
				}
			}
		}else {
			allErrors.addAll(compErrors);
			return allErrors;
		}
		return allErrors;
	}

	private void updateDependency(RepoSchema repoSchema, String dependency, 
			Map<String, Subject> registrySubjects) {
		Subject depSubj = resolvedVersions.get(dependency);
		if (depSubj == null) {
			if ((depSubj = registrySubjects.get(dependency)) == null) return;
		}
		
		String schemaType = repoSchema.getSchema().schemaType();
		
		if (ProtobufSchema.TYPE.equals(schemaType)) {
			List<SchemaReference> refs = repoSchema.getSchema().references();
			if (refs == null || refs.isEmpty()) return;
			for (SchemaReference ref : refs) {
				if (ref.getName().equals(dependency)) {
					ref.setVersion(depSubj.getLatestVersionId());
					return;
				}
			}
		}
	}

	private List<String> getDependencies(RepoSchema repoSchema, Map<String, List<RepoSchema>> repoSubjects,
			Map<String, Subject> resolvedVersions2, Map<String, Subject> registrySubjects) {
		
		List<String> dependencies = new LinkedList<>();
		
		String schemaType = repoSchema.getSchema().schemaType();
		if (ProtobufSchema.TYPE.equals(schemaType)) {
			List<SchemaReference> refs = repoSchema.getSchema().references();
			if (refs == null || refs.isEmpty()) return dependencies;
			refs.forEach(ref -> dependencies.add(ref.getSubject()));
		}
		return dependencies;
	}
}
