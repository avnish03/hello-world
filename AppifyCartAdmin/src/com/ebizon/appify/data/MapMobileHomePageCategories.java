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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by avnish on 04/09/17.
 */
public class MapMobileHomePageCategories {
    private String user;
    private String osType;
    private JSONArray categoryMaps;

    ArrayList<HomePageSection> listSection = new ArrayList<HomePageSection>(3);
    public MapMobileHomePageCategories(){
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

    /**
     *  *    Maps the Collections to show in the homepage sections
     */
    public void updateAppHomepageSections() {
       if (this.categoryMaps != null) {
            String appId = Utils.getAppId(this.user, this.osType);
            System.out.println("\n\n update categories list : "+this.categoryMaps);
            String tableName = TableNameGenerator.getShopifyTable(appId, Constants.AppSections);

           int index = 0;
            for (Object obj : categoryMaps) {

                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    Document checkDoc = new Document("section", jsonObject.getString("section"));

                    System.out.println("\n\n updating "+index+": checkDoc : "+checkDoc);
                    Document newDoc = new Document("sectionMapId", String.valueOf(jsonObject.getLong("sectionMapId")))
                            .append("section", jsonObject.getString("section"))
                            .append("name", jsonObject.getString("sectionName"))
                            .append("status", jsonObject.getString("status"));
                    System.out.println("\n\n updating : newDoc : "+newDoc);

                    MongoDBHandler.updateOne(tableName, checkDoc, newDoc);
                }
                index++;
            }
        }
    }

    /**
     *      HOMEPAGE
     *     loading the callections and subcollections showing in the homepage sections
     *
     * @param appId
     * @return
     */
    public JSONObject getAppHomepageSectionsMapping(String appId){
        ShopData shopData = new ShopData(appId);
        WebHandler webHandlerObj = new WebHandler(new ShopData(appId));
        String baseURL = shopData.getWebsiteURL();
        JSONObject jsonObjectAllCategories = new JSONObject();
        try {
            jsonObjectAllCategories = getAllCategories(webHandlerObj, baseURL);
            System.out.println("\n\n\njsonobjects for all categories "+jsonObjectAllCategories);
            saveAppHomepageSections(appId, Constants.Collections, jsonObjectAllCategories);
        }catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObjectResponse = new JSONObject();
        jsonObjectResponse.put("mappings",MapMobileHomePageCategories.loadAppHomepageSectionsData(appId));
        jsonObjectResponse.put("allCategories",jsonObjectAllCategories);
        return jsonObjectResponse;
    }

    /**
     * HOMEPAGE
     *
     *  save collection for sections in mobile app home page
     * @param appId
     * @param collectionTag
     * @param jsonObject
     */
    private void saveAppHomepageSections(String appId, String collectionTag, JSONObject jsonObject){
        JSONArray collection = jsonObject.getJSONArray(collectionTag);
        System.out.println("collectionTag : "+collectionTag);
        String tableName = TableNameGenerator.getShopifyTable(appId, Constants.AppSections);
        System.out.println("tableName : "+tableName);
        System.out.println("saving Collections in mapcategories : "+collection);

        for(int i = 0 ; i < Constants.NoOfSectionsInMobile ; i++){
            Document checkDoc = new Document()
               .append("section", "section"+i);
            long count = MongoDBHandler.count(tableName, checkDoc);
            if (count == 0){
                String status = (i == 3)?"Disabled":"Enabled";
                checkDoc.append("name", getSectionName(i));
                checkDoc.append("sectionMapId", getSectionId(i));
                checkDoc.append("status", status);
                MongoDBHandler.insertOne(tableName, checkDoc);
            }
        }
    }

