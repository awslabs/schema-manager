package com.amazonaws.schemamanager.properties;

public class RepoClientConfig extends LocalRepoClientConfig {

	public static final String COMPATIBILITY_FIELD = "compatibility";
	
	// DO NOT GENERATE equals, hashCode and toString for this member (cyclic invocation).
	private String repoEndPoint;
	private String credentials;
	private String sshKeyFile;

	public String getRepoEndPoint() {
		return repoEndPoint;
	}
	public void setRepoEndPoint(String repoEndPoint) {
		this.repoEndPoint = repoEndPoint;
	}
	public String getCredentials() {
		return credentials;
	}
	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}
    public String getLocalBaseInfo() {
        return null;
    }

    public String getSshKeyFile() {
    	return sshKeyFile;
    }
    
    public void setSshKeyFile(String sshKeyFile) {
    	this.sshKeyFile = sshKeyFile;
    }
	
}
