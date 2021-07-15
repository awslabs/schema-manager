package com.amazonaws.schemamanager;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.schemamanager.analyze.ISchemaAnalyzer;
import com.amazonaws.schemamanager.analyze.SchemaAnalyzerFactory;
import com.amazonaws.schemamanager.analyze.ValidationError;
import com.amazonaws.schemamanager.properties.AppConfigHelper;
import com.amazonaws.schemamanager.registry.IRegistryClient;
import com.amazonaws.schemamanager.registry.RegistryClientFactory;
import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.IRepoClient;
import com.amazonaws.schemamanager.repo.RepoClientFactory;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

public class SchemaManagerPrValidator implements ISchemaManagerRunner{
	
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaManagerPrValidator.class);

	public void run() throws Exception{
		
//		AppConfig config = 
		AppConfigHelper.getConfig();
		
		IRepoClient prRepoClient = RepoClientFactory.createClient(RepoClientFactory.LOCAL_REPO);
		//prRepoClient.init(config.getLocalRepoClientConfig());
		
		IRepoClient baselineRepoClient = RepoClientFactory.createClient();
//		baselineRepoClient.init(config.getRepoClientProperties());
		
		List<RepoSchema> prSchemas = prRepoClient.getSchemaList();
		log.info("Conncted to PR  repository. Schemas found: " + prSchemas.size());
		List<RepoSchema> repoSchemas = baselineRepoClient.getSchemaList();
		log.info("Conncted to baseline repository. Schemas found: " + repoSchemas.size());
		
		ISchemaAnalyzer analyzer = SchemaAnalyzerFactory.createAnalyzer();
		
		List<ValidationError> errors = analyzer.validateSchemas(prSchemas);
		if (errors != null) {
			errors.forEach(e -> e.setErrorMsg("Validation Error. " + e.getErrorMsg()));
		}
		log.info("Validated schemas in PR. Errors found: " + (errors == null? 0 : errors.size()));
		prSchemas = prSchemas.stream().filter(s -> s.getSchema() != null).collect(Collectors.toList());
		List<ValidationError> compErrors = analyzer.testCompatibility(prSchemas, repoSchemas, AppConfigHelper.getDefaultCompatibility());
		log.info("Tested compatibility against baseline. Compatibility errors found: " + (compErrors == null? 0 : compErrors.size()));
		if (compErrors != null) {
			compErrors.forEach(e -> {
				e.setErrorMsg("Stage: PR -> Base Repo Branch. " + e.getErrorMsg());
			});
			errors.addAll(compErrors);
		}
		
		IRegistryClient registryClient = RegistryClientFactory.createClient(null);
		List<Subject> registrySubjects = registryClient.getSubjects();
		
		compErrors = analyzer.testCompatibility(prSchemas, registrySubjects);
		if (compErrors != null) {
			compErrors.forEach(e -> {
				e.setErrorMsg("Stage: PR -> Registry. " + e.getErrorMsg());
			});
			errors.addAll(compErrors);
		}
		
		if (!errors.isEmpty()) {
			errors.forEach(e -> {
				log.error(e.getErrorMsg());
			});
			throw new Exception("PR Validation Failed");
		}
	}
}
 