package com.ebizon.appify.builder;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.AuthGenerator;
import com.ebizon.appify.utils.Authorization;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidBuilder extends BaseBuilder{
	private static final long buildVersionBase = 10000;
	private static String OSType = "android";
	private static String BuildExt = "apk";
	private String keyStoreFileName = "";
	private String keyStorePassword = "";
	private String aliasName = "";
	private String appIconFile = "";
	private String appSplashFile = "";
	private String appLanguage = "";
    private String buildType = "";

	public AndroidBuilder(String buildId){
		super(buildId, AndroidBuilder.OSType, AndroidBuilder.BuildExt);

		loadBuildData(buildId);
	}

	private void loadBuildData(String buildId){
		FindIterable<Document> iterable = MongoDBHandler.find("buildIdData", new Document("_id", new ObjectId(buildId)));
		
		if (iterable.first() != null){
			Document doc = iterable.first();
			this.appId = doc.getString("appId");
			this.appName = doc.getString("appname");
			this.bundleId = doc.getString("bundleId");
			this.keyStoreFileName = doc.getString("keyStoreFileName");
			this.keyStorePassword = doc.getString("keyStorePassword");
			this.aliasName = doc.getString("aliasName");
			this.appIconFile = doc.getString("appIcon");
			this.appSplashFile = doc.getString("appSplash");
			this.appLanguage = doc.getString("appLanguage");
            this.buildType = doc.getString("buildType");
			this.loadUser();
		}
	}
	
	public void execute(){
		try{

			System.out.println("androidbuild this.appName : "+this.appName);
			System.out.println("androidbuild this.getBuildDir() : "+this.getBuildDir());
			System.out.println("androidbuild this.keyStoreFileName() : "+this.keyStoreFileName);
			System.out.println("androidbuild this.keyStorePassword() : "+this.keyStorePassword);
			System.out.println("androidbuild this.aliasName() : "+this.aliasName);
			System.out.println("androidbuild this.bundleId : "+this.bundleId);
			System.out.println("androidbuild this.appIconFile : "+this.appIconFile);
			System.out.println("androidbuild this.appSplashFile : "+this.appSplashFile);
			System.out.println("androidbuild this.buildId : "+this.buildId);
			System.out.println("androidbuild this.getServiceURL() : "+this.getServiceURL());

			ArrayList<String> commands = new ArrayList<String>();
			commands.add(BuilderConfig.getInstance().getAndroidBuildScript());
			commands.add(this.appName);
			commands.add(this.getBuildDir());
			commands.add(this.keyStoreFileName);
			commands.add(this.keyStorePassword);
			commands.add(this.aliasName);
			commands.add(this.bundleId);
			commands.add(this.appIconFile);
			commands.add(this.appSplashFile);
			commands.add(this.buildId);
            commands.add(this.getServiceURL());

            Authorization authorization = AuthGenerator.getInstance().getAuth(this.appId, Integer.MAX_VALUE);
            commands.add(authorization.getAuthKey());
            commands.add(authorization.getAuthSecret());

            System.out.println("androidbuild authorization.getAuthKey() : "+authorization.getAuthKey());
            System.out.println("androidbuild authorization.getAuthSecret() : "+authorization.getAuthSecret());

            long buildVersion = buildVersionBase + this.getBuildNumber();
			commands.add(Long.toString(buildVersion));
			commands.add(this.appLanguage);

            System.out.println("androidbuild buildVersion : "+buildVersion);
            System.out.println("androidbuild buildType : "+this.buildType);
            System.out.println("androidbuild this.appLanguage : "+this.appLanguage);


			ProcessBuilder pb = new ProcessBuilder(commands);
			pb = pb.inheritIO();
			Process process = pb.start();
			OutputStream stdin = process.getOutputStream();
			InputStream stdout = process.getInputStream();

			Logger logger = Logger.getLogger(this.appName);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
			logger.log(Level.INFO, "starts");
			writer.flush();

			Scanner scanner = new Scanner(stdout);
			while (scanner.hasNextLine()) {
				logger.log(Level.INFO, scanner.nextLine());
			}

			try {
				int exitValue = process.waitFor();
				logger.log(Level.INFO,"\n\nExit Value is " + exitValue);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			logger.log(Level.INFO, "finished");
		}catch(IOException io){
			System.out.println(io.getMessage());
		}
	}
}

