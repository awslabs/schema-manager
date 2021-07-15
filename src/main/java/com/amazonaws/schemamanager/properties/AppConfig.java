package com.amazonaws.schemamanager.properties;

public class AppConfig {
	
	public static final String DEFAULT_COMPATIBILITY = "FULL_TRANSITIVE";
	
	private AppConfig() {
	}
		
	private String applicationName;
	private Integer applicationInstanceId;
	private String environment;
	
	private String repoClientClass;
	private String registryClientClass;
	private String reporterClass;
	private String analyzerClass;
	
	private RepoClientConfig repoClientProperties;
	private RegistryClientConfig registryClientProperties;
	private LocalRepoClientConfig localRepoClientProperties;
	
	private Boolean deploy;


	private AnalyzerConfig analyzerProperties;
	private ReporterConfig reporterProperties;
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public Integer getApplicationInstanceId() {
		return applicationInstanceId;
	}
	public void setApplicationInstanceId(Integer applicationInstanceId) {
		this.applicationInstanceId = applicationInstanceId;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getRepoClientClass() {
		return repoClientClass;
	}
	public void setRepoClientClass(String repoClientClass) {
		this.repoClientClass = repoClientClass;
	}
	public String getRegistryClientClass() {
		return registryClientClass;
	}
	public void setRegistryClientClass(String registryClientClass) {
		this.registryClientClass = registryClientClass;
	}
	public String getReporterClass() {
		return reporterClass;
	}
	public void setReporterClass(String reporterClass) {
		this.reporterClass = reporterClass;
	}
	public String getAnalyzerClass() {
		return analyzerClass;
	}
	public void setAnalyzerClass(String analyzerClass) {
		this.analyzerClass = analyzerClass;
	}
	public RepoClientConfig getRepoClientProperties() {
		return repoClientProperties;
	}
	public void setRepoClientProperties(RepoClientConfig repoClientProperties) {
		this.repoClientProperties = repoClientProperties;
		if (repoClientProperties != null) {
			repoClientProperties.setAppConfig(this);
		}
	}
	public RegistryClientConfig getRegistryClientProperties() {
		return registryClientProperties;
	}
	public void setRegistryClientProperties(RegistryClientConfig registryClientProperties) {
		this.registryClientProperties = registryClientProperties;
		if (registryClientProperties != null) {
			registryClientProperties.setAppConfig(this);
		}
	}
	public AnalyzerConfig getAnalyzerProperties() {
		return analyzerProperties;
	}
	public void setAnalyzerProperties(AnalyzerConfig analyzerProperties) {
		this.analyzerProperties = analyzerProperties;
		if (analyzerProperties != null) {
			analyzerProperties.setAppConfig(this);
		}
	}
	public LocalRepoClientConfig getLocalRepoClientConfig() {
		return localRepoClientProperties;
	}
	public void setLocalRepoClientProperties(LocalRepoClientConfig localRepoClientProperties) {
		this.localRepoClientProperties = localRepoClientProperties;
		if(localRepoClientProperties != null) {
			localRepoClientProperties.setAppConfig(this);
		}
	}

	public ReporterConfig getReporterProperties() {
		return reporterProperties;
	}
	public void setReporterProperties(ReporterConfig reporterProperties) {
		this.reporterProperties = reporterProperties;
		if (reporterProperties != null) {
			reporterProperties.setAppConfig(this);
		}
	}
	public Boolean getDeploy() {
		return deploy;
	}
	public Boolean isDeploy() {
		return (deploy!= null) && deploy;
	}
	public void setDeploy(Boolean deploy) {
		this.deploy = deploy;
	}
	
}
