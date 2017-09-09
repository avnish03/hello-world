package com.ebizon.appify.scheduledjob;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.Constants;
import com.ebizon.appify.utils.TableNameGenerator;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

public class ShopifyNewProducts extends ShopifyJob implements org.quartz.Job{
	private static final int MaxNumberOfProducts = 10;

	public ShopifyNewProducts(String appId) {
		super(appId);
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("ShopifyNewProducts is executing.");
		this.createAllAppNewProducts();
	}

	private void createAllAppNewProducts(){
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
		try{
			JSONArray products = this.loadProducts(appId);

			JSONArray latestProducts = filterLatestProducts(products);

			repopulateLatestProducts(appId, latestProducts);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	// for executing the syncing of data manually
    public void executeDuplicate(String appId){
		try{
			JSONArray products = this.loadProducts(appId);

			JSONArray latestProducts = filterLatestProducts(products);

			repopulateLatestProducts(appId, latestProducts);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	private JSONArray filterLatestProducts(JSONArray products){
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < products.length(); i++) {
            jsonValues.add(products.getJSONObject(i));
        }

        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "created_at";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                return -valA.compareTo(valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });
        int count = Math.min(MaxNumberOfProducts, products.length());

        for (int i = 0; i < count; i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }

	    
	    System.out.println(String.format("Number of new products populated %d", sortedJsonArray.length()));
	    
		return sortedJsonArray;
	}

	private void repopulateLatestProducts(String appId, JSONArray latestProducts){
		String collectionName = TableNameGenerator.getShopifyTable(appId, Constants.NewProducts);
		int index = 0;

		for(Object obj : latestProducts){
			if(obj instanceof JSONObject){
				JSONObject json = (JSONObject)obj;

				Document checkDoc = new Document()
						.append("index", index);
				Document newDoc = new Document()
						.append("product", Document.parse(json.toString()));

				UpdateResult result = MongoDBHandler.updateOne(collectionName, checkDoc, newDoc);

				if (result.getMatchedCount() == 0){
					newDoc.append("index", index);
					MongoDBHandler.insertOne(collectionName, newDoc);
				}

				++index;
			}
		}
	}

	private JSONArray loadProducts(String appId){
		JSONArray jsonArray = new JSONArray();
		String collectionName = TableNameGenerator.getShopifyTable(appId, Constants.Products);

		FindIterable<Document> iterable = MongoDBHandler.find(collectionName, new Document());

		Block<Document> appBlock = new Block<Document>() { 
			@Override 
			public void apply(final Document document) { 
				JSONObject jsonObj = new JSONObject(JSON.serialize(document.get("product")));
				jsonArray.put(jsonObj);
			} 
		}; 

		iterable.forEach(appBlock);

		System.out.println(String.format("Number of products loaded %d", jsonArray.length()));
		return jsonArray;
	}
}
