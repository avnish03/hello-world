package com.ebizon.appify.data;

import com.ebizon.appify.builder.BaseBuilder;
import com.ebizon.appify.builder.BuildPool;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Register implements Callable<Void> {
	private static final String emailTemplate = "webapps/AppifyCartAdmin/emailTemplate/welcome.html";
	private static final String newUseremailTemplate = "webapps/AppifyCartAdmin/emailTemplate/newUserWelcome.html";
	private static final String reInstallEmailTemplate = "webapps/AppifyCartAdmin/emailTemplate/reInstalledWelcome.html";
	private static final int PasswordSize = 8;
    private String fullname;
    private String email;
    private String password;
    private String timezoneName;
    private String signupmethod;
    private boolean sendPasswordAsEmail = false;
    private boolean isReinstalled = false;

	public Register(){
	}

	public Register(String fullname, String email, String timezoneName, String signupmethod) {
		this.fullname = fullname;
		this.email = email;
		this.timezoneName = timezoneName;
		this.signupmethod = signupmethod;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setTimezoneName(String timezoneName) {
		this.timezoneName = timezoneName;
	}

	public void setSignupmethod(String signupmethod) {
		this.signupmethod = signupmethod;
	}

	public JSONObject save(){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 0);
		jsonObj.put("message", "Registration failed");

		if(this.email != null){
			boolean isExist = MongoDBHandler.isExist("users", new Document("email", this.email));
			if (!isExist){
				if (this.password == null){
					RandomString randomString = new RandomString(Register.PasswordSize);
					this.password = randomString.nextString();

					sendPasswordAsEmail = true;
				}

				String encrptedPassword = SecurityUtils.encryptWithMD5(this.password);
				Document document = new Document("email", this.email)
						.append("fullname", this.fullname)
						.append("password", encrptedPassword)
						.append("signupmethod", this.signupmethod)
						.append("timezoneName", this.timezoneName);
				MongoDBHandler.insertOne("users", document);

				jsonObj.put("status", 1);
				jsonObj.put("message", "Registration success");

				Authorization authorization = AuthGenerator.getInstance().getAuth(this.email);
				jsonObj.put("accesstoken", authorization.getToken());
				jsonObj.put("signupmethod", this.signupmethod);

                BuildPool.getInstance().addToQueue(this);
			}
		}
		System.out.println("Registering a new user response: "+jsonObj);
		return jsonObj;
	}

	public JSONObject registerExistingShopifyUser(){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("status", 0);
			jsonObj.put("message", "Reset Failed");

		System.out.println("email : "+this.email);
		Document checkDoc = new Document("email", this.email);
			if(this.email != null){
				boolean isExist = MongoDBHandler.isExist("users", checkDoc);
				if (isExist){

                    RandomString randomString = new RandomString(Register.PasswordSize);
                    this.password = randomString.nextString();

                    sendPasswordAsEmail = true;

                    String encrptedPassword = SecurityUtils.encryptWithMD5(this.password);
                    Document newDocument = new Document("email", this.email)
                                .append("fullname", this.fullname)
                                .append("password", encrptedPassword)
                                .append("signupmethod", "shopifyapp")
                                .append("timezoneName", this.timezoneName);
                    UpdateResult result = MongoDBHandler.updateOne("users", checkDoc, newDocument);

                    if(result.getMatchedCount() == 0){
                        jsonObj.put("status", -1);
                        jsonObj.put("message", "Reset Failed : User not Exist");
                    }else{
                    	this.isReinstalled = true;
                        jsonObj.put("status", 1);
                        jsonObj.put("message", "Reset success");
                    }

                    BuildPool.getInstance().addToQueue(this);
				}
			}
        return jsonObj;
		}

	public Void call() {
		try{
			if(sendPasswordAsEmail){
				this.sendPasswordByEmail();
				this.sendRegisterNotifToDevs();
			}

			this.registerToDrip();

			this.registerToCampaign();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}

		return null;
	}

	private void sendPasswordByEmail() {
		String subject = "Welcome to AppifyCart";

		Map<String, String> input = new HashMap<>();
		input.put("t_name", this.fullname);
		input.put("t_user", this.email);
		input.put("t_password", this.password);

		String emailToSend = (isReinstalled)?Register.reInstallEmailTemplate:Register.emailTemplate;
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File emailTempl = new File( catalinaBase, emailToSend);

		String filePath = emailTempl.getAbsolutePath();
		String htmlText = EmailUtils.readEmailFromHtml(filePath, input);

		EmailUtils.sendEmail(email, subject, htmlText);
	}

    private void sendRegisterNotifToDevs() {
        String subject = "New User Registered to AppifyCart";

        Map<String, String> input = new HashMap<>();
        input.put("t_name", this.fullname);
        input.put("t_user", this.email);

        File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
        File emailTempl = new File( catalinaBase, Register.newUseremailTemplate);

        String filePath = emailTempl.getAbsolutePath();
        String htmlText = EmailUtils.readEmailFromHtml(filePath, input);

        EmailUtils.sendEmail(BaseBuilder.notifEmails, subject, htmlText);
    }

    private void registerToDrip(){
		try{
			JSONObject subscriber = new JSONObject();
			subscriber.put("email", this.email);
			subscriber.put("time_zone", this.timezoneName);

			JSONObject customFields = new JSONObject();
			customFields.put("your_organisation", this.fullname);

			subscriber.put("custom_fields", customFields);

			JSONArray jsonArray = new JSONArray();
			jsonArray.put(subscriber);

			JSONObject postObj = new JSONObject();
			postObj.put("subscribers", jsonArray);

			String url = "https://api.getdrip.com/v2/1442911/subscribers";

			String response = postToURL(postObj, url);
			System.out.println(response);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void registerToCampaign(){
		try{
			JSONObject subscriber = new JSONObject();
			subscriber.put("email", this.email);
			subscriber.put("time_zone", this.timezoneName);

			JSONObject customFields = new JSONObject();
			customFields.put("your_organisation", this.fullname);

			subscriber.put("custom_fields", customFields);

			JSONArray jsonArray = new JSONArray();
			jsonArray.put(subscriber);

			JSONObject postObj = new JSONObject();
			postObj.put("subscribers", jsonArray);

			String url = "https://api.getdrip.com/v2/1442911/campaigns/60191675/subscribers";

			String response = postToURL(postObj, url);
			
			System.out.println(response);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private String postToURL(JSONObject postObj, String url){
		StringBuilder response = new StringBuilder();

		try{
			URL obj = new URL(url);
			HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
			conn.setUseCaches(true);
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/vnd.api+json");
			conn.setRequestProperty("Authorization", "Bearer 23a9112d725d59c532a53fdd123cb0ff925239e2b94464da968dca1a959f4227");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			// Send post request
			OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
			wr.write(postObj.toString());
			wr.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;


			while ((inputLine = in.readLine()) != null){
				response.append(inputLine);
			}
			in.close();

		}catch(Exception e){
			e.printStackTrace();
		}
		
		return response.toString();
	}

}