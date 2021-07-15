package com.amazonaws.schemamanager.analyze;

import java.util.List;

import com.amazonaws.schemamanager.analyze.types.CheckChangesRequest;
import com.amazonaws.schemamanager.analyze.types.CheckChangesResponse;
import com.amazonaws.schemamanager.properties.AnalyzerConfig;
import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.ParsedSchema;

public interface ISchemaAnalyzer {
	public List<ValidationError> testCompatibility(List<RepoSchema> newSchemas, List<Subject> registrySchemas);

	public boolean testCompatibility(ParsedSchema newSchema, List<? extends ParsedSchema> existingSchemas, CompatibilityLevel compatibilityMode);

	public boolean testCompatibility(String newSchema, String existingSchema, String compatibilityMode);

	/**
	 * Tests compatibility between two RepoSchema sets.
	 * Every schema may have compatibility defined in metadata. If this not defined, <pre>defaultCompatibility</pre> will be used.
	 * @param newSchemas - collection of updated schemas that needs to be tested for compatibility against existing ones (usually comes from Pull Requests)
	 * @param existingSchemas - collection of existing schemas (usually comes from repository)
	 * @param defaultCompatibility
	 */
	public List<ValidationError> testCompatibility(
			List<RepoSchema> newSchemas, 
			List<RepoSchema> existingSchemas,
			CompatibilityLevel defaultCompatibility);
	
	public List<ValidationError> validateSchemas(List<RepoSchema> newSchemas);
	
	public CheckChangesResponse checkChanges(CheckChangesRequest request);

	public void init(AnalyzerConfig config);
	
}
