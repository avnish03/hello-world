package com.ebizon.appify.data;

import com.ebizon.appify.builder.AndroidBuilder;
import com.ebizon.appify.builder.BaseBuilder;
import com.ebizon.appify.builder.BuildPool;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.scheduledjob.InstantDataLoader;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidAppData extends AppData{
	private String buildType;
	private String keyStoreFileName;
	private String keyStorePassword;
	private String aliasName;

	public AndroidAppData(){
		//Emptry
	}


	public void setAccountEmail(String accountEmail){
		this.accountEmail = accountEmail;
	}

	public void setOsType(String osType){
		this.osType = osType;
	}

	public void setPlatform(String platform){
		this.platform = platform;
	}

	public void setWebsite(String website){
		this.website = website;
	}

	public void setAppName(String appName){
		this.appName = appName;
	}

	public void setApiKey(String apiKey){
		this.apiKey = apiKey;
	}

	public void setApiPassword(String apiPassword){
		this.apiPassword = apiPassword;
	}

	public void setAppIcon(String appIcon){
		this.appIcon = appIcon;
	}

	public void setAppSplash(String appSplash){
		this.appSplash = appSplash;
	}

	public void setBundleId(String bundleId){
		this.bundleId = bundleId;
	}

	public void setKeyStoreFileName(String keyStoreFileName){
		this.keyStoreFileName = keyStoreFileName;
	}
	
	public void setKeyStorePassword(String keyStorePassword){
		this.keyStorePassword = keyStorePassword;
	}
	
	public void setAliasName(String aliasName){
		this.aliasName = aliasName;
	}

    public void setAppLanguage(String appLanguage) {
        this.appLanguage = appLanguage;
    }

	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}

	public void setShopifyAccessToken(String shopifyAccessToken) {
		this.shopifyAccessToken = shopifyAccessToken;
	}

	public void save(){
        System.out.println("Inside androidappdata.json save()");

        Document checkDoc = new Document()
				.append("accountemail", this.accountEmail)
				.append("ostype", this.osType);

        if(this.buildType.equalsIgnoreCase("development")) {
            HashMap<String, String> imagesPaths = new BaseBuilder(this.accountEmail).generateDemoAppImages(this.appName);
            this.appIcon = imagesPaths.get("appIcon").toString();
            this.appSplash = imagesPaths.get("appSplash").toString();
        }

		Document newDoc = new Document()
				.append("accountemail", this.accountEmail)
				.append("ostype", this.osType)
				.append("platform", this.platform)
				.append("website", this.website)
				.append("appname", this.appName)
				.append("apikey", this.apiKey)
				.append("apipassword", this.apiPassword)
				.append("shopifyAccessToken", this.shopifyAccessToken)
			    .append("appIcon", this.appIcon)
				.append("appSplash", this.appSplash)
				.append("bundleId", this.bundleId)
				.append("keyStoreFileName", this.keyStoreFileName)
				.append("keyStorePassword", this.keyStorePassword)
				.append("aliasName", this.aliasName)
				.append("buildType", this.buildType)
				.append("appLanguage", this.appLanguage);

		Logger logger = Logger.getLogger(this.bundleId);
		logger.log(Level.INFO, String.format("Android build data ; %s",newDoc.toJson()));

		UpdateResult result = MongoDBHandler.updateOne("appData", checkDoc, newDoc);
		
		if (result.getMatchedCount() == 0){
			 MongoDBHandler.insertOne("appData", newDoc);
		}
	}
	
	public String create(){
		String message = "Successfully created app";

		String appId = this.getAppId();
		
		String buildId = createBuildId(appId);
		
		AndroidBuilder androidBuilder = new AndroidBuilder(buildId);
		BuildPool.getInstance().addToQueue(androidBuilder);
		
		InstantDataLoader.getInstance().addToQueue(appId);
		 
		return "{\"This is get\" : \" "+ message +" \"}";
	}
	
	private String getAppId(){
		String appID = "";
		Document filterDoc = new Document("accountemail", this.accountEmail)
				.append("ostype", this.osType);
				
		FindIterable<Document> iterable = MongoDBHandler.find("appData", filterDoc);
				
		if (iterable.first() != null){
			Document doc = iterable.first();
			appID = doc.getObjectId("_id").toString();
		}
		
		Logger logger = Logger.getLogger(this.bundleId);
		logger.log(Level.INFO, String.format("App ID for this is %s", appID));
		
		return appID;
	}
	
	private String createBuildId(String appID){
        String buildId = "";

        String tempBundleId = (this.bundleId.equalsIgnoreCase("default"))?BaseBuilder.DefaultBundleId:this.bundleId;
        String tempKeyStoreFileName = (this.keyStoreFileName.equalsIgnoreCase("default"))?BaseBuilder.DefaultAndroidKeystore:this.keyStoreFileName;
        String tempKeyStorePassword = (this.keyStorePassword.equalsIgnoreCase("default"))?BaseBuilder.DefaultAndroidPassphrase:this.keyStorePassword;
        String tempAliasName = (this.aliasName.equalsIgnoreCase("default"))?BaseBuilder.DefaultAndroidAliasName:aliasName;


		if (!appID.isEmpty()){
			Document buildDoc = new Document("appId", appID)
			.append("appname", this.appName)
		    .append("appIcon", this.appIcon)
			.append("appSplash", this.appSplash)
			.append("bundleId", tempBundleId)
			.append("keyStoreFileName", tempKeyStoreFileName)
			.append("keyStorePassword", tempKeyStorePassword)
			.append("aliasName", tempAliasName)
			.append("buildType", this.buildType)
			.append("appLanguage", this.appLanguage);

			Logger logger = Logger.getLogger(this.bundleId);
			logger.log(Level.INFO, String.format("AndroidAppData:createBuildId() ; %s",buildDoc.toJson()));

			MongoDBHandler.insertOne("buildIdData", buildDoc);
			
			buildId = buildDoc.getObjectId("_id").toString();
		}
		
		return buildId;
	}

}
