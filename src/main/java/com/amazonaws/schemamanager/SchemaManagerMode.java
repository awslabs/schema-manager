package com.amazonaws.schemamanager;

public enum SchemaManagerMode {
	FULL,
	PR_VALIDATION,
	BUILD_VALIDATION;
	
	public static SchemaManagerMode forName(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Unknown mode: " + name);
		}
		if (name.equalsIgnoreCase("full")) {
			return FULL;
		}
		if (name.equalsIgnoreCase("pr")) {
			return PR_VALIDATION;
		}
		if (name.equalsIgnoreCase("build")) {
			return BUILD_VALIDATION;
		}
		throw new IllegalArgumentException("Unknown mode: " + name);
	}
}
