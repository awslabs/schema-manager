package com.amazonaws.schemamanager.properties;

public class LocalRepoClientConfig implements IRepoClientConfig{

	// DO NOT GENERATE equals, hashCode and toString for this member (cyclic invocation).
	
	private AppConfig appConfig;
	
	private String baseInfo;
	
	private String pathPrefix;
	private String subjectNamePattern;
	private String schemaNamePattern;


	public AppConfig getAppConfig() {
		return appConfig;
	}
	
	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public String getBaseInfo() {
		return baseInfo;
	}
	public void setBaseInfo(String baseInfo) {
		this.baseInfo = baseInfo;
	}

    public String getLocalBaseInfo() {
        return null;
    }

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public String getSubjectNamePattern() {
		return subjectNamePattern;
	}

	public void setSubjectNamePattern(String subjectNamePattern) {
		this.subjectNamePattern = subjectNamePattern;
	}

	public String getSchemaNamePattern() {
		return schemaNamePattern;
	}

	public void setSchemaNamePattern(String schemaNamePattern) {
		this.schemaNamePattern = schemaNamePattern;
	}

	@Override
	public String toString() {
		return "LocalRepoClientConfig [baseInfo=" + baseInfo + ", pathPrefix=" + pathPrefix
				+ ", subjectNamePattern=" + subjectNamePattern + ", schemaNamePattern=" + schemaNamePattern + "]";
	}
}
