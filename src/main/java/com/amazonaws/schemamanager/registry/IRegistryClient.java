package com.amazonaws.schemamanager.registry;

import java.io.IOException;
import java.util.List;

import com.amazonaws.schemamanager.properties.RegistryClientConfig;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

public interface IRegistryClient {
    
    public void init(RegistryClientConfig config);
    public List<Subject> getSubjects() throws IOException, RestClientException;
    public ParsedSchema getSchema(String schema);
    public ParsedSchema getSchema(String subject, Integer schemaId) throws IOException, RestClientException;
    public void registerSchema(String subject, ParsedSchema schema) throws Exception;
	public CachedSchemaRegistryClient getSchemaRegistryClient();
	public void deployChanges(List<RepoSchema> repoSchemas);
	public void deployChanges(List<RepoSchema> repoSchemas, List<Subject> registrySubjects);
}