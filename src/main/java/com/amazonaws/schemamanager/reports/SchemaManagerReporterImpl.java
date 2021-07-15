package com.amazonaws.schemamanager.reports;

import java.util.stream.Collectors;

import com.amazonaws.schemamanager.analyze.types.CheckChangesResponse;
import com.amazonaws.schemamanager.properties.ReporterConfig;

public class SchemaManagerReporterImpl implements ISchemaManagerReporter {
	
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaManagerReporterImpl.class);

	private ReporterConfig config;
	
	@Override
	public void init(ReporterConfig config) {
		this.config = config;
	}
	
	

	@Override
	public void reportPRValidationResults() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportSchemaRegistryStatus(CheckChangesResponse response) {
		log.info(String.format(
				"Repository Used: %s. Branch: %s",
				config.getAppConfig().getRepoClientProperties().getRepoEndPoint(),
				config.getAppConfig().getRepoClientProperties().getBaseInfo()
				));
		
		final StringBuilder sb = new StringBuilder();
		
		// Print new subjects in repository
		sb.append(String.format(
				"New Subjects Found in Repository: %d\n",
				response.getNewSubjectsInRepo()!=null ? response.getNewSubjectsInRepo().size() : 0));
		if (response.getNewSubjectsInRepo() != null && response.getNewSubjectsInRepo().size() > 0) {
			response.getNewSubjectsInRepo().forEach((s,r) -> {
				sb.append(String.format("\tNew Subject: %s; Path: %s\n", s, r.getPath()));
			});
		}
		log.info(sb.toString());
		
		sb.setLength(0); // reset
		
		// Print Ambiguous issues
		sb.append(String.format(
					"Ambiguous Subjects Found in Repository: %d\n",
					response.getAmbiguousSubjects()!=null ? response.getAmbiguousSubjects().size() : 0));
		if (response.getAmbiguousSubjects() != null && response.getAmbiguousSubjects().size() > 0) {
			response.getAmbiguousSubjects().forEach((s,l) -> {
				sb.append(String.format("Subject: %s; defined in: \n\t%s\n", s, String.join("\n\t\t", l.stream().map(r->r.getPath()).collect(Collectors.toList()))));
			});
		}
		log.info(sb.toString());
		
		sb.setLength(0); // reset
		
		// Updated compatibility levels
		sb.append(String.format(
				"Updated Compatibility Levels Found in Repository: %d\n",
				response.getUpdateCompatibility()!=null ? response.getUpdateCompatibility().size() : 0));
		if (response.getUpdateCompatibility() != null && response.getUpdateCompatibility().size() > 0) {
			response.getUpdateCompatibility().forEach((s,c) -> {
				sb.append(String.format("\tSubject: %s; New Compatibility Level: %s\n", s, c.getRepoSchema().getMetadata().getCompatibilityLevel(s).toString()));
			});
		}
		log.info(sb.toString());
		
		sb.setLength(0); // reset
		
		// Updates in repository
		sb.append(String.format(
				"Updates Subjects Found in Repository: %d\n",
				response.getNewVersionFound()!=null ? response.getNewVersionFound().size() : 0));
		if (response.getNewVersionFound() != null && response.getNewVersionFound().size() > 0) {
			response.getNewVersionFound().forEach((s,scp) -> {
				sb.append(String.format("\tUpdated Subject: %s; Path: %s\n", s, scp.getRepoSchema().getPath()));
			});
		}
		log.info(sb.toString());
		
		sb.setLength(0); // reset
		
		// Updates in repository
		sb.append(String.format(
				"Subject not in the repository: %d\n",
				response.getNotInRepositorySubjects()!=null ? response.getNotInRepositorySubjects().size() : 0));
		if (response.getNotInRepositorySubjects() != null && response.getNotInRepositorySubjects().size() > 0) {
			response.getNotInRepositorySubjects().forEach(s -> {
				sb.append(String.format("\t%s\n", s));
			});
		}
		log.info(sb.toString());
		
		sb.setLength(0); // reset
		
		// Up to date:
		sb.append(String.format(
				"Sychronized subjects: %d\n",
				response.getUnchangedSchemas()!=null ? response.getUnchangedSchemas().size() : 0));
		if (response.getUnchangedSchemas() != null && response.getUnchangedSchemas().size() > 0) {
			response.getUnchangedSchemas().forEach(scp -> {
				sb.append(String.format("\tPath: %s\n", scp.getRepoSchema().getPath()));
			});
		}
		log.info(sb.toString());
		
		sb.setLength(0); // reset
	}

	@Override
	public void reportDeploymentStatus() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void start() {
		log.info(
				String.format("======= REPORT START. Application: %s-%d-%s =======",
				config.getAppConfig().getApplicationName(),
				config.getAppConfig().getApplicationInstanceId(),
				config.getAppConfig().getEnvironment()));
		
	}



	@Override
	public void close() {
		log.info("======= REPORT END =======");
		
	}

}
