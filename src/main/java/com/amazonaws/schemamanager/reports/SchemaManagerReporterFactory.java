package com.amazonaws.schemamanager.reports;

import com.amazonaws.schemamanager.properties.AppConfig;
import com.amazonaws.schemamanager.properties.AppConfigHelper;
import com.amazonaws.schemamanager.properties.ReporterConfig;
import com.amazonaws.schemamanager.registry.IRegistryClient;

public class SchemaManagerReporterFactory {
	
	public static ISchemaManagerReporter create() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		ISchemaManagerReporter reporter = null;
        AppConfig appConfig = AppConfigHelper.getConfig();
		ReporterConfig config = appConfig.getReporterProperties();
		
        String reporterClassName = appConfig.getReporterClass();
        if(reporterClassName != null){
        	reporter = (ISchemaManagerReporter) Class.forName(reporterClassName).newInstance();
        } else {
        	reporter = (ISchemaManagerReporter) Class.forName(SchemaManagerReporterImpl.class.getName()).newInstance();
        }
        reporter.init(config);
        return reporter;

	}
}
