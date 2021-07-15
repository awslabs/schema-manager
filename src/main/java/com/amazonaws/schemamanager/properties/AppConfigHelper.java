package com.amazonaws.schemamanager.properties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.yaml.snakeyaml.Yaml;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;

public class AppConfigHelper {
	
	private static AppConfig appConfig;

	public static void initConfig(String path) throws IOException {
		if (appConfig != null) return;
		
		Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream( Paths.get( path ) ) ) {
        	appConfig = yaml.loadAs(in, AppConfig.class);
        }
		return;
	}
	
	public static AppConfig getConfig() {
		return appConfig;
	}
	
	/**
	 * Defines default compatibility level.
	 * If property file doesn't have value defined, or if this value not valid, FULL_TRANSITIVE is returned as default.
	 *   
	 * @return default compatibility.
	 */
	public static CompatibilityLevel getDefaultCompatibility() {
		try {
			return CompatibilityLevel.valueOf(AppConfig.DEFAULT_COMPATIBILITY);
		}catch(Exception e) {
			return CompatibilityLevel.FULL_TRANSITIVE;
		}
	}
	
	public static void main(String[] args) throws IOException {
		initConfig("./src/main/resources/sample_conf.yml");
		System.out.println( getConfig() );
	}
	
}
