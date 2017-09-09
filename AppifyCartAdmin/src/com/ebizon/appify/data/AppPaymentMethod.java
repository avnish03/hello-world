package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppPaymentMethod {
	private String accountEmail;
	private String paymentMethodType;
	private String clientId;
	private String clientIdType;
	private String status;
	
	public AppPaymentMethod(){
		//Empty
	}
	
	public AppPaymentMethod(String accountEmail, String paymentMethodType, String clientId, String clientIdType, String status){
		this.accountEmail = accountEmail;
		this.paymentMethodType = paymentMethodType;
		this.clientId = clientId;
		this.clientIdType = clientIdType;
		this.status = status;
	}

	public void setAccountEmail(String accountEmail){
		this.accountEmail = accountEmail;
	}
	
	public void setPaymentMethodType(String paymentMethodType){
		this.paymentMethodType = paymentMethodType;
	}
	
	public void setClientId(String clientId){
		this.clientId = clientId;
	}
	
	public void setClientIdType(String clientIdType){
		this.clientIdType = clientIdType;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public void save(){		
		Document checkDoc = new Document("accountemail", this.accountEmail)
				.append("paymentMethodType", this.paymentMethodType);
		
		Document newDoc = new Document("accountemail", this.accountEmail)
				.append("paymentMethodType", this.paymentMethodType)
				.append("clientId", this.clientId)
				.append("clientIdType", this.clientIdType)
				.append("status", this.status);

		UpdateResult result = MongoDBHandler.updateOne("appPaymentMethod", checkDoc, newDoc);
		
		if (result.getMatchedCount() == 0){
			MongoDBHandler.insertOne("appPaymentMethod", newDoc);
		}
	}
	
	public static List<JSONObject> loadPaymentMethods(String userName){
		List<JSONObject> jsonList = new ArrayList<>();

		FindIterable<Document> iterable = MongoDBHandler.find("appPaymentMethod", new Document("accountemail", userName));
		
		Block<Document> appBlock = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("accountemail", document.get("accountemail"));
				jsonObj.put("paymentMethodType", document.get("paymentMethodType"));
				jsonObj.put("clientId", document.get("clientId"));
				jsonObj.put("clientIdType", document.get("clientIdType"));
				jsonObj.put("status", document.get("status"));
				jsonList.add(jsonObj);
			}
		};

		iterable.forEach(appBlock);
		
		return jsonList;
	}
}
