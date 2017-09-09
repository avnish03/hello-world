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

public class XCodeBuilder extends BaseBuilder{
	private static String OSType = "ios";
	private static String BuildExt = "ipa";
	private String provisioningProfile = "";
	private String certificate = "";
	private String passphrase = "";
	private String appIconFile = "";
	private String appSplashFile = "";
	private String buildType = "";
	private String appLanguage = "";

	public XCodeBuilder(String buildId){
		super(buildId, XCodeBuilder.OSType, XCodeBuilder.BuildExt);
		loadBuildData(buildId);
	}
	
	private void loadBuildData(String buildId){
		FindIterable<Document> iterable = MongoDBHandler.find("buildIdData", new Document("_id", new ObjectId(buildId)));
				
		if (iterable.first() != null){
			Document doc = iterable.first();
			this.appId = doc.getString("appId");
			this.appName = doc.getString("appname");
			this.bundleId = doc.getString("bundleId");
			this.provisioningProfile = doc.getString("provisioningProfile");
			this.certificate = doc.getString("certificate");
			this.passphrase = doc.getString("certificatePassword");
			this.appIconFile = doc.getString("appIcon");
			this.appSplashFile = doc.getString("appSplash");
			this.buildType = doc.getString("buildType");
			this.appLanguage = doc.getString("appLanguage");

			this.loadUser();
		}
	}

	public void execute(){
		try{

			System.out.println("xcodebuild this.appName : "+this.appName);
			System.out.println("xcodebuild this.getBuildDir() : "+this.getBuildDir());
			System.out.println("xcodebuild this.provisioningProfile : "+this.provisioningProfile);
			System.out.println("xcodebuild this.certificate : "+this.certificate);
			System.out.println("xcodebuild this.passphrase : "+this.passphrase);
			System.out.println("xcodebuild this.bundleId : "+this.bundleId);
			System.out.println("xcodebuild this.appIconFile : "+this.appIconFile);
			System.out.println("xcodebuild this.appSplashFile : "+this.appSplashFile);
			System.out.println("xcodebuild this.buildId : "+this.buildId);
			System.out.println("xcodebuild this.getServiceURL() : "+this.getServiceURL());

			ArrayList<String> commands = new ArrayList<String>();
			commands.add(BuilderConfig.getInstance().getIOSBuildScript());
			commands.add(this.appName);
			commands.add(this.getBuildDir());
			commands.add(this.provisioningProfile);
			commands.add( this.certificate);
			commands.add(this.passphrase);
			commands.add(this.bundleId);
			commands.add(this.appIconFile);
			commands.add(this.appSplashFile);
			commands.add(this.buildId);
			commands.add(this.getServiceURL());

			Authorization authorization = AuthGenerator.getInstance().getAuth(this.appId, Integer.MAX_VALUE);
			commands.add(authorization.getAuthKey());
			commands.add(authorization.getAuthSecret());

			System.out.println("xcodebuild authorization.getAuthKey() : "+authorization.getAuthKey());
			System.out.println("xcodebuild authorization.getAuthSecret() : "+authorization.getAuthSecret());

			String buildVersion = String.format("1.0.%d", this.getBuildNumber());
			commands.add(buildVersion);
			commands.add(this.buildType);

            System.out.println("xcodebuild buildVersion : "+buildVersion);
            System.out.println("xcodebuild this.buildType : "+this.buildType);


            // ADD THE LANGUAGE
			commands.add(this.appLanguage);
            System.out.println("xcodebuild this.appLanguage : "+this.appLanguage);

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
