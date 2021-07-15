package com.amazonaws.schemamanager.utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.kafka.common.cache.Cache;

import com.amazonaws.schemamanager.repo.RepoUtils;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchemaMetadata;
import com.squareup.wire.schema.Location;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.ProtoParser;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtobufParser {
	
	private Map<String, ProtoFileElement> dependencies = new HashMap<>();

	
	public ProtobufParser() {
	}
	
	/**
	 * Parses string into Protobuf schema at given location.
	 * @param path
	 * @param schemaStr
	 * @return protobuf schema
	 */
	public RepoSchema parseProtoFile(String path, String schemaStr, RepoSchema repoSchema) throws Exception{
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Location cannot be empty: " + path);
		}
		
		if (repoSchema == null) {
			repoSchema = new RepoSchema();
			repoSchema.setPath(path);
		}
		
		ProtoFileElement pfe = ProtoParser.Companion.parse(Location.get(path), schemaStr);
		
		List<SchemaReference> refs = new LinkedList<>();
//		Map<String, ProtoFileElement> localDependencies = new LinkedHashMap<>();
		for (String imprt : pfe.getImports()) {
			ProtoFileElement dependency = dependencies.get(imprt);
			if (dependency == null) {
				throw new Exception("Reference is not resolved yet " + imprt);
			}
//			localDependencies.put(imprt, dependency);
			refs.add(new SchemaReference(imprt, imprt, -1));
		}
		
		ProtobufSchema parsedSchema = new ProtobufSchema(pfe, new LinkedList<>(refs), new LinkedHashMap<>(dependencies));
		parsedSchema.validate();
		repoSchema.setSchema(parsedSchema);
		
		RepoSchemaMetadata repoSchemaMetadata = new RepoSchemaMetadata();
		repoSchema.setMetadata(repoSchemaMetadata);
		
		
//		if(repoSchemaMetadata.getSchemaName() == null){
//			repoSchemaMetadata.setSchemaName(RepoUtils.getFullName(parsedSchema));
//		}
//		
		String pkgName = pfe.getPackageName();
		String refName = pkgName.replaceAll("\\.", "/") + "/" + Paths.get(path).getFileName();
		
		dependencies.put(refName, pfe);
		
		repoSchemaMetadata.setSchemaName(refName);

		return repoSchema;
	}

	
	/**
	 * Parses string into Protobuf schema at given location.
	 * @param path
	 * @param schemaStr
	 * @return protobuf schema
	 */
	public ProtobufSchema parse(String path, String schemaStr){
		return parse(Location.get(path), schemaStr);
	}

	public ProtobufSchema parse(Location location, String schemaStr) {
		
		String locStr = null;
		if (location == null || (locStr = location.getPath()) == null || locStr.isEmpty()) {
			throw new IllegalArgumentException("Location cannot be empty: " + location);
		}
		
		ProtoFileElement pfe = ProtoParser.Companion.parse(location, schemaStr);
		
		List<SchemaReference> refs = new LinkedList<>();
		
		pfe.getImports().forEach(imprt -> refs.add(new SchemaReference(imprt, imprt, 1)));
		ProtobufSchema parsedSchema = new ProtobufSchema(pfe, new LinkedList<>(refs), dependencies);
		
		// run validate to make sure the schema has all dependencies resolved.
		parsedSchema.validate();
		// if validate hasn't failed, we can add the dependency:
		String pkgName = pfe.getPackageName();
		String refName = pkgName.replaceAll("\\.", "/") + "/" + Paths.get(locStr).getFileName();

		dependencies.put(refName, pfe);
		return parsedSchema;
	}
	
//	public static ParsedSchema lookupReferenceVersion(SchemaRegistryClient schemaRegistry, String subject,
//			ParsedSchema schema, Cache<SubjectSchema, ParsedSchema> cache, boolean latestCompatStrict)
//			throws IOException, RestClientException {
//		SubjectSchema ss = new SubjectSchema(subject, schema);
//		ParsedSchema latestVersion = null;
//		if (cache != null) {
//			latestVersion = cache.get(ss);
//		}
//		if (latestVersion == null) {
//			SchemaMetadata schemaMetadata = schemaRegistry.getLatestSchemaMetadata(subject);
//			Optional<ParsedSchema> optSchema = schemaRegistry.parseSchema(
//					schemaMetadata.getSchemaType(),
//					schemaMetadata.getSchema(), 
//					schemaMetadata.getReferences());
//			latestVersion = optSchema.orElseThrow(() -> 
//					new IOException("Invalid schema " + schemaMetadata.getSchema()
//						+ " with refs " + schemaMetadata.getReferences() + " of type " + schemaMetadata.getSchemaType()));
//			// Sanity check by testing latest is backward compatibility with schema
//			// Don't test for forward compatibility so unions can be handled properly
//			if (latestCompatStrict && !latestVersion.isBackwardCompatible(schema).isEmpty()) {
//				throw new IOException("Incompatible schema " + schemaMetadata.getSchema() + " with refs "
//						+ schemaMetadata.getReferences() + " of type " + schemaMetadata.getSchemaType() + " for schema "
//						+ schema.canonicalString());
//			}
//			if (cache != null) {
//				cache.put(ss, latestVersion);
//			}
//		}
//		return latestVersion;
//	}
//
//	protected static class SubjectSchema {
//		private String subject;
//		private ParsedSchema schema;
//
//		public SubjectSchema(String subject, ParsedSchema schema) {
//			this.subject = subject;
//			this.schema = schema;
//		}
//
//		public String getSubject() {
//			return subject;
//		}
//
//		public ParsedSchema getSchema() {
//			return schema;
//		}
//
//		@Override
//		public boolean equals(Object o) {
//			if (this == o) {
//				return true;
//			}
//			if (o == null || getClass() != o.getClass()) {
//				return false;
//			}
//			SubjectSchema that = (SubjectSchema) o;
//			return subject.equals(that.subject) && schema.equals(that.schema);
//		}
//
//		@Override
//		public int hashCode() {
//			return Objects.hash(subject, schema);
//		}
//	}

//	protected ProtoFileElement parseElement(String path, String schemaStr) {
//		if (path == null || path.isEmpty()) {
//			throw new IllegalArgumentException("Location cannot be empty: " + path);
//		}
//		ProtoFileElement pfe = ProtoParser.Companion.parse(location, schemaStr);
//		
//		List<SchemaReference> refs = new LinkedList<>();
//		pfe.getImports().forEach(imprt -> refs.add(new SchemaReference(imprt, imprt, 1)));
//
//		return null;
//	}
}
