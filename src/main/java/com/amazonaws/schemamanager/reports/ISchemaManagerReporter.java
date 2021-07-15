package com.amazonaws.schemamanager.reports;

import com.amazonaws.schemamanager.analyze.types.CheckChangesResponse;
import com.amazonaws.schemamanager.properties.ReporterConfig;

public interface ISchemaManagerReporter {

	public void init(ReporterConfig config);
	
	public void start();
	public void close();
	
	public void reportPRValidationResults();
	
	public void reportSchemaRegistryStatus(CheckChangesResponse response);
	
	public void reportDeploymentStatus();
	
}
