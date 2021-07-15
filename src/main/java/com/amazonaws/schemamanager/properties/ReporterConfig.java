package com.amazonaws.schemamanager.properties;

public class ReporterConfig {
	// DO NOT GENERATE equals, hashCode and toString for this member (cyclic invocation).
	private AppConfig appConfig;
	private String bucketName;
	private String region;
	private String prefixDir;
	private String filenamePattern;
	private Boolean useHeader;
	
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getPrefixDir() {
		return prefixDir;
	}
	public void setPrefixDir(String prefixDir) {
		this.prefixDir = prefixDir;
	}
	public String getFilenamePattern() {
		return filenamePattern;
	}
	public void setFilenamePattern(String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}
	public AppConfig getAppConfig() {
		return appConfig;
	}
	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public Boolean getUseHeader() {
		return useHeader;
	}
	public void setUseHeader(Boolean useHeader) {
		this.useHeader = useHeader;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	
}
