package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBContextListener;
import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.Block;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by avnish on 23/1/17.
 */

public class CMSPages {
    ArrayList<String> numbers = new ArrayList<String>();
    // public String totalCMS_Pages ;
    public String email;
    public String pageId;
    public String title;
    public String content;
    public Boolean isEnable ;

    public JSONObject saveData(){
        JSONObject jsonObj = new JSONObject();
        JSONObject jsonObjResult ;
        jsonObj.put("status", 0);
        jsonObj.put("message", "failed");

        boolean isExist = MongoDBHandler.isExist("CMS", new Document("_id",new ObjectId(this.pageId))) ;
        if(!isExist){
            jsonObjResult = this.insertCMSData(jsonObj);
        }
        else{
            jsonObjResult = this.updateCMSPages(jsonObj, new ObjectId(this.pageId));
        }
        jsonObjResult.put("status",1);
        return jsonObjResult;
    }

    //  Insert CMS function
    public JSONObject insertCMSData(JSONObject jsonObj){
        FindIterable<Document> iterable = MongoDBHandler.find("CMS",  new Document("email", this.email));
        Document resetData = iterable.first();
        if(this.email != null) {
            Document document = new Document("email", this.email);
          //  String randomHexValue = this.getRandomHexString(this.title.length());
         //   System.out.println("random Hex Value  :  " +randomHexValue) ;
         //   Document CMSDoc = new Document("pageId", randomHexValue);
           // System.out.println("CMSDoc : "+CMSDoc  );
       //     document.put("pageId" , randomHexValue);
            document.put("title",this.title);
            document.put("content",this.content);
            document.put("isEnable",this.isEnable);
            try {
                MongoDBHandler.insertOne("CMS", document);
                jsonObj.put("status","1");
                jsonObj.put("message","success");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
        return jsonObj;
    }

    // Update CMS page
    public JSONObject updateCMSPages(JSONObject jsonObj, ObjectId pageId){
        String email = this.email;
        Document filterDoc = new Document("_id",pageId);
        System.out.println("FilterDoc :  "+filterDoc);
        Document newDoc = new Document("_id", new ObjectId(this.pageId))
                .append("title", this.title)
                .append("content", this.content)
                .append("isEnable" , this.isEnable);
        System.out.println("New Doc :  "+newDoc);
        UpdateResult result = MongoDBHandler.updateOne("CMS", filterDoc, new Document(newDoc));
        System.out.println("on line 83"+result);
        if (result.getMatchedCount() > 0){
            jsonObj.put("status", 1);
            jsonObj.put("message", "CMS data updated successfully");
        }else{
            jsonObj.put("status", 0);
            jsonObj.put("message", "CMS data update failed");
        }
        return jsonObj;
    }

// function for delete document

    public Boolean deleteCMS(){
        boolean isDeleted = false ;
        System.out.println("-------- page Id ----------> "+this.pageId);
        boolean isExist = MongoDBHandler.isExist("CMS", new Document("_id",new ObjectId(this.pageId))) ;
        System.out.println("---------- isExit Result --------> " +isExist);
        if(!isExist){
            System.out.println("Page id not Exist");
        }
        else{
            isDeleted = this.deleteCMSPages(this.pageId);
        }
        return isDeleted;
    }

    public  Boolean deleteCMSPages(String pageId){
        String email = this.email;
        Document filterDoc = new Document("pageId",pageId);
        Boolean isDeleted =  MongoDBHandler.deleteOne("CMS", new Document("_id", new ObjectId(this.pageId))) ;

        return  isDeleted ;
    }

    public JSONObject fetchData(String userName) {
        List<JSONObject> jsonList = new ArrayList<>();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", 0);
        jsonObj.put("message", "failed");

        Document filterDoc = new Document("email", userName);

        boolean isExist = MongoDBHandler.isExist("CMS", filterDoc);
        if (isExist) {
            FindIterable<Document> iterable = MongoDBHandler.find("CMS", filterDoc);
            Block<Document> appBlock = new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    System.out.println("DOCUMETN : : : :: : "+ document);
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("email", document.get("email"));
                    jsonObj.put("pageId", document.get("pageId"));
                    jsonObj.put("title", document.get("title"));
                    jsonObj.put("content", document.get("content"));
                    jsonObj.put("isEnable", document.get("isEnable"));
                    jsonObj.put("id" , document.get("_id"));
                    jsonList.add(jsonObj);
                }
            };
            iterable.forEach(appBlock);
            jsonObj.put("status", 1);
            jsonObj.put("message", "success");
        }else{
            System.out.print("CMS Document not exists");
        }
        jsonObj.put("data",jsonList);
        System.out.println("CMS TOTOAL DATA : "+ jsonObj.toString());
        return jsonObj;
    }
    private String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }
}
