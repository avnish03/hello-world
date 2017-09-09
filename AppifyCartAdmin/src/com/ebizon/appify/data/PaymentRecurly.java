package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentRecurly {
	private final String  accountEmail;
	private final String plan;

	public PaymentRecurly(String accountEmail, String plan ){
		this.accountEmail = accountEmail;
		this.plan = plan;
	}

	public void save(){
		List<JSONObject> oldPayments =  PaymentRecurly.loadPayments(this.accountEmail);
		
		Document newDoc = new Document()
				.append("accountemail", this.accountEmail)
				.append("plan", this.plan);

		MongoDBHandler.insertOne("payment", newDoc);
		
		if (oldPayments.size() > 0){
			Logger logger = Logger.getLogger(this.accountEmail);
			JSONObject firstObj = oldPayments.iterator().next();
			String plan = firstObj.getString("plan");
			logger.log(Level.INFO, String.format("Plan registeres in RecurlyResponse for last payment is "+plan));
		}
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
				jsonObj.put("plan", document.get("plan"));
				jsonList.add(jsonObj);
			}
		};

		iterable.forEach(appBlock);
		
		return jsonList;
	}
}
