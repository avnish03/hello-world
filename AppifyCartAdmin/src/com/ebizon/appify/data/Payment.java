package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Payment {
	private final String  accountEmail;
	private final String productID;
	private final String orderNumber;
	private final String invoiceID;
	private final String currencyCode;
	private final String amount;
	private final String paymentMethod;

	public Payment(String accountEmail, String productID, String orderNumber, String invoiceID, String currencyCode, String amount, String paymentMethod){
		this.accountEmail = accountEmail;
		this.productID = productID;
		this.orderNumber = orderNumber;
		this.invoiceID = invoiceID;
		this.currencyCode = currencyCode;
		this.amount = amount;
		this.paymentMethod = paymentMethod;
	}

	public void save(){
		List<JSONObject> oldPayments =  Payment.loadPayments(this.accountEmail);
		
		Document newDoc = new Document()
				.append("accountemail", this.accountEmail)
				.append("productID", this.productID)
				.append("orderNumber", this.orderNumber)
				.append("invoiceID", this.invoiceID)
				.append("currencyCode", this.currencyCode)
				.append("amount", this.amount)
				.append("paymentMethod", this.paymentMethod);

		MongoDBHandler.insertOne("payment", newDoc);
	}
	
	public static List<JSONObject> loadPayments(String userName){
		List<JSONObject> jsonList = new ArrayList<>();

		FindIterable<Document> iterable = MongoDBHandler.find("payment", new Document("accountemail", userName))
				.sort(new BasicDBObject("creationTime", -1)).limit(1);

		Block<Document> appBlock = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("accountemail", document.get("accountemail"));
				jsonObj.put("productID", document.get("productID"));
				jsonObj.put("orderNumber", document.get("orderNumber"));
				jsonObj.put("invoiceID", document.get("invoiceID"));
				jsonObj.put("currencyCode", document.get("currencyCode"));
				jsonObj.put("amount", document.get("amount"));
				jsonObj.put("paymentMethod", document.get("paymentMethod"));
				jsonObj.put("creationTime", document.get("creationTime"));
				jsonList.add(jsonObj);
			}
		};

		iterable.forEach(appBlock);
		
		return jsonList;
	}
}
