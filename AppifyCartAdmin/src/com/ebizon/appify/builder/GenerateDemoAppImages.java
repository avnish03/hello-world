package com.ebizon.appify.builder;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class GenerateDemoAppImages {

	private String accountEmail ;
	public GenerateDemoAppImages(String accountEmail) {
		this.accountEmail = accountEmail;
	}

	public HashMap<String, String> generate(String appName) {

//	    String organisation = this.getOrganisation();
//	    int organisationStrLen = organisation.length();

		String organisation = appName;
		int organisationStrLen = organisation.length();

		organisation = (organisationStrLen > 15 )?organisation.substring(0,14):organisation ;
	    if(organisationStrLen <= 0 )
	    	organisation = "Demo APP" ;

		organisationStrLen = organisation.length();

	    HashMap<Integer, Integer> hmSplashPointSize = new HashMap<>();
	    	hmSplashPointSize.put(1, 500);
	    	hmSplashPointSize.put(2 , 400);
	    	hmSplashPointSize.put(3 , 400);
	    	hmSplashPointSize.put(4 , 300);
	    	hmSplashPointSize.put(5 , 250);
	    	hmSplashPointSize.put(6 , 200);
	    	hmSplashPointSize.put(7 , 180);
	    	hmSplashPointSize.put(8 , 160);
	    	hmSplashPointSize.put(9 , 140);
	    	hmSplashPointSize.put(10 , 120);
	    	hmSplashPointSize.put(11 , 120);
	    	hmSplashPointSize.put(12 , 120);
	    	hmSplashPointSize.put(13 , 120);
	    	hmSplashPointSize.put(14 , 120);
	    	hmSplashPointSize.put(15 , 120);


        String splashPointSize = hmSplashPointSize.get(organisationStrLen).toString();

		int iconPointSizeTemp = hmSplashPointSize.get(organisationStrLen) / 10 ;
		String iconPointSize = Integer.toString(iconPointSizeTemp);

		java.nio.file.Path path = getFilePath();

		String appIconfile =  "file_Icon_" + randomKey()  ;
		String appIconfilePath = path + "/" + appIconfile + ".png" ;
		String appSplashfile = "file_Splash_" + randomKey() ;
		String appSplashfilePath = path + "/" + appSplashfile + ".png" ;

		ArrayList<String> commands = new ArrayList<String>();
		commands.add(BuilderConfig.getInstance().getGenerateAppIconSplash());

		// command 1 - image directory
		commands.add(path.toString());

		commands.add(appIconfile);

		commands.add(iconPointSize);

		commands.add(appSplashfile);

		commands.add(splashPointSize);

		commands.add(organisation);


		ProcessBuilder pb = new ProcessBuilder(commands);
		pb = pb.inheritIO();
		Process process = null;

		try {
			process = pb.start();
			int exitValue = process.waitFor();
			System.out.println("Generated Demo Images for App with exitvalue "+exitValue);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        HashMap<String,String> imagesPath = new HashMap<>();
		imagesPath.put("appIcon",appIconfilePath);
		imagesPath.put("appSplash",appSplashfilePath);

		return imagesPath;
	}

    private String randomKey() {
        SecureRandom random = new SecureRandom();
        BigInteger id1 = new BigInteger(130, random);
        BigInteger id2 = BigInteger.valueOf(Instant.now().toEpochMilli());
        BigInteger id = id1.add(id2);

        return id.toString(32);
    }


	private java.nio.file.Path getFilePath(){
		StringBuilder stringBuilder = new StringBuilder(BuilderConfig.getInstance().getBaseDir());
		stringBuilder.append("/");
		stringBuilder.append("uploadFiles");
		stringBuilder.append("/");
		stringBuilder.append(this.accountEmail);

		java.nio.file.Path path = Paths.get(stringBuilder.toString());
		try
		{
			Files.createDirectories(path);
		}catch(IOException e){
			e.printStackTrace();
		}
		return path;
	}


	private String getOrganisation(){
		String fullname = "";
		FindIterable<Document> appIterable = MongoDBHandler.find("users", new Document("email", this.accountEmail));

		if (appIterable.first() != null){
			Document document = appIterable.first();
			fullname = document.getString("fullname");
		}
		return fullname;
	}


}
