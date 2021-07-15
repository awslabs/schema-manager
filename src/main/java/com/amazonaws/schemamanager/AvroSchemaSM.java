package com.amazonaws.schemamanager;

import java.util.Map;
import java.util.Objects;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;

/**
 * Represents an Avro Schema that also holds known references in a Map. 
 */
public class AvroSchemaSM extends AvroSchema {

	private static Schema.Parser parser;

	public AvroSchemaSM(String schemaObj) {
		super(schemaObj);
	}

	@Override
	protected Parser getParser() {
		return getStaticParser();
	}

	private static  Parser getStaticParser() {
		if (parser != null)
			return parser;

		parser = new Schema.Parser();
		parser.setValidateDefaults(true);
		parser.setValidate(true);
		return parser;
	}
	
	public static void initParser(Map<String, Schema> knownTypes) {
		parser = new Schema.Parser();
		parser.addTypes(knownTypes);
		parser.setValidateDefaults(true);
	}
	
	public static Map<String, Schema> getTypes() {
		return parser.getTypes();
	}

	@Override
	public String name() {
		String name = super.name();
		if (name == null && this.rawSchema() != null) {
			name = this.rawSchema().getFullName();
		}
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof AvroSchema)) {
			return false;
		}
//		if (o == null || getClass() != o.getClass()) {
//			return false;
//		}
		AvroSchema that = (AvroSchema) o;
		// Can't use schemaObj as it doesn't compare field doc, aliases, etc.
		return Objects.equals(
				canonicalString(), that.canonicalString()) 
				&& Objects.equals(references(), that.references())
				&& Objects.equals(version(), that.version());
	}

}
