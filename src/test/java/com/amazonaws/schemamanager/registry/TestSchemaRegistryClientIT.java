package com.amazonaws.schemamanager.registry;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.amazonaws.schemamanager.properties.AppConfigHelper;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

public class TestSchemaRegistryClientIT {
    
    @Test
    @DisplayName(value = "Ensure that initalization works for Registry Client")
    public void testCleanSr() throws Exception {
    	AppConfigHelper.initConfig("src/test/resources/deployer_config.yml");
    	IRegistryClient registryClient = RegistryClientFactory.createClient(null);
    	CachedSchemaRegistryClient sr = registryClient.getSchemaRegistryClient();
    	List<Subject> subjs = null;
    	
    	while((subjs = registryClient.getSubjects())!=null && !subjs.isEmpty()) {
	    	subjs.forEach(s ->{
	    		try {
					sr.deleteSubject(s.getName(), false);
				} catch (IOException | RestClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	});
    	}
    }
    

}
