package com.amazonaws.schemamanager.repo.datatypes;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.schemamanager.properties.AppConfigHelper;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;

public class RepoSchemaMetadata {
	private Map<String, Map<String, String>> subjects;
	private String defaultCompatibility;
	private String schemaName;
	
	
	/**
	 * Computes a subject's compatibility.
	 * If not defined explicitly, will be taking from the default values.
	 * @param subject
	 * @return
	 */
	public CompatibilityLevel getCompatibilityLevel(String subject) {
		String compStr;
		if (subject == null || subjects == null) {
			compStr = getDefaultCompatibility();
		}else {
			compStr = subjects.getOrDefault(subject, new HashMap<String, String>())
					.getOrDefault("compatibility", getDefaultCompatibility());
		}
		return compStr != null? CompatibilityLevel.valueOf(compStr) : AppConfigHelper.getDefaultCompatibility();
	}


	public Map<String, Map<String, String>> getSubjects() {
		return subjects;
	}


	public void setSubjects(Map<String, Map<String, String>> subjects) {
		this.subjects = subjects;
	}


	public String getDefaultCompatibility() {
		return defaultCompatibility;
	}


	public void setDefaultCompatibility(String defaultCompatibility) {
		this.defaultCompatibility = defaultCompatibility;
	}


	public String getSchemaName() {
		return schemaName;
	}


	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultCompatibility == null) ? 0 : defaultCompatibility.hashCode());
		result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
		result = prime * result + ((subjects == null) ? 0 : subjects.hashCode());
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
		RepoSchemaMetadata other = (RepoSchemaMetadata) obj;
		if (defaultCompatibility == null) {
			if (other.defaultCompatibility != null)
				return false;
		} else if (!defaultCompatibility.equals(other.defaultCompatibility))
			return false;
		if (schemaName == null) {
			if (other.schemaName != null)
				return false;
		} else if (!schemaName.equals(other.schemaName))
			return false;
		if (subjects == null) {
			if (other.subjects != null)
				return false;
		} else if (!subjects.equals(other.subjects))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "RepoSchemaMetadata [subjects=" + subjects + ", defaultCompatibility=" + defaultCompatibility
				+ ", schemaName=" + schemaName + "]";
	}
	
	public void merge(RepoSchemaMetadata other) {
		if (other == null) {
			return;
		}
		
		if (other.getSchemaName() != null &&!other.getSchemaName().isEmpty()) {
			this.setSchemaName(other.getSchemaName());
		}

		if (other.getDefaultCompatibility() != null &&!other.getDefaultCompatibility().isEmpty()) {
			this.setDefaultCompatibility(other.getDefaultCompatibility());
		}
		
		Map<String, Map<String, String>> otherSubjects = other.getSubjects();
		if (otherSubjects != null && !otherSubjects.isEmpty()) {
			if (this.subjects == null) {
				this.subjects = otherSubjects;
			}else {
				otherSubjects.forEach((s, v) -> this.subjects.put(s, v));
			}
		}
	}
}
