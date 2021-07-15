package com.amazonaws.schemamanager.reports;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.schemamanager.analyze.types.CheckChangesResponse;
import com.amazonaws.schemamanager.properties.ReporterConfig;
import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3Object;

public class SchemaManagerReporterS3 implements ISchemaManagerReporter {
	
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaManagerReporterS3.class);

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
		
		List<ReportRecord> records = convertToRecords(response);
		
		Regions region = Regions.DEFAULT_REGION;
		String regionStr = config.getRegion();
		if (regionStr != null && !regionStr.isEmpty()) {
			try {
				region = Regions.valueOf(regionStr);
			}catch(Exception e) {
				log.error("Cannot use region: "+ regionStr, e);
			}
		}
		
        String bucketName = config.getBucketName();
        String key = config.getPrefixDir() + String.format(config.getFilenamePattern(), "status", getFileDate());
        
        try {
        	AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider())
                    .withRegion(region)
                    .build();
	
        	// Set the pre-signed URL to expire after one hour.
        	java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60;
            expiration.setTime(expTimeMillis);
            
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
                    .withMethod(HttpMethod.PUT)
                    .withExpiration(expiration);
            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            // Create the connection and use it to upload the new object using the pre-signed URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            for (ReportRecord r: records) {
            	out.write(r.toJson()+ "\n");
            }
            out.close();
            
            connection.getResponseCode();
            
            log.info("Create Object HTTP response code: " + connection.getResponseCode());
            
            // validation
            S3Object object = s3Client.getObject(bucketName, key);
            log.info("Object " + object.getKey() + " created in bucket " + object.getBucketName());

        }catch(Exception e) {
        	 e.printStackTrace();
        }
	}
	
	private String getFileDate() {
		Date d = new Date(System.currentTimeMillis());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

		return simpleDateFormat.format(d);
	}



	protected List<ReportRecord> convertToRecords(CheckChangesResponse response){
		List<ReportRecord> result = new LinkedList<>();
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Date date = cal.getTime();
		
		// Print new subjects in repository		
		if (response.getNewSubjectsInRepo() != null && response.getNewSubjectsInRepo().size() > 0) {
			response.getNewSubjectsInRepo().forEach((s,r) -> {
				ReportRecord rr = createRecord(date, config);
				result.add(rr);
				rr.setRepoSubjectName(s);
				rr.setRepoPath(r.getPath());
				rr.setSchemasEqual(false);
				rr.setRepoCompatibility(r.getMetadata().getCompatibilityLevel(s).toString());
			});
		}
		
		// Print Ambiguous issues		
		if (response.getAmbiguousSubjects() != null && response.getAmbiguousSubjects().size() > 0) {
			response.getAmbiguousSubjects().forEach((s,l) -> {
				l.forEach(r -> {
					ReportRecord rr = createRecord(date, config);
					result.add(rr);
					rr.setRepoSubjectName(s);
					rr.setRepoPath(r.getPath());
					rr.setSchemasEqual(false);
					rr.setRepoCompatibility(r.getMetadata().getCompatibilityLevel(s).toString());
					rr.setDetails("Ambiguous Subjects");
				});
			});
		}
		
		// Updated compatibility levels
		if (response.getUpdateCompatibility() != null && response.getUpdateCompatibility().size() > 0) {
			response.getUpdateCompatibility().forEach((s,scp) -> {
				RepoSchema repoSchema = scp.getRepoSchema();
				Subject regSchema = scp.getRegistrySchema();
				ReportRecord rr = createRecord(date, config);
				result.add(rr);
				rr.setRepoSubjectName(s);
				rr.setRepoPath(repoSchema.getPath());
				rr.setRepoCompatibility(repoSchema.getMetadata().getCompatibilityLevel(s).toString());

				rr.setRegistrySubjectName(regSchema.getName());
				rr.setRegistryCompatibility(regSchema.getCompatibility().toString());
				
				if (response.getNewVersionFound() != null && response.getNewVersionFound().get(s) != null) {
					rr.setSchemasEqual(false);
				}
				if (response.getNewVersionFound() != null  && response.getNewVersionFound().get(s) != null 
						&& response.getCompatibilityErrors() != null && response.getCompatibilityErrors().get(s) == null ) {
					rr.setSchemasCompatible(true);
				}else if(response.getNewVersionFound() != null && response.getNewVersionFound().get(s) != null && response.getCompatibilityErrors().get(s) != null) {
					rr.setSchemasCompatible(false);
					rr.setDetails(String.join(";\n", response.getCompatibilityErrors().get(s).getErrors()));
				}
			});
		}
		
		// Updates in repository
		if (response.getNewVersionFound() != null && response.getNewVersionFound().size() > 0) {
			response.getNewVersionFound().forEach((s,scp) -> {
				if (response.getUpdateCompatibility() != null && response.getUpdateCompatibility().get(s) != null) {
					// previously handled with new compatibility
					return;
				}
				ReportRecord rr = createRecord(date, config);
				result.add(rr);
				rr.setRepoSubjectName(s);
				rr.setRepoPath(scp.getRepoSchema().getPath());
				rr.setRepoCompatibility(scp.getRepoSchema().getMetadata().getCompatibilityLevel(s).toString());

				rr.setRegistrySubjectName(scp.getRegistrySchema().getName());
				rr.setRegistryCompatibility(scp.getRegistrySchema().getCompatibility().toString());
			});
		}

		// Not in repository		
		if (response.getNotInRepositorySubjects() != null && response.getNotInRepositorySubjects().size() > 0) {
			response.getNotInRepositorySubjects().forEach(s -> {
				ReportRecord rr = createRecord(date, config);
				result.add(rr);
				rr.setRegistrySubjectName(s.getName());
				rr.setRegistryCompatibility(s.getCompatibility().toString());
			});
		}

		// Up to date:
		if (response.getUnchangedSchemas() != null && response.getUnchangedSchemas().size() > 0) {
			response.getUnchangedSchemas().forEach(scp -> {
				ReportRecord rr = createRecord(date, config);
				result.add(rr);
				rr.setRepoSubjectName(scp.getRegistrySchema().getName());
				rr.setRepoSchemaName(scp.getRepoSchema().getSchema().name());
				rr.setRepoPath(scp.getRepoSchema().getPath());
				rr.setSchemasEqual(true);
				rr.setRepoCompatibility(scp.getRepoSchema().getMetadata().getCompatibilityLevel(scp.getRegistrySchema().getName()).toString());
				rr.setRegistrySubjectName(scp.getRegistrySchema().getName());
				rr.setRegistryCompatibility(scp.getRegistrySchema().getCompatibility().toString());
			});
		}

		return result;
	}

	private ReportRecord createRecord(Date date, ReporterConfig config2) {
		String repoEndpoint = config.getAppConfig().getRepoClientProperties().getRepoEndPoint() + " (" + config.getAppConfig().getRepoClientProperties().getBaseInfo() + ")";
		String registryEndpoint = config.getAppConfig().getRegistryClientProperties().getRegistryEndPoint();
		
		String appName = config.getAppConfig().getApplicationName();
		String appEnv = config.getAppConfig().getEnvironment();
		Integer appInstanceId = config.getAppConfig().getApplicationInstanceId();
		
		ReportRecord rr = new ReportRecord();
		rr.setTimestamp(date);
		rr.setAppName(appName);
		rr.setAppEnv(appEnv);
		rr.setInstanceId(appInstanceId);
		rr.setRepoEndpoint(repoEndpoint);
		rr.setRegistryEndpoint(registryEndpoint);
		return rr;
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
