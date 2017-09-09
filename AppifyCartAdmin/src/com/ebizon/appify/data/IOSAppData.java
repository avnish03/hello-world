package com.ebizon.appify.data;

import com.ebizon.appify.builder.BaseBuilder;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.restapi.MainController;
import com.ebizon.appify.scheduledjob.InstantDataLoader;
import com.ebizon.appify.utils.TransferFilesToIOSServer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONObject;

import java.util.HashMap;

public class IOSAppData extends AppData{
	private String buildType;
	private String certificate;
	private String certificatePassword;
	private String provisioningProfile;
	
	public IOSAppData(){
		//Empty
	}

    public String getBuildType() {
        return buildType;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public String getProvisioningProfile() {
        return provisioningProfile;
    }


    public void setBuildType(String buildType){
		this.buildType = buildType;
	}
	
	public void setCertificate(String certificate){
		this.certificate = certificate;
	}
	
	public void setCertificatePassword(String certificatePassword){
		this.certificatePassword = certificatePassword;
	}
	
	public void setProvisioningProfile(String provisioningProfile){
		this.provisioningProfile = provisioningProfile;
	}
	
	public void setAppLanguage(String appLanguage) {
		this.appLanguage = appLanguage;
	}

	public void setShopifyAccessToken(String shopifyAccessToken) {
		this.shopifyAccessToken = shopifyAccessToken;
	}

	public void save(){
		Document checkDoc = new Document("accountemail", this.accountEmail)
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
				.append("buildType", this.buildType)
				.append("certificate", this.certificate)
				.append("certificatePassword", this.certificatePassword)
				.append("provisioningProfile", this.provisioningProfile)
				.append("appLanguage", this.appLanguage);

		UpdateResult result = MongoDBHandler.updateOne("appData", checkDoc, newDoc);
		
		if (result.getMatchedCount() == 0){
			MongoDBHandler.insertOne("appData", newDoc);
		}

		if(this.buildType.equalsIgnoreCase("development")){
            this.transferDemoBuildFiles(this.appIcon);
            this.transferDemoBuildFiles(this.appSplash);
        }
	}

    private void transferDemoBuildFiles(String file){
        int temp = file.lastIndexOf("file_");
        String path = file.substring(0, temp);
        String fileName = file.substring(temp, file.length());
        // execute to transfer files from one server to another
        System.out.println("PATHHHHHHHH:"+path);
        System.out.println("FILEEEEEEEE:"+fileName);
        boolean isTransfer = TransferFilesToIOSServer.copyFiles(path,fileName);
        System.out.println("result : "+isTransfer);
    }
	private String createBuildId(String appID){
		String buildId = "";

        this.bundleId = (this.bundleId.equalsIgnoreCase("default"))?BaseBuilder.DefaultBundleId:this.bundleId;
        this.certificatePassword = (this.certificatePassword.equalsIgnoreCase("default"))?BaseBuilder.DefaultIOSPassphrase:this.certificatePassword;
        this.certificate = (this.certificate.equalsIgnoreCase("default"))?BaseBuilder.DefaultIOSCertificate:this.certificate;
        this.provisioningProfile = (this.provisioningProfile.equalsIgnoreCase("default"))?BaseBuilder.DefaultIOSProvisonalProfile:this.provisioningProfile;

        if (!appID.isEmpty()){
			Document buildDoc = new Document("appId", appID)
					.append("appname", this.appName)
					.append("appIcon", this.appIcon)
					.append("appSplash", this.appSplash)
					.append("bundleId", this.bundleId)
					.append("buildType", this.buildType)
					.append("certificate", this.certificate)
					.append("certificatePassword", this.certificatePassword)
					.append("provisioningProfile", this.provisioningProfile)
					.append("appLanguage", this.appLanguage);

			MongoDBHandler.insertOne("buildIdData", buildDoc);

			buildId = buildDoc.getObjectId("_id").toString();
		}

		return buildId;
	}


	public JSONObject create(){
        String appId = getAppId();

        String buildId = createBuildId(appId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId",appId);
        jsonObject.put("buildId",buildId);
		InstantDataLoader.getInstance().addToQueue(appId);
		return jsonObject;
//		String message = "Successfully created app";
//
//		XCodeBuilder xcodeBuilder = new XCodeBuilder(buildId);
//		BuildPool.getInstance().addToQueue(xcodeBuilder);
//
//        InstantDataLoader.getInstance().addToQueue(appId);
//
//		return "{\"This is get\" : \" "+ message +" \"}";
	}
	
	private String getAppId(){
		return IOSAppData.getAppId(this.accountEmail, this.osType);
	}
	
	public static String getAppId(String accountEmail, String osType){
		String appID = "";
		
		Document filterDoc = new Document("accountemail", accountEmail)
				.append("ostype", osType);
		FindIterable<Document> iterable = MongoDBHandler.find("appData", filterDoc);
				
		if (iterable.first() != null){
			Document doc = iterable.first();
			appID = doc.getObjectId("_id").toString();
		}
				
		return appID;
	}

	public String serviceUrl(){
		String baseServiceURL = MainController.getBaseURLAuthority()+"/%s/rest/%s/api.json"; //1st arg = War name of storetype, 2nd arg = app id
		String storeConnector = "ShopifyConnect"; //Add cases for others, only Shopify supported now
		String serviceUrl = String.format(baseServiceURL, storeConnector, this.getAppId());
		System.out.println("service url --------------->"+serviceUrl);
		return serviceUrl;
	}

    public long getBuildNumber(){
        Document document = new Document("appId", this.getAppId())
                .append("bundleId", this.bundleId);
        long count = MongoDBHandler.count("buildIdData", document);

        return count + 1;
    }
	public static String getBundleId(String accountEmail, String osType){
		String bundleID = "";
		
		Document filterDoc = new Document("accountemail", accountEmail)
				.append("ostype", osType);
		FindIterable<Document> iterable = MongoDBHandler.find("appData", filterDoc);
				
		if (iterable.first() != null){
			Document doc = iterable.first();
			bundleID = doc.getString("bundleId");
		}
				
		return bundleID;
	}
}
