package com.amazonaws.schemamanager.repo;

import com.amazonaws.schemamanager.properties.AppConfig;
import com.amazonaws.schemamanager.properties.AppConfigHelper;

public class RepoClientFactory {
	
	public static final String LOCAL_REPO = "local";
	
	public static IRepoClient createClient() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return createClient(null);
	}

	/**
	 * Creates an instance of the Repository Client. If the client is a local client, then 
	 * the baseInfo information used is from the LocalRepo baseInfo. If not, it is the baseInfo from Repository.
	 * @param clientType
	 * @return RepoClient
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static IRepoClient createClient(String clientType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		AppConfig config = AppConfigHelper.getConfig();
		IRepoClient repoClient;
		if (clientType != null && clientType.equals(LOCAL_REPO)) {
			repoClient = new FileSystemRepoClient();
			repoClient.init(config.getLocalRepoClientConfig());
		}else {
			String repoClientClassName = config.getRepoClientClass();
			repoClient =  (IRepoClient) Class.forName(repoClientClassName).newInstance();
			repoClient.init(config.getRepoClientProperties());		
		}
		return repoClient;
	}
}
