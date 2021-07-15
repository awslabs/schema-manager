package com.amazonaws.schemamanager;

public class SchemaManagerRunnerFactory {
	public static ISchemaManagerRunner getRunner(SchemaManagerMode mode) {
		switch (mode) {
		case FULL:{
			return new SchemaManagerTask();
		}
		case PR_VALIDATION:{
			return new SchemaManagerPrValidator();
		}
		case BUILD_VALIDATION:{
			return new SchemaManagerBuildValidator();
		}
		default:{
		}
		}
		return null;
	}
}
