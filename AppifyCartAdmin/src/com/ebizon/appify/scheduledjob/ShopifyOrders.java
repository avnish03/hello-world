package com.ebizon.appify.scheduledjob;

import java.net.Authenticator;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.ebizon.appify.data.ShopData;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.database.ShopAuthenticator;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;

public class ShopifyOrders extends ShopifyJob implements org.quartz.Job{
	private static long numItemsPerPage = 100;
	private static WebHandler webHandler = null;

	public ShopifyOrders(String appId) {
		super(appId);
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("ShopifyOrders is executing.");
		this.loadAllAppOrders();
	}
	
	private void loadAllAppOrders(){
		FindIterable<Document> iterable = MongoDBHandler.find("appData", new Document());

		Block<Document> appBlock = new Block<Document>() { 
			@Override 
			public void apply(final Document document) { 
				String appId =  document.getObjectId("_id").toString();
				execute(appId);
			} 
		}; 

		iterable.forEach(appBlock);
	}

	protected void execute(String appId){
		ShopData shopData = new ShopData(appId);

//		Authenticator.setDefault(new ShopAuthenticator(shopData.getApiKey(), shopData.getPassword()));
		String url = shopData.getWebsiteURL() + "/admin/orders/count.json";
        WebHandler webHandlerObj = new WebHandler(shopData);
		try{
			JSONObject jsonObject = webHandlerObj.readJsonFromUrl(url);
			long count = jsonObject.getLong("count");
			loadOrders(shopData, count,appId);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	public void executeDuplicate(String appId){
		ShopData shopData = new ShopData(appId);
//		Authenticator.setDefault(new ShopAuthenticator(shopData.getApiKey(), shopData.getPassword()));
		String url = shopData.getWebsiteURL() + "/admin/orders/count.json";
        WebHandler webHandlerObj = new WebHandler(shopData);
		try{
			JSONObject jsonObject = webHandlerObj.readJsonFromUrl(url);
			long count = jsonObject.getLong("count");
			loadOrders(shopData, count,appId);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}


	private void loadOrders(ShopData shopData, long count, String appId){
		long numPages = count / numItemsPerPage + 1;
//		Authenticator.setDefault(new ShopAuthenticator(shopData.getApiKey(), shopData.getPassword()));
        WebHandler webHandlerObj = new WebHandler(shopData);
		for(long i = 1; i <= numPages; ++i){
			String ulrExt = String.format("/admin/orders.json?limit=%d&page=%d", numItemsPerPage, i);
			String url = shopData.getWebsiteURL() + ulrExt;
			System.out.println(url);
			try{
				JSONObject jsonObject = webHandlerObj.readJsonFromUrl(url);
				JSONArray orders = jsonObject.getJSONArray("orders");
				saveOrdersToDB(orders, appId);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
	}

	private void saveOrdersToDB(JSONArray jsonArray, String appId){
		String tableName = String.format("shopify_%s_orders", appId);
		System.out.println(String.format("Total orders found %d", jsonArray.length()));
		
		for(Object obj : jsonArray){
			if(obj instanceof JSONObject){
				JSONObject json = (JSONObject)obj;
				long id = json.getLong("id");
				
				Document checkDoc = new Document()
						.append("_id", id);
				Document newDoc = new Document()
						.append("order", Document.parse(json.toString()));
				
				UpdateResult result = MongoDBHandler.updateOne(tableName, checkDoc, newDoc);
				
				if (result.getMatchedCount() == 0){
					newDoc.append("_id", id);
					MongoDBHandler.insertOne(tableName, newDoc);
				}
			}
		}
	}
}
