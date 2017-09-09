package com.ebizon.appify.shopifyapp;

import com.ebizon.appify.data.Login;
import com.ebizon.appify.data.Register;
import com.ebizon.appify.data.ShopData;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.scheduledjob.WebHandler;
import com.ebizon.appify.utils.PostJsonData;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/shopifyapp")
public class InstallApplication {
    @Context
    static UriInfo uriInfo;
    @Context
    static HttpServletRequest request;
    Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getInstallRequest")
    public Response installShopifyApp() {
        System.out.println("\n------------ 1st time ----------------\n");
        logger.log(Level.INFO, "1st request to install shopify app : " + uriInfo.getQueryParameters().toString());

        String shop = uriInfo.getQueryParameters().getFirst("shop");
        shop = ShopifyApp.extractShopName(shop);
        String hmac = uriInfo.getQueryParameters().getFirst("hmac");
        String timestamp = uriInfo.getQueryParameters().getFirst("timestamp");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("install_shop", shop);
        jsonObject.put("install_hmac", hmac);
        jsonObject.put("install_timestamp", timestamp);
        ShopifyApp.stores.put(shop, jsonObject);
        //	DisplayRequestAttributes.printParams(request);
        java.net.URI location = null;
        try {
            location = new java.net.URI(new ShopifyApp().getAuthorizeUrl(shop));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return Response.temporaryRedirect(location).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/redirectedUrl")
    public Response shopifyAppRedirectedUrl() {
        System.out.println("\n------------ 2nd time ----------------\n");
        logger.log(Level.INFO, "2nd request to install shopify app : " + uriInfo.getQueryParameters().toString());
        //DisplayRequestAttributes.printParams(request);

        java.net.URI location = null;
        String loginStatus = null;
        String loginToken = null;
        String creationTime = null;
        String shop = uriInfo.getQueryParameters().getFirst("shop");
        shop = ShopifyApp.extractShopName(shop);
        String hmac = uriInfo.getQueryParameters().getFirst("hmac");
        String code = uriInfo.getQueryParameters().getFirst("code");
        String state = uriInfo.getQueryParameters().getFirst("state");
        String timestamp = uriInfo.getQueryParameters().getFirst("timestamp");

        JSONObject json = ShopifyApp.stores.get(shop);
        json.put("redirected_shop", shop);
        json.put("redirected_hmac", hmac);
        json.put("redirected_code", code);
        json.put("redirected_state", state);
        json.put("redirected_timestamp", timestamp);

        if (HMACValidator.validateNonce(shop)) {

            JSONObject postRequest = new JSONObject();
            postRequest.put("client_id", ShopifyApp.getAppApiKey());
            postRequest.put("client_secret", ShopifyApp.getAppApiSecret());
            postRequest.put("code", ShopifyApp.stores.get(shop).get("redirected_code"));

            String postMessage = postRequest.toString();
            JSONObject jsonResponse;
            JSONObject shopDetails = null;
            JSONArray payments = null;
            String redirectTo = "";
            WebHandler webHandlerObj = null;
            String urlToGetAccessToken = "https://" + shop + ".myshopify.com/admin/oauth/access_token";
            try {
                jsonResponse = PostJsonData.getPostDataResponse(urlToGetAccessToken, postMessage);
                String accessToken = jsonResponse.getString("access_token");
                webHandlerObj = new WebHandler(new ShopData(shop, accessToken, true));
                shopDetails = this.getShopData(shop, webHandlerObj);

                logger.log(Level.INFO, "RESPONSE FOR GETTING ACCESS TOKEN : " + jsonResponse);

                //getting the accesstoken for shopify website for the current user
                JSONObject loginShopifAppResponse = this.loginShopifyApp(shopDetails, accessToken);
                loginStatus = loginShopifAppResponse.get("loginStatus").toString();
                loginToken = loginShopifAppResponse.get("loginToken").toString();
                creationTime = loginShopifAppResponse.get("creationTime").toString();
                payments = loginShopifAppResponse.getJSONArray("payments");
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            //?code={authorization_code}&hmac=da9d83c171400a41f8db91a950508985&timestamp=1409617544&state={nonce}&shop={hostname}

            // checking for free trial and payments
            try {
                String freeTrialStatus = "";
                JSONObject recurringPaymentsListJsonObj;
                JSONObject paymentStatus;
                String currentUser = shopDetails.getJSONObject("shop").getString("email");
                String currentShop = shopDetails.getJSONObject("shop").getString("myshopify_domain");

                boolean force_ssl = shopDetails.getJSONObject("shop").getBoolean("force_ssl");
                // setting the correct protocol for shop
                String protocol = (!force_ssl) ? "http://" : "https://";
                // getting the shopify store (myshopify) domain
                currentShop = protocol.concat(currentShop);

                /**
                 *      get payment info from db and update the new changes for payments
                 */
                //  paymentStatus = getShopifyPaymentStatus(webHandlerObj, currentShop);
                //  logger.log(Level.INFO, "Getting all RecurringApplicationCharge : " + paymentStatus);


                recurringPaymentsListJsonObj = getRecurringChargesList(webHandlerObj, currentShop);
               // paymentStatus = checkShopifyPaymentStatus(recurringPaymentsListJsonObj);  // checking commented for now
                long freeTrialEnds = Long.parseLong(creationTime) + 1209600;


                long currentInstant = Instant.now().getEpochSecond();
                currentUser = Base64.getEncoder().encodeToString(currentUser.getBytes("utf-8"));
                loginStatus = Base64.getEncoder().encodeToString(loginStatus.getBytes("utf-8"));
                loginToken = Base64.getEncoder().encodeToString(loginToken.getBytes("utf-8"));
                currentShop = Base64.getEncoder().encodeToString(currentShop.getBytes("utf-8"));



                System.out.println("\n\ncurrent instant : " + currentInstant);
                System.out.println("freeTrialEnds instant : " + freeTrialEnds);
                if (currentInstant <= freeTrialEnds) {
                    redirectTo = (payments.length() != 0) ? "pricing" : "website";
                    freeTrialStatus = "active";
                } else {
                    redirectTo = "pricing";
                    freeTrialStatus = "NA";
                }

                //freetrial activates for now for all
                freeTrialStatus = "active";
                redirectTo = "website";

                        redirectTo = Base64.getEncoder().encodeToString(redirectTo.getBytes("utf-8"));
                String url = ShopifyApp.appifycartWebsite.concat("?currentUser=" + currentUser + "&loginstatus="
                        + loginStatus + "&loginToken=" + loginToken + "&currentShop=" + currentShop + "&redirectTo="
                        + redirectTo + "&freeTrialStatus=" + freeTrialStatus);
                location = new java.net.URI(url);
                System.out.println("\nredirecting to : " + location.toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                location = new java.net.URI(ShopifyApp.appifyUnauthorized);
                System.out.println("redirecting to " + location.toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return Response.temporaryRedirect(location).build();
    }

    private JSONObject checkShopifyPaymentStatus(JSONObject jsonObject) {
        JSONObject response = new JSONObject();
        JSONArray recurringList = jsonObject.getJSONArray("recurring_application_charges");

        for(Object obj : recurringList){
            if ( obj instanceof JSONObject ) {
                JSONObject json = (JSONObject)obj;
                long api_client_id = json.getLong("id");
                String activated_on = json.get("activated_on").toString();
                String status = json.get("status").toString();

            }
        }
        return null;
    }

    private JSONObject getRecurringChargesList(WebHandler webHandlerObj, String currentShop) {
        String recurringApi = "/admin/recurring_application_charges.json";
        String url = currentShop.concat(recurringApi);
        System.out.println("Recurring url : "+url);
        JSONObject json = webHandlerObj.readJsonFromUrl(ShopifyApp.getShopifyReccurPaymentsUrl(currentShop));

        System.out.println("Response form JSON for recurrly payments : "+json);
        return json;
    }

    private String getShopifyPaymentStatus(WebHandler webHandlerObj, String currentShop) {

        /**
         *  find the doc which directly gicves you information about the payment quickly
         */

        /**
         *  update payment info from shopify then return with payment status
         */

//        Document filterDoc = new Document("shop", currentShop);
//
//        FindIterable<Document> iterable = MongoDBHandler.find("shopifyRecurringPaymentStatus", filterDoc);
//
//        if (iterable.first() != null){
//            Document doc = iterable.first();
//            String status = doc.getObjectId("_id").toString();
//        }

        boolean isPaymentDone = MongoDBHandler.isExist("shopifyRecurringPaymentStatus", new Document("shop", currentShop));
        if (!isPaymentDone) {

        }
        boolean freeTrialExists;
        JSONObject shopifyPayents = webHandlerObj.readJsonFromUrl(ShopifyApp.getShopifyReccurPaymentsUrl(currentShop));
        freeTrialExists = checkForFreeTrial(shopifyPayents);

        return null;
    }

    private boolean checkForFreeTrial(JSONObject getShopifyPayents) {

        return false;
    }

    private JSONObject getShopData(String shop, WebHandler webHandlerObj) {
        String urlToQuery = "https://" + shop + ".myshopify.com/admin/shop.json";
        JSONObject json = webHandlerObj.readJsonFromUrl(urlToQuery);
        return json;
    }

    private JSONObject loginShopifyApp(JSONObject shopDetails, String accessToken) {
        JSONObject response = new JSONObject();
        JSONArray payments = null;
        String loginToken = null;
        String creationTime = null;
        String email = shopDetails.getJSONObject("shop").getString("email");
        String shopname = ShopifyApp.extractShopName(shopDetails.getJSONObject("shop").getString("myshopify_domain"));
        String fullName = shopDetails.getJSONObject("shop").getString("shop_owner");
        String timezone = shopDetails.getJSONObject("shop").getString("timezone");
        if (email != null) {
            // if email not found in shopifyAPPTokens collection then register the install request for new user
            boolean isUserExist = MongoDBHandler.isExist("users", new Document("email", email));
            if (!isUserExist) {
                // if block executed when -> register user as a new user
                Register newUser = new Register(fullName, email, timezone, "shopifyapp");
                JSONObject authorizationTokens = newUser.save();
                System.out.println("new user saved, signed up from shopify app store");
                response.put("loginStatus", "saved as new user ----------->" + authorizationTokens);

                // insert the accesstoken for shopify app on 1st time installation
                Document newDoc = new Document("email", email)
                        .append("shop", shopname)
                        .append("accessToken", accessToken);
                MongoDBHandler.insertOne("shopifyAPPTokens", newDoc);
                System.out.println("inserting new doc in shopifyAPPTokens");

                // now login
                Login login = new Login();
                login.setUsername(email);
                login.setPassword(accessToken);
                JSONObject jsonObjectLoginCheck = login.loginViaShopifyApp(shopname);
                loginToken = jsonObjectLoginCheck.getString("accesstoken");
                creationTime = jsonObjectLoginCheck.getString("creationTime");
                payments = jsonObjectLoginCheck.getJSONArray("payments");
                // prepare response for this method
                response.put("loginStatus", "new user");
                response.put("creationTime", creationTime);
                response.put("payments", payments);
            } else {
                //  else block executed when -> user already registered from website
                Login login = new Login();
                login.setUsername(email);
                login.setPassword(accessToken);
                JSONObject jsonObjectLoginCheck = login.loginViaShopifyApp(shopname);
                if (jsonObjectLoginCheck.getInt("status") != 1) {
                    //  1. update the accesstoken in db
                    Document checkDoc = new Document("email", email)
                            .append("shop", shopname);
                    Document newDoc = new Document("email", email)
                            .append("shop", shopname)
                            .append("accessToken", accessToken);
                    UpdateResult result = MongoDBHandler.updateOne("shopifyAPPTokens", checkDoc, newDoc);
                    System.out.println("UpdateResult : " + result.getMatchedCount());

                    if (result.getMatchedCount() == 0) {
                        // this case handles when user has already registered with website but not registered with shopify app
                        MongoDBHandler.insertOne("shopifyAPPTokens", newDoc);
                        System.out.println("installing the shopify app for existing user; inserting new doc in shopifyAPPTokens");
                    } else {
                        //shopify app is reinstalled by a previous user, now we need to do :
                        System.out.println("reinstalling the shopify app ; updating the document in shopifyAPPTokens collection ");
                    }

                    //  2. reset the website user email password
                    JSONObject passwordresetMessage = new Register(fullName, email, timezone, "shopifyapp")
                            .registerExistingShopifyUser();
                    if (passwordresetMessage.getInt("status") == 1)
                        System.out.println("Password reset again, Reinstalled the appifycart app");
                    else
                        System.out.println("Password reset failed while reinstalling the app");
                    /**
                     *     NOW TAKING THE LOGINTOKEN AFTER RESETTING PASSWORD
                     */
                    jsonObjectLoginCheck = login.loginViaShopifyApp(shopname);
                    payments = jsonObjectLoginCheck.getJSONArray("payments");
                    loginToken = jsonObjectLoginCheck.getString("accesstoken");
                    creationTime = jsonObjectLoginCheck.getString("creationTime");
                } else {
                    /**
                     *    login is success and we just need to extract the access token from login response
                     */
                    System.out.println("login success with existing app, getting accesstoken : " + accessToken);
                    loginToken = jsonObjectLoginCheck.getString("accesstoken");
                    creationTime = jsonObjectLoginCheck.getString("creationTime");
                    payments = jsonObjectLoginCheck.getJSONArray("payments");
                }
                response.put("loginStatus", "existing user");
            }
            response.put("loginToken", loginToken);
            response.put("payments", payments);
            response.put("creationTime", creationTime);
        } else {
            response.put("loginToken", "failed");
            response.put("loginStatus", "email not found");
        }
        return response;
    }

    /**
     * Billing api for shopify app
     *
     * @return
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/recurringCharge")
    public Response recurringChargeApi() {
        System.out.println("/recurringChargeApi");
        logger.log(Level.INFO, "get request for : " + uriInfo.getQueryParameters().toString());
        String confirmation_url = null;
        java.net.URI location = null;
        String returnUrl = ShopifyApp.shopifyPaymentResult;
        int trialDays = 14;

        String myshopify_url = uriInfo.getQueryParameters().getFirst("myshopify_url");
        String name = uriInfo.getQueryParameters().getFirst("plan");
        String email = uriInfo.getQueryParameters().getFirst("user");
        long price = RecurringShopifyAppCharge.getPlanPrice(name);
        try {
            myshopify_url = java.net.URLDecoder.decode(myshopify_url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        String myshopify_domain = ShopifyApp.extractShopName(myshopify_url);
        String accessToken ;
        System.out.println("\n------------ Details for recurring charge api call ----------------\n");
        System.out.println("name : " + name);
        System.out.println("price : " + price);
        System.out.println("email : " + email);
        System.out.println("myshopify_url : " + myshopify_url);
        System.out.println("myshopify_domain : " + myshopify_domain);

        accessToken = ShopifyApp.getAccessToken(email, myshopify_domain);

        JSONObject postRequest = new JSONObject();
        JSONObject recurringObject = new JSONObject();
        recurringObject.put("name", name);
        recurringObject.put("price", price);
        recurringObject.put("return_url", returnUrl);
        recurringObject.put("trial_days", trialDays);
        // recurringObject.put("test", true);
        postRequest.put("recurring_application_charge", recurringObject);
        String postMessage = postRequest.toString();
        System.out.println("post message : ---->\n" + postMessage);
        JSONObject jsonResponse = null;
        String url = myshopify_url.concat("/admin/recurring_application_charges.json");
        try {
            jsonResponse = PostJsonData.getPostDataResponseWithAccessToken(url, postMessage, accessToken);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "RESPONSE FOR BILLING API REQUEST : " + jsonResponse);
        confirmation_url = jsonResponse.getJSONObject("recurring_application_charge").get("confirmation_url").toString();
        try {
            location = new java.net.URI(confirmation_url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return Response.temporaryRedirect(location).build();
    }

    /**
     *  generate the document to be inserted in mongo for reccur payemtns
     * @param jsonObject
     * @return
     */
    private Document generateReccurDoc(JSONObject jsonObject) {
        Document doc = new Document();
        System.out.println("reccur payment doc : "+jsonObject);
        Iterator<String> keys = jsonObject.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            Object value = jsonObject.get(key);
            if(jsonObject.isNull(key)){
                value = "NA_null";
            }
            doc.append(key,value);
        }
        System.out.println("DOC to be apppend in omngon"+doc);
        return doc;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/shopifyPaymentResult")
    public Response paymentSuccess() {
        System.out.println("/shopifyPaymentResult");

        String status = null;
        String trial_ends_on;
        String redirect_url = ShopifyApp.appifycartMyAppsPage;
        java.net.URI location = null;
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        System.out.println("params" + queryParams.toString());

        String charge_id = uriInfo.getQueryParameters().getFirst("charge_id");
        if(charge_id!=null){

            Document filterDoc = new Document("id", charge_id);
            FindIterable<Document> iterable = MongoDBHandler.find("shopifyRecurringPayments", filterDoc);

            if (iterable.first() != null){
                Document doc = iterable.first();
                status = doc.get("status").toString();
                trial_ends_on = doc.get("trial_ends_on").toString();
            }
//            if(status.equalsIgnoreCase("active") ) {
//                redirect_url = ShopifyApp.appifycartMyAppsPage;
//            }
//            if(status.equalsIgnoreCase("pending") ) {
//                redirect_url = ShopifyApp.appifycartMyAppsPage;
//            }
            redirect_url = ShopifyApp.appifycartMyAppsPage;
        }

        JSONObject json = new JSONObject();
        json.put("data", queryParams.toString());
        logger.log(Level.INFO, "Shopify Payment Result ; Response : " + queryParams.toString());
        try {
            location = new java.net.URI(redirect_url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return Response.temporaryRedirect(location).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/redirectget")
    public JSONObject redirectgetTest() {
        System.out.println("/fails");
        String currentUser = "avnish.kumar@ebizontek.com";
        String loginStatus = "abcdefgnsdfsmgdklg";
        String loginToken = "TOKEN_abcdefgnsdfsmgdklg";
        String url = "../admin/RedirectShopifyApp.jsp?currentUser=" + currentUser + "loginstatus=" + loginStatus + "&loginToken=" + loginToken;
        java.net.URI location = null;
        try {
            location = new java.net.URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "123");
        jsonObject.put("value", "abc");
        return jsonObject;
    }
}