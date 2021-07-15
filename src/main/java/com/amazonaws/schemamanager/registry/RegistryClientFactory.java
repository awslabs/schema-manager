package com.amazonaws.schemamanager.registry;

import com.amazonaws.schemamanager.properties.AppConfig;
import com.amazonaws.schemamanager.properties.AppConfigHelper;

public class RegistryClientFactory {

    public IRegistryClient createClient() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return createClient(null);
    }

    /**
     * Creates an Instance of a Registry Client. If registryClientType is null, then the configuration Registry Client type will be used.
     * @param registryClientType
     * @return RegistryClient
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static IRegistryClient createClient(String registryClientType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        IRegistryClient registryClient;
        AppConfig config = AppConfigHelper.getConfig();
        String registryClientClassName;
        if(registryClientType != null){
            registryClientClassName = registryClientType;
        } else {
            registryClientClassName = config.getRegistryClientClass();
        }
        registryClient = (IRegistryClient) Class.forName(registryClientClassName).newInstance();
        registryClient.init(config.getRegistryClientProperties());
        return registryClient;
    }
    
}