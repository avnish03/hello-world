package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ShopData {

	/* shop Website url */
	private String websiteURL = "";
	/*Your shops private app api key*/
	private String apikey = "";
	/*Your shops private app api password*/
	private String apipassword = "";

	private String accountEmail = "";

	private String accessToken = "";

	private boolean isAccessTokenRequired ;

    public ShopData(String appID){
        this.loadShopData(appID);
        this.setAccessTokenRequired();
    }

    public ShopData(String websiteURL, String accessToken, boolean isAccessTokenRequired) {
        this.websiteURL = websiteURL;
        this.accessToken = accessToken;
        this.isAccessTokenRequired = isAccessTokenRequired;
    }

    public String getWebsiteURL() {
        if(websiteURL.endsWith("/"))
			return websiteURL.substring(0,websiteURL.length()-1);
		return websiteURL;
	}

	public String getApiKey() {
		return apikey;
	}

	public String getPassword() {
		return apipassword;
	}

	public String getAccountEmail() {
		return accountEmail;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public boolean isAccessTokenRequired() {
        return this.isAccessTokenRequired;
	}

	public void setAccessTokenRequired() {
//        String accessToken = this.getAccessToken();
//        if( accessToken == null || accessToken.equalsIgnoreCase("NA"))
//		    this.isAccessTokenRequired = false;
//        else
//            this.isAccessTokenRequired = true;
		this.isAccessTokenRequired= this.ifShopifySignUpMethod(getAccountEmail());
		this.accessToken = this.getAccessToken(getAccountEmail());
	}

	private void loadShopData(final String appID){
		FindIterable<Document> iterable=null;
		Logger logger = Logger.getLogger(appID);
		try {
			 iterable = MongoDBHandler.find("appData", new Document("_id", new ObjectId(appID)));
			 logger.log(Level.INFO, String.format("iterable for app %s is %s", appID, Boolean.toString(iterable == null)));
			 logger.log(Level.INFO, String.format("iterable.first for app %s is %s", appID, Boolean.toString(iterable.first() == null)));

			if (iterable.first() != null){
				Document doc = iterable.first();
				this.websiteURL = doc.getString("website");
				this.apikey = doc.getString("apikey");
				this.apipassword = doc.getString("apipassword");
				this.accountEmail = doc.getString("accountemail");
//				this.accessToken = doc.getString("shopifyAccessToken");
			}
		}catch (IllegalArgumentException | NullPointerException ex){
			ex.printStackTrace();
		}

		logger.log(Level.INFO, String.format("URL for app %s is %s", appID, this.websiteURL));
		logger.log(Level.INFO, String.format("Key for app %s is %s", appID, this.apikey));
		logger.log(Level.INFO, String.format("Password for app %s is %s", appID, this.apipassword));
	}

	public static JSONObject fetchAccessToken(String user) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("shopifyAccessToken", "NA");
        jsonObj.put("status", 0);

        FindIterable<Document> iterable = MongoDBHandler.find("shopifyAPPTokens", new Document("email", user));
        // .append("shop",shop));  // check on shop will be needed for multistore on a single mail id

        Document document = iterable.first();
        if (document != null) {
            String accessToken = document.getString("accessToken");
            jsonObj.put("status", 1);
            jsonObj.put("shopifyAccessToken", accessToken);
        }
        return jsonObj;
	}

    private String getAccessToken(String user) {
        String accessToken = "NA";
        FindIterable<Document> iterable = MongoDBHandler.find("shopifyAPPTokens", new Document("email", user));
        Document document = iterable.first();
        if (document != null) {
            accessToken = document.getString("accessToken");
        }
        return accessToken;
    }

    private boolean ifShopifySignUpMethod(String user) {
		boolean response = false;
		FindIterable<Document> iterable = MongoDBHandler.find("users", new Document("email", user));
		try{
			Document document = iterable.first();
			if (document != null) {
				String temp = document.getString("signupmethod");
				response = temp.equalsIgnoreCase("shopifyapp")?true:false;

			}
		}catch (IllegalArgumentException | NullPointerException ex){
			//ex.printStackTrace();
		}
		return response;
	}


}
