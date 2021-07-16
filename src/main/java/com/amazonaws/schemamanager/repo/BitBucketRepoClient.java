package com.amazonaws.schemamanager.repo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import com.amazonaws.schemamanager.properties.IRepoClientConfig;
import com.amazonaws.schemamanager.properties.RepoClientConfig;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class BitBucketRepoClient extends FileSystemRepoClient {
	
	private static final String DEFAULT_BASE_DIR = System.getProperty("java.io.tmpdir");

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BitBucketRepoClient.class);
	private RepoClientConfig config;
	private File localDir;
	private Repository repository;
	
	
	@Override
	public void init(IRepoClientConfig config) {
		this.config = (RepoClientConfig)config;
		String url = this.config.getRepoEndPoint();
		String branch = this.config.getBaseInfo();
		localDir = prepareLocalDir(this.config.getLocalBaseInfo(), this.config.getAppConfig().getApplicationName()); // this is a local dir on file system to clone the repo into
		
		init(url, branch);
		
	}
	
	public void init(String url, String branch) {
		
		final String passphrase = config.getCredentials();
		final String sshFileLoc = config.getSshKeyFile();
		
		SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure( Host host, Session session ) {
				session.setConfig("StrictHostKeyChecking", "no");
			}
			@Override
			protected JSch createDefaultJSch( FS fs ) throws JSchException {
			  JSch defaultJSch = super.createDefaultJSch( fs );
			  if (sshFileLoc != null && !sshFileLoc.isEmpty()) {
				  defaultJSch.addIdentity(sshFileLoc, passphrase);
				  log.info("Using ssh key from: " + sshFileLoc);
			  }
			  return defaultJSch;
			}
		};

		Git git = null;
		try {
			git = Git.cloneRepository()
			.setDirectory(localDir)
			.setURI(url)
			.setBranchesToClone(Arrays.asList(branch))
			.setBranch(branch)
			.setCloneAllBranches(true)
			.setTransportConfigCallback( new TransportConfigCallback() {
				@Override
				public void configure( Transport transport ) {
					SshTransport sshTransport = ( SshTransport )transport;
					sshTransport.setSshSessionFactory( sshSessionFactory );
				}
			})
			.call();
		} catch (Exception e) {
			log.error("Failed to initialize GitRepoClient... ", e);
			return;
		}

		this.repository = git.getRepository();
	}

	private File prepareLocalDir(String localBaseInfo, String appName) {
		if (localBaseInfo == null) {
			localBaseInfo = DEFAULT_BASE_DIR;
		}
		File fullDirPath = null;
		do{
			String dirName = appName + "-" + RandomStringUtils.randomAlphanumeric(13);
			fullDirPath = Paths.get(localBaseInfo, dirName).toFile();
		}while(fullDirPath.exists());
		
		try {
			fullDirPath.mkdirs();
		}catch(Exception e) {
			log.error("Couldn't creat directories for repository, going to break now...", e);
		}
		
		return fullDirPath;
	}

	@Override
	public List<RepoSchema> getSchemaList() throws IOException{
		return super.getSchemaList(Paths.get(localDir.getPath(), ObjectUtils.firstNonNull(config.getPathPrefix(), "")).toString());
	}

	@Override
	public RepoSchema getSchema(String schemaName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepoSchema getSchemaUpdates(String schemaName) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		try {
			//testGit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
