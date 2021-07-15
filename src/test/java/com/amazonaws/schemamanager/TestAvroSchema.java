package com.amazonaws.schemamanager;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;

import com.amazonaws.schemamanager.repo.FileSystemRepoClient;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;

public class TestAvroSchema {
	public static void main(String[] args) throws Exception {
		List<String> refs = Arrays.asList(
				//list of files to the schemas
				""
		);
		
		Map<String, String> rrs = new HashMap<>();
		Map<String, Schema> rawSchemas = new HashMap<>();
		Map<String, AvroSchema> parsedSchemas = new HashMap<>();
		refs.forEach(r -> {
			InputStream in;
			try {
				in = Files.newInputStream(Paths.get(r));
				String schemaString =  FileSystemRepoClient.convertStreamToString(in);
				AvroSchema parsedSchema = new AvroSchemaSM(schemaString);
				String n = parsedSchema.name();
				parsedSchemas.put(n, parsedSchema);
				rawSchemas.put(n, parsedSchema.rawSchema());
				AvroSchemaSM.initParser(rawSchemas);
				List<SchemaReference> rf = parsedSchema.references();
				Map<String, String> rr = parsedSchema.resolvedReferences();
				System.out.println(n);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(String.format("failed %s with %s", r, e.getMessage()));
				e.printStackTrace();
			}
		});
		parsedSchemas.get("com.appdynamics.platform.ingestion.metrics.DistributionDto").isCompatible(
				CompatibilityLevel.FULL_TRANSITIVE, 
				Arrays.asList(parsedSchemas.get("com.appdynamics.platform.ingestion.metrics.DistributionDto").copy()));
		Map<String, Schema> schemas = AvroSchemaSM.getTypes();

		System.out.println(schemas);
	}
}
