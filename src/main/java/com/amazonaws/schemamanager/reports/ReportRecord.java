package com.amazonaws.schemamanager.reports;

import java.util.Date;

public class ReportRecord {
	
	private Date timestamp;
	private String repoSchemaName;
	private String repoSubjectName;
	private String repoPath;
	private String repoEndpoint;
	private String registrySchemaName;
	private String registrySubjectName;
	private String registryEndpoint;
	private Boolean schemasEqual;
	private Boolean schemasCompatible;
	private String repoCompatibility;
	private String registryCompatibility;
	private String details;
	
	private String appName;
	private String appEnv;
	private Integer instanceId;
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getRepoSchemaName() {
		return repoSchemaName;
	}
	public void setRepoSchemaName(String repoSchemaName) {
		this.repoSchemaName = repoSchemaName;
	}
	public String getRepoSubjectName() {
		return repoSubjectName;
	}
	public void setRepoSubjectName(String repoSubjectName) {
		this.repoSubjectName = repoSubjectName;
	}
	public String getRepoPath() {
		return repoPath;
	}
	public void setRepoPath(String repoPath) {
		this.repoPath = repoPath;
	}
	public String getRepoEndpoint() {
		return repoEndpoint;
	}
	public void setRepoEndpoint(String repoEndpoint) {
		this.repoEndpoint = repoEndpoint;
	}
	public String getRegistrySchemaName() {
		return registrySchemaName;
	}
	public void setRegistrySchemaName(String registrySchemaName) {
		this.registrySchemaName = registrySchemaName;
	}
	public String getRegistrySubjectName() {
		return registrySubjectName;
	}
	public void setRegistrySubjectName(String registrySubjectName) {
		this.registrySubjectName = registrySubjectName;
	}
	public String getRegistryEndpoint() {
		return registryEndpoint;
	}
	public void setRegistryEndpoint(String registryEndpoint) {
		this.registryEndpoint = registryEndpoint;
	}
	public Boolean getSchemasEqual() {
		return schemasEqual;
	}
	public void setSchemasEqual(Boolean schemasEqual) {
		this.schemasEqual = schemasEqual;
	}
	public Boolean getSchemasCompatible() {
		return schemasCompatible;
	}
	public void setSchemasCompatible(Boolean schemasCompatible) {
		this.schemasCompatible = schemasCompatible;
	}
	public String getRepoCompatibility() {
		return repoCompatibility;
	}
	public void setRepoCompatibility(String repoCompatibility) {
		this.repoCompatibility = repoCompatibility;
	}
	public String getRegistryCompatibility() {
		return registryCompatibility;
	}
	public void setRegistryCompatibility(String registryCompatibility) {
		this.registryCompatibility = registryCompatibility;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getAppEnv() {
		return appEnv;
	}
	public void setAppEnv(String appEnv) {
		this.appEnv = appEnv;
	}
	public Integer getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(Integer instanceId) {
		this.instanceId = instanceId;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb
		.append("{")
		.append("\"timestamp\":\"" + timestamp).append("\",")
		.append("\"repoSchemaName\":\"" + repoSchemaName).append("\",")
		.append("\"repoSubjectName\":\"" + repoSubjectName).append("\",")
		.append("\"repoPath\":\"" + repoPath).append("\",")
		.append("\"repoEndpoint\":\"" + repoEndpoint).append("\",")
		.append("\"registrySchemaName\":\"" + registrySchemaName).append("\",")
		.append("\"registrySubjectName\":\"" + registrySubjectName).append("\",")
		.append("\"registryEndpoint\":\"" + registryEndpoint).append("\",")
		
		.append("\"schemasEqual\":" + schemasEqual).append(",")
		.append("\"schemasCompatible\":" + schemasCompatible).append(",")
		
		.append("\"repoCompatibility\":\"" + repoCompatibility).append("\",")
		.append("\"registryCompatibi\":\"" + registryCompatibility).append("\",")
		.append("\"details\":\"" + details).append("\",")
		.append("\"appName\":\"" + appName).append("\",")
		.append("\"appEnv\":\"" + appEnv).append("\",")
		.append("\"instanceId\":" + instanceId).append("")
		.append("}");
		
		return sb.toString();
	}

}
