package com.amazonaws.schemamanager.repo.datatypes;

import io.confluent.kafka.schemaregistry.ParsedSchema;

public class RepoSchema {
	
	private ParsedSchema schema;
	private RepoSchemaMetadata metadata;
	private String path;

	public ParsedSchema getSchema() {
		return schema;
	}

	public void setSchema(ParsedSchema schema) {
		this.schema = schema;
	}

	public RepoSchemaMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(RepoSchemaMetadata metadata) {
		this.metadata = metadata;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
		RepoSchema other = (RepoSchema) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RepoSchema [schema=" + schema + ", metadata=" + metadata + ", path=" + path + "]";
	}


}