    /**
     * HOMEPAGE
     *
     *  load the Collections for sections in mobile app home page
     * @param appId
     * @return
     */
    private static List<JSONObject>  loadAppHomepageSectionsData(String appId){
        List<JSONObject> jsonList = new ArrayList<>();

        String tableName = TableNameGenerator.getShopifyTable(appId, Constants.AppSections);
        FindIterable<Document> iterable = MongoDBHandler.find(tableName, new Document());

        Block<Document> appBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("section", document.get("section"));
                jsonObj.put("sectionName", document.get("name"));
                jsonObj.put("sectionMapId", document.get("sectionMapId"));
                jsonObj.put("status", document.get("status"));

                jsonList.add(jsonObj);
            }
        };
        iterable.forEach(appBlock);
        System.out.println(String.format("Number of AppHomepageSections Data loaded %d", jsonList.size()));
        System.out.println("AppHomepageSections Data loaded "+jsonList);
        return jsonList;
    }


    /**
     * HOMEPAGE
     *
     *  method to get all categories in single json object
     * @param webHandlerObj
     * @param baseURL
     * @return
     */
    private JSONObject getAllCategories(WebHandler webHandlerObj, String baseURL) {
        String urlSmartCollection = baseURL + "/admin/smart_collections.json";
        String urlCustomeCollection =  baseURL + "/admin/custom_collections.json";
        JSONObject response = new JSONObject();

        try {
            JSONObject jsonSmartColl = webHandlerObj.readJsonFromUrl(urlSmartCollection);
            JSONObject jsonCustomColl = webHandlerObj.readJsonFromUrl(urlCustomeCollection);
            response = parseAllCategories(jsonCustomColl,jsonSmartColl);

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }


    /**
     * HOMEPAGE
     *
     *  method to return all custom and smart Collections as single unit
     *
     * @param jsonCustom
     * @param jsonSmart
     * @return
     * @throws JSONException
     */

    public JSONObject parseAllCategories(JSONObject jsonCustom, JSONObject jsonSmart) throws JSONException {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        HashMap<String, JSONObject> allCollectionMap = new HashMap<>();

        JSONArray custom_collections = jsonCustom.getJSONArray("custom_collections");
        JSONArray smart_collections = jsonSmart.getJSONArray("smart_collections");

        populateCollection(allCollectionMap, smart_collections);

        populateCollection(allCollectionMap, custom_collections);

        addFixedCollection(allCollectionMap);
        //  filterCollection(appID, allCollectionMap);      //filtering the unavailable Collections
        System.out.println("allCollectionMap.values() : : : "+allCollectionMap.values());
        hashMap.put(Constants.Collections, allCollectionMap.values());

        return new JSONObject(hashMap);
    }

    private void addFixedCollection(HashMap<String, JSONObject> allCollectionMap) {
        for(int i = 0 ; i < 3 ; i++){
            String title = getSectionName(i);
            String id= String.valueOf(getSectionId(i));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("title", title);

            allCollectionMap.put(id, jsonObject);
        }
    }

    /**
     * HOMEPAGE
     *
     *  populate from json of a collection for getting all categories
     * @param allCollectionMap
     * @param collections
     */
    private static void populateCollection(HashMap<String, JSONObject> allCollectionMap, JSONArray collections) {
        for (int i = 0; i < collections.length(); i++) {
            String id = Long.toString(collections.getJSONObject(i).getLong("id"));
            String title = collections.getJSONObject(i).getString("title");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("title", title);

            allCollectionMap.put(id, jsonObject);
        }
    }
    private String getSectionName(int i) {
        if(i == 0)
            return Constants.NewProducts;
        else if(i == 1)
            return Constants.BestSellers;
        else if( i == 2)
            return Constants.CustomCategory;
        else
            return null;
    }

    private int getSectionId(int i) {
        if(i == 0)
            return Constants.NewProductsId;
        else if(i == 1)
            return Constants.BestSellersId;
        else if( i == 2)
            return Constants.CustomCatogoryID;
        else
            return 0;
    }

    private class HomePageSection {
        String section;

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }
    }
}
