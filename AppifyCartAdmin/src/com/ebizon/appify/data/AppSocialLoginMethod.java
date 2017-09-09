package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppSocialLoginMethod {
	private String accountEmail;
	private String osType;
	private String socialLoginMethodType;
	private String developerId;
	private String status;
	
	public AppSocialLoginMethod(){
		//Empty
	}
	
	public AppSocialLoginMethod(String accountEmail, String osType, String socialLoginMethodType, String developerId, String status){
		this.accountEmail = accountEmail;
		this.osType = osType;
		this.socialLoginMethodType = socialLoginMethodType;
		this.developerId = developerId;
		this.status = status;
	}

	public void setAccountEmail(String accountEmail){
		this.accountEmail = accountEmail;
	}
	
	public void setOsType(String osType){
		this.osType = osType;
	}
	
	public void setSocialLoginMethodType(String socialLoginMethodType){
		this.socialLoginMethodType = socialLoginMethodType;
	}
	
	public void setDeveloperId(String developerId){
		this.developerId = developerId;
	}
		
	public void setStatus(String status){
		this.status = status;
	}
		
	public void save(){			
		Document checkDoc = new Document("accountemail", this.accountEmail)
				.append("socialLoginMethodType", this.socialLoginMethodType)
				.append("osType", this.osType);
		
		Document newDoc = new Document(checkDoc)
				.append("developerId", this.developerId)
				.append("status", this.status);

		UpdateResult result =  MongoDBHandler.updateOne("appSocialLoginMethod", checkDoc, newDoc);
		
		if (result.getMatchedCount() == 0){
			MongoDBHandler.insertOne("appSocialLoginMethod", newDoc);
		}
	}
	
	public static List<JSONObject> loadSocialLoginMethods(String userName){
		List<JSONObject> jsonList = new ArrayList<>();

		FindIterable<Document> iterable = MongoDBHandler.find("appSocialLoginMethod", new Document("accountemail", userName));

		Block<Document> appBlock = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("accountemail", document.get("accountemail"));
				jsonObj.put("osType", document.get("osType"));
				jsonObj.put("socialLoginMethodType", document.get("socialLoginMethodType"));
				jsonObj.put("developerId", document.get("developerId"));
				jsonObj.put("status", document.get("status"));
				jsonList.add(jsonObj);
			}
		};

		iterable.forEach(appBlock);
		
		return jsonList;
	}
}
