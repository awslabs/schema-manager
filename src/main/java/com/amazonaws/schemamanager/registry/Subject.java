package com.amazonaws.schemamanager.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.ParsedSchema;


/**
 * Subject is the parent of Schema. It contains a list of all schemas associated with the subject, compatibility for the subject and version ids for the schemas.
 */
public class Subject {
	
	private String name;
	private CompatibilityLevel compatibility;
	
	private TreeMap<Integer, ParsedSchema> versionedSchemas = new TreeMap<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CompatibilityLevel getCompatibility() {
		return compatibility;
	}
	public void setCompatibility(CompatibilityLevel compatibility) {
		this.compatibility = compatibility;
	}
	
	public List<Integer> getVersionIds() {
		return new LinkedList<Integer>(versionedSchemas.keySet());
	}
	public List<ParsedSchema> getSchemas() {
		return new LinkedList<ParsedSchema>(versionedSchemas.values());
	}
	
	public void addSchema(ParsedSchema parsedSchema, Integer versionId) {
		versionedSchemas.put(versionId, parsedSchema);
	}
	
	public ParsedSchema getSchema(Integer versionId) {
		if (versionId == null) return null;
		return versionedSchemas.get(versionId);
	}

	public Integer getLatestVersionId() {
		return versionedSchemas.lastKey();
	}
	
	public ParsedSchema getLatestSchema() {
		return versionedSchemas.lastEntry().getValue();
	}
}
