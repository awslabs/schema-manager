package com.amazonaws.schemamanager.properties;

public class AnalyzerConfig {
	// DO NOT GENERATE equals, hashCode and toString for appConfig (cyclic invocation).
	private AppConfig appConfig;
	
	private String defaultCompatibility;
	private Boolean allowSchemaNameDuplicates;

	public String getDefaultCompatibility() {
		return defaultCompatibility;
	}

	public void setDefaultCompatibility(String defaultCompatibility) {
		this.defaultCompatibility = defaultCompatibility;
	}

	public Boolean getAllowSchemaNameDuplicates() {
		return allowSchemaNameDuplicates != null ? allowSchemaNameDuplicates : Boolean.FALSE;
	}

	public void setAllowSchemaNameDuplicates(Boolean allowSchemaNameDuplicates) {
		this.allowSchemaNameDuplicates = allowSchemaNameDuplicates;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultCompatibility == null) ? 0 : defaultCompatibility.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalyzerConfig other = (AnalyzerConfig) obj;
		if (defaultCompatibility == null) {
			if (other.defaultCompatibility != null)
				return false;
		} else if (!defaultCompatibility.equals(other.defaultCompatibility))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnalyzerConfig [defaultCompatibility=" + defaultCompatibility + "]";
	}
	
}
