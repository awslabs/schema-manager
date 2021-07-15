package com.amazonaws.schemamanager.analyze.types;

import java.util.List;

import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

public class CheckChangesRequest {
	
	private List<RepoSchema> repoSchemas;
	private List<Subject> subjects;
	
	public List<Subject> getSubjects() {
		return subjects;
	}
	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}
	public List<RepoSchema> getRepoSchemas() {
		return repoSchemas;
	}
	public void setRepoSchemas(List<RepoSchema> repoSchemas) {
		this.repoSchemas = repoSchemas;
	}

}
