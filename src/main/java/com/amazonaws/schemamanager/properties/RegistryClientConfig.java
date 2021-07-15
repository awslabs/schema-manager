package com.amazonaws.schemamanager.properties;

public class RegistryClientConfig {
	
	// DO NOT GENERATE equals, hashCode and toString for this member (cyclic invocation).
	private AppConfig appConfig;
	private String registryEndPoint;

	public String getRegistryEndPoint() {
		return registryEndPoint;
	}

	public void setRegistryEndPoint(String registryEndPoint) {
		this.registryEndPoint = registryEndPoint;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((registryEndPoint == null) ? 0 : registryEndPoint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegistryClientConfig other = (RegistryClientConfig) obj;
		if (registryEndPoint == null) {
			if (other.registryEndPoint != null)
				return false;
		} else if (!registryEndPoint.equals(other.registryEndPoint))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RegistryClientProperties [registryEndPoint=" + registryEndPoint + "]";
	}
	
	
}
