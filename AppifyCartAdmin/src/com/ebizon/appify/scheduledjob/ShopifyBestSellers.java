package com.ebizon.appify.scheduledjob;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.Constants;
import com.ebizon.appify.utils.TableNameGenerator;
import com.ebizon.appify.utils.Utils;
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

import java.util.HashMap;
import java.util.Map;

public class ShopifyBestSellers extends ShopifyJob implements org.quartz.Job{
	private static int MaxNumberOfProducts = 10;
	
	public ShopifyBestSellers(String appId) {
		super(appId);
	}
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("ShopifyBestSellers is executing.");
		this.createAllAppBestSellers();
	}

	private void createAllAppBestSellers(){
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
			JSONArray orders = this.loadOrders(appId);
			JSONArray products = this.loadProducts(appId);
			
			JSONArray bestsellers = filterBestSellerProducts(orders, products);
			
			repopulateBestSellers(appId, bestsellers);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
    public void executeDuplicate(String appId){
        try{
            JSONArray orders = this.loadOrders(appId);
            JSONArray products = this.loadProducts(appId);

            JSONArray bestsellers = filterBestSellerProducts(orders, products);

            repopulateBestSellers(appId, bestsellers);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
	private JSONArray filterBestSellerProducts(JSONArray orders, JSONArray products){
		Map<Long, Long> sortedMap = getOrderedCounts(orders);

		JSONArray filteredObjects = new JSONArray();
		
		for (Map.Entry<Long, Long> entry : sortedMap.entrySet()){
			System.out.println(String.format("Order pair %d%n : %d%n", entry.getKey(), entry.getValue()));
			JSONObject foundObject = findProduct(products, entry.getKey());
			if(foundObject != null){
				filteredObjects.put(foundObject);
			}
			
			if (filteredObjects.length() == MaxNumberOfProducts){
				break;
			}
		}
		
		return filteredObjects;
	}

	private void repopulateBestSellers(String appId, JSONArray newBestSellers){
		String collectionName = TableNameGenerator.getShopifyTable(appId, Constants.BestSellers);
		int index = 0;
		
		for(Object obj : newBestSellers){
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
	
	private JSONArray loadOrders(String appId){
		JSONArray jsonArray = new JSONArray();
		String collectionName = TableNameGenerator.getShopifyTable(appId, Constants.Orders);

        FindIterable<Document> iterable = MongoDBHandler.find(collectionName, new Document());

		Block<Document> appBlock = new Block<Document>() { 
			@Override 
			public void apply(final Document document) { 
				JSONObject jsonObj = new JSONObject(JSON.serialize(document.get("order")));
				jsonArray.put(jsonObj);
			} 
		}; 

		iterable.forEach(appBlock);

		System.out.println(String.format("Number of orders loaded %d", jsonArray.length()));
		return jsonArray;
	}
	
	private JSONObject findProduct(JSONArray products, long idToSearch){
		JSONObject foundObject = null;
		
		for(int i = 0; i < products.length(); ++i){
			JSONObject object = products.getJSONObject(i);
			long id = object.getLong("id");
			
			if(idToSearch == id){
				System.out.println(String.format("found idToSearch %d vs id %d", idToSearch, id));
				foundObject = object;
				break;
			}
		}
		return foundObject;
	}
	
	private Map<Long, Long> getOrderedCounts(JSONArray orders){
		Map<Long, Long> hashMap = new HashMap<Long, Long>();
		long prev_Null_product_id=Long.MIN_VALUE;
		try{
			for (int i = 0; i < orders.length(); ++i){
				JSONObject jsonObject = orders.getJSONObject(i);
				JSONArray lineItems = jsonObject.getJSONArray("line_items");
				for (int j = 0; j < lineItems.length(); ++j){
					JSONObject json = lineItems.getJSONObject(j);
                    long id = prev_Null_product_id;
                    if(json.get("product_id") == JSONObject.NULL){
                        prev_Null_product_id += 1;
                    }else{
                        id = json.getLong("product_id");
                    }
                    if (hashMap.containsKey(id)) {
                        hashMap.put(id, hashMap.get(id) + 1);
                    } else {
                        hashMap.put(id, 1L);
                    }
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}

		return Utils.sortByValue(hashMap, false);
	}
}