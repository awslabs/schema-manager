package com.amazonaws.schemamanager.analyze.types;

import java.util.List;
import java.util.Map;

import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;


public class CheckChangesResponse {
	
	private Map<String, RepoSchema>  newSubjectsInRepo;
	private Map<String, SchemaPair> newVersionFound;
	private List<SchemaPair> unchangedSchemas;
	private Map<String, SchemaPair> updateCompatibility;
	private Map<String, List<RepoSchema>> ambiguousSubjects;
	private Map<String, CompatibilityError> compatibilityErrors;
	private List<Subject> notInRepositorySubjects;
	
	public CheckChangesResponse() {
		
	}
	
	public CheckChangesResponse(Map<String, RepoSchema>  newSubjectsInRepo, Map<String, SchemaPair> newVersionFound,
			List<SchemaPair> unchangedSchemas, Map<String, SchemaPair> updateCompatibility,
			Map<String, List<RepoSchema>> ambiguousSubjects, Map<String, CompatibilityError> compatibilityErrors,
			List<Subject> notInRepositorySubjects) {
		super();
		this.newSubjectsInRepo = newSubjectsInRepo;
		this.newVersionFound = newVersionFound;
		this.unchangedSchemas = unchangedSchemas;
		this.updateCompatibility = updateCompatibility;
		this.ambiguousSubjects = ambiguousSubjects;
		this.compatibilityErrors = compatibilityErrors;
		this.notInRepositorySubjects = notInRepositorySubjects;;
	}

	public Map<String, RepoSchema>  getNewSubjectsInRepo() {
		return newSubjectsInRepo;
	}

	public void setNewSubjectsInRepo(Map<String, RepoSchema>  newSubjectsInRepo) {
		this.newSubjectsInRepo = newSubjectsInRepo;
	}

	public Map<String, SchemaPair> getNewVersionFound() {
		return newVersionFound;
	}

	public void setNewVersionFound(Map<String, SchemaPair> newVersionFound) {
		this.newVersionFound = newVersionFound;
	}

	public List<SchemaPair> getUnchangedSchemas() {
		return unchangedSchemas;
	}

	public void setUnchangedSchemas(List<SchemaPair> unchangedSchemas) {
		this.unchangedSchemas = unchangedSchemas;
	}

	public Map<String, SchemaPair> getUpdateCompatibility() {
		return updateCompatibility;
	}

	public void setUpdateCompatibility(Map<String, SchemaPair> updateCompatibility) {
		this.updateCompatibility = updateCompatibility;
	}

	public Map<String, List<RepoSchema>> getAmbiguousSubjects() {
		return ambiguousSubjects;
	}

	public void setAmbiguousSubjects(Map<String, List<RepoSchema>> ambiguousSubjects) {
		this.ambiguousSubjects = ambiguousSubjects;
	}

	public Map<String, CompatibilityError> getCompatibilityErrors() {
		return compatibilityErrors;
	}

	public void setCompatibilityErrors(Map<String, CompatibilityError> compatibilityErrors) {
		this.compatibilityErrors = compatibilityErrors;
	}

	public List<Subject> getNotInRepositorySubjects() {
		return notInRepositorySubjects;
	}

	public void setNotInRepositorySubjects(List<Subject> notInRepositorySubjects) {
		this.notInRepositorySubjects = notInRepositorySubjects;
	}
}
