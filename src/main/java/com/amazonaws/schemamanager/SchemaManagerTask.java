package com.amazonaws.schemamanager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.amazonaws.schemamanager.analyze.ISchemaAnalyzer;
import com.amazonaws.schemamanager.analyze.SchemaAnalyzerFactory;
import com.amazonaws.schemamanager.analyze.types.CheckChangesRequest;
import com.amazonaws.schemamanager.analyze.types.CheckChangesResponse;
import com.amazonaws.schemamanager.analyze.types.SchemaPair;
import com.amazonaws.schemamanager.properties.AppConfig;
import com.amazonaws.schemamanager.properties.AppConfigHelper;
import com.amazonaws.schemamanager.registry.IRegistryClient;
import com.amazonaws.schemamanager.registry.RegistryClientFactory;
import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.IRepoClient;
import com.amazonaws.schemamanager.repo.RepoClientFactory;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;
import com.amazonaws.schemamanager.reports.ISchemaManagerReporter;
import com.amazonaws.schemamanager.reports.SchemaManagerReporterFactory;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

public class SchemaManagerTask implements ISchemaManagerRunner{
	
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaManagerTask.class);

	private IRegistryClient registryClient;
	
	public void run() throws Exception{
		AppConfig config = AppConfigHelper.getConfig();
		
		IRepoClient baselineRepoClient = RepoClientFactory.createClient();
		List<RepoSchema> repoSchemas = baselineRepoClient.getSchemaList();
		
		log.info("Conncted to baseline repository. Schemas found: " + repoSchemas.size());

		registryClient = RegistryClientFactory.createClient(null);
		List<Subject> registrySubjects = registryClient.getSubjects();
		
		CheckChangesRequest taskRequest = new CheckChangesRequest();
		taskRequest.setRepoSchemas(repoSchemas);
		taskRequest.setSubjects(registrySubjects);
		
		ISchemaAnalyzer analyzer = SchemaAnalyzerFactory.createAnalyzer();
		ISchemaManagerReporter reporter = SchemaManagerReporterFactory.create();
		
		
		CheckChangesResponse response = analyzer.checkChanges(taskRequest);
		reporter.start();
		reporter.reportSchemaRegistryStatus(response);
		
		if (config.isDeploy()) {
//			deployChanges(config, response.getNewSubjectsInRepo(), response.getNewVersionFound(), response.getUpdateCompatibility());
			registryClient.deployChanges(repoSchemas, registrySubjects);
		}
		reporter.close();

//		registryClient.registerSchema(null, null);

	}
	
	private void deployChanges(AppConfig config, Map<String, RepoSchema> newSubjectsInRepo, Map<String, SchemaPair> newVersionFound,
			Map<String, SchemaPair> updateCompatibility) {
		
		// First, let's update compatibility:
		CachedSchemaRegistryClient sr = registryClient.getSchemaRegistryClient();
		if (updateCompatibility != null) {
			updateCompatibility.forEach((s, scp) -> {
				try {
					sr.updateCompatibility(s, scp.getRepoSchema().getMetadata().getCompatibilityLevel(s).toString());
				} catch (Exception e) {
					log.error("Couldn't update compatibility level " + scp.getRepoSchema().getMetadata().getCompatibilityLevel(s).toString() + " for subject " + s, e);
				}
			});
		}
		
		
		// Let's deploy updates in existing schemas:
		if (newVersionFound != null) {
			newVersionFound.forEach((s, scp) -> {
				try {
					sr.register(s, scp.getRepoSchema().getSchema());
				} catch (IOException | RestClientException e) {
					log.error("Couldn't register subject " + s + "from repository schema at " + scp.getRepoSchema().getPath(), e);
				}
			});
		}
		
		// Let's register brand new schemas:
		if (newSubjectsInRepo != null) {
			newSubjectsInRepo.forEach((s, r) -> {
				if (s == null) return;
				try {
					sr.register(s, r.getSchema());
				} catch (Exception e) {
					log.error("Couldn't register subject " + s + "from repository schema at " + r.getPath(), e);
				}
				try {
					sr.updateCompatibility(s, r.getMetadata().getCompatibilityLevel(s).toString());
				} catch (Exception e) {
					log.error("Couldn't update compatibility level " + r.getMetadata().getCompatibilityLevel(s).toString() + " for subject " + s, e);
				}
			});
		}
		
	}
}
