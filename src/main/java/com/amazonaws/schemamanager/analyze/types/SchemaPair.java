package com.amazonaws.schemamanager.analyze.types;

import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

public class SchemaPair {
	private RepoSchema repoSchema;
	private Subject registrySchema;
	
	public SchemaPair() {
	}
	
	public SchemaPair(RepoSchema repoSchema, Subject registrySchema) {
		this.repoSchema = repoSchema;
		this.registrySchema = registrySchema;
	}

	public RepoSchema getRepoSchema() {
		return repoSchema;
	}
	public void setRepoSchema(RepoSchema repoSchema) {
		this.repoSchema = repoSchema;
	}
	public Subject getRegistrySchema() {
		return registrySchema;
	}
	public void setRegistrySchema(Subject registrySchema) {
		this.registrySchema = registrySchema;
	}
}
