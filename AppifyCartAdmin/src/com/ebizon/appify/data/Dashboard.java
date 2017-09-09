package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.scheduledjob.WebHandler;
import com.ebizon.appify.utils.Utils;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avnish on 30/3/17.
 */
public class Dashboard {

    public String email;
    public String iosAppId;
    public String androidAppId;
    public long active_users;
    public int new_orders;
    public int total_profit;



    public JSONObject fetchData(String user) {
        JSONObject jsonObject = new JSONObject();

        // setting iosAppId and androidAppId to get their respective stats for each os
        this.iosAppId = Utils.getAppId(this.email, "ios");
        this.androidAppId = Utils.getAppId(this.email, "android");
        //getting total active users
        this.active_users = getAndroidActiveUsers() + getIOSActiveUsers() ;
        jsonObject.put("active_users",this.active_users);

        //getting total payments done



        return jsonObject ;
    }


    public long getIOSActiveUsers(){
        long count = 0;
        Document filterDoc = new Document("appId",this.iosAppId);
        count = MongoDBHandler.count("appDeviceIds", filterDoc);
        return  count;
    }
    public long getAndroidActiveUsers(){
        long count = 0 ;
        Document filterDoc = new Document("appId",this.androidAppId);
        count = MongoDBHandler.count("payment", filterDoc);
        return  count;
    }

    public long getTotalPayments(){
        long count = 0 ;
        Document filterDoc = new Document("appId",this.androidAppId);
        count = MongoDBHandler.count("appDeviceIds", filterDoc);
        return  count;
    }

}
