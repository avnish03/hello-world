package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.scheduledjob.WebHandler;
import com.ebizon.appify.utils.Constants;
import com.ebizon.appify.utils.TableNameGenerator;
import com.ebizon.appify.utils.Utils;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 02/01/17.
 */
public class CategoryMapData {
    private String user;
    private String osType;
    private JSONArray categoryMaps;
    public CategoryMapData(){
        //Empty
    }

    public void setUser(String user){
        this.user = user;
    }

    public void setOsType(String osType){
        this.osType = osType;
    }

    public void setCategoryMaps(JSONArray categoryMaps){
        this.categoryMaps = categoryMaps;
    }

    public void update() {
       if (this.categoryMaps != null) {
            String appId = Utils.getAppId(this.user, this.osType);
            System.out.println(this.categoryMaps);
            String tableName = TableNameGenerator.getShopifyTable(appId, Constants.Category);

            for (Object obj : categoryMaps) {
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    Document checkDoc = new Document("_id", jsonObject.getString("categoryId"));

                    Document newDoc = new Document("parentId", jsonObject.getString("parentId"))
                            .append("status", jsonObject.getString("status"));

                    MongoDBHandler.updateOne(tableName, checkDoc, newDoc);
                }
            }
        }
    }

    public static List<JSONObject> getCategoryMapData(String appId){
        ShopData shopData = new ShopData(appId);
//        Authenticator.setDefault(new ShopAuthenticator(shopData.getApiKey(), shopData.getPassword()));
        WebHandler webHandlerObj = new WebHandler(new ShopData(appId));
        String baseURL = shopData.getWebsiteURL();
        String urlSmartCollection = baseURL + "/admin/smart_collections.json";
        String urlCustomeCollection =  baseURL + "/admin/custom_collections.json";

        try {
            saveCollections(webHandlerObj, appId, urlCustomeCollection, "custom_collections");
            saveCollections(webHandlerObj, appId, urlSmartCollection, "smart_collections");
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return CategoryMapData.loadCollectionData(appId);
    }

    private static void saveCollections(WebHandler webHandlerObj, String appId, String url, String collectionTag){

        JSONObject jsonObject = webHandlerObj.readJsonFromUrl(url);

        JSONArray collection = jsonObject.getJSONArray(collectionTag);
        String tableName = TableNameGenerator.getShopifyTable(appId, Constants.Category);

        for(Object obj : collection){
            if(obj instanceof JSONObject){
                JSONObject json = (JSONObject)obj;
                long id = json.getLong("id");
                String name = json.getString("title");
                Document checkDoc = new Document()
                        .append("_id", Long.toString(id));

                long count = MongoDBHandler.count(tableName, checkDoc);

                if (count == 0){
                    checkDoc.append("name", name);
                    checkDoc.append("parentId", "");
                    checkDoc.append("status", "Enabled");
                    MongoDBHandler.insertOne(tableName, checkDoc);
                }
            }
        }
    }

    private static List<JSONObject>  loadCollectionData(String appId){
        List<JSONObject> jsonList = new ArrayList<>();

        String tableName = TableNameGenerator.getShopifyTable(appId, Constants.Category);
        FindIterable<Document> iterable = MongoDBHandler.find(tableName, new Document());


        Block<Document> appBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("categoryId", document.get("_id"));
                jsonObj.put("categoryName", document.get("name"));
                jsonObj.put("parentId", document.get("parentId"));
                jsonObj.put("status", document.get("status"));

                jsonList.add(jsonObj);
            }
        };

        iterable.forEach(appBlock);

        System.out.println(String.format("Number of categoryMapData loaded %d", jsonList.size()));

        return jsonList;
    }
}
