package com.amazonaws.schemamanager.repo;

import java.io.IOException;
import java.util.List;

import com.amazonaws.schemamanager.properties.IRepoClientConfig;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

/**
 * Interface for all Repository type clients. 
 * Example: File System
 */
public interface IRepoClient {
	public void init(IRepoClientConfig config);
	public List<RepoSchema> getSchemaList() throws IOException;
	public List<RepoSchema> getSchemaList(boolean useFilters) throws IOException;
	public RepoSchema getSchema(String schemaName);
	public RepoSchema getSchemaUpdates(String schemaName);
}
