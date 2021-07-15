package com.amazonaws.schemamanager;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TestSchemaManagerMain {

	@Test
	void testHelp() throws Exception {
		
		SchemaManagerMain.main("--help".split(" "));
	}

	@Test
	void testConfigFileNotProvided() throws Exception {
		Exception e = assertThrows(RuntimeException.class, () ->{
			SchemaManagerMain.main("".split(" "));
		});
	}

	@Test
	void testPrValidation() throws Exception {
		SchemaManagerMain.main("-m pr -c src/test/resources/pr_validation_config.yml".split(" "));
	}

	@Test
	void testTaskReadOnly() throws Exception {
		SchemaManagerMain.main("-m full -c src/test/resources/reporter_config.yml".split(" "));
	}

	@Test
	void testTaskDeployer() throws Exception {
		SchemaManagerMain.main("-m full -c src/test/resources/deployer_config.yml".split(" "));
	}
}
