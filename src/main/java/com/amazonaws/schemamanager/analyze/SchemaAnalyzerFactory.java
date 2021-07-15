package com.amazonaws.schemamanager.analyze;

import com.amazonaws.schemamanager.properties.AnalyzerConfig;
import com.amazonaws.schemamanager.properties.AppConfigHelper;

public class SchemaAnalyzerFactory {
	
	public static ISchemaAnalyzer createAnalyzer() {
		AnalyzerConfig config = AppConfigHelper.getConfig().getAnalyzerProperties();
		ISchemaAnalyzer analyzer = new SchemaAnalyzerImpl();
		analyzer.init(config);
		return analyzer;
	}
}
