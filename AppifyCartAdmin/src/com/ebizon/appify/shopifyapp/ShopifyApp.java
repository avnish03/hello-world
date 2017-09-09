package com.ebizon.appify.shopifyapp;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

public class ShopifyApp {
	public static HashMap<String, JSONObject> stores = new HashMap<>();
	private int nonce ;
	Logger log = Logger.getLogger(this.getClass().getSimpleName());


    /**
     *  AppifyDemo App
     *
     */
	private static String appApiKey = "f9d8929d964d1259bc8724bb92ac7516";// AppifyDemo->//"6d2f24db42fb10a7b9f0eead0962422e";
	private static String appApiSecret = "bebcf436cd1d18f975550c8ca699208f";// AppifyDemo -> //"7ab39847e79f683c1fc65b04126a5519";
	private static final String scope = "read_content,write_content,read_themes,write_themes,read_products,write_products,read_customers,write_customers,read_orders," +
			"write_orders," +
			"read_draft_orders,write_draft_orders,read_script_tags,write_script_tags,read_fulfillments,write_fulfillments,read_shipping,write_shipping,read_analytics," +
			"read_checkouts," +
			"write_checkouts,read_reports,write_reports";
	private static final String redirectUri = "https://staging.appifycart.com:8443/AppifyCartAdmin/rest/shopifyapp/redirectedUrl";
	public static final String appifycartWebsite = "../admin/RedirectShopifyApp.jsp";
	public static final String appifyUnauthorized = "../admin/AuthorizationError.html";
	public static final String appifycartMyAppsPage = "https://staging.appifycart.com:8443/AppifyCartAdmin/admin/MyApps.html";
	public static final String shopifyPaymentResult = "https://staging.appifycart.com:8443/AppifyCartAdmin/rest/shopifyapp/shopifyPaymentResult";

    /**
     *  AppifyCart App
     *
     */
//	private static String appApiKey = "050a483590b661c9a7668f0c795f66bd";
//	private static String appApiSecret = "f1c80b992752d20532ceacf037845c8e";
//	private static final String scope = "read_content,write_content,read_themes,write_themes,read_products,write_products,read_customers,write_customers,read_orders"
//            +",write_orders,read_draft_orders,write_draft_orders,read_script_tags,write_script_tags,read_fulfillments,write_fulfillments,read_shipping,"
//                + "write_shipping,read_analytics,read_checkouts,write_checkouts,read_reports,write_reports";
//	private static final String redirectUri = "https://app.appifycart.com/AppifyCartAdmin/rest/shopifyapp/redirectedUrl";
//	public static final String appifycartWebsite = "../admin/RedirectShopifyApp.jsp";
//	public static final String appifyUnauthorized = "../admin/AuthorizationError.html";
//	public static final String appifycartMyAppsPage = "https://app.appifycart.com/AppifyCartAdmin/admin/MyApps.html";
//	public static final String shopifyPaymentResult = "https://app.appifycart.com/AppifyCartAdmin/rest/shopifyapp/shopifyPaymentResult";


    public static String getAppApiKey() {
		return appApiKey;
	}

	public static String getAppApiSecret() {
		return appApiSecret;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	/**
	 *
	 * @param shop
	 * @return URL - authorization url for installing shopify app
	 */
	public String getAuthorizeUrl(String shop) {
		this.generateNounce();
		StringBuilder sb = new StringBuilder("https://");
		sb.append(shop);
		sb.append(".myshopify.com");
		sb.append("/admin/oauth/authorize?client_id=");
		sb.append(ShopifyApp.getAppApiKey());
		sb.append("&scope=");
		sb.append(ShopifyApp.scope);
		sb.append("&redirect_uri=");
		sb.append(ShopifyApp.redirectUri);
		sb.append("&state=");
		sb.append(this.getNonce());
		sb.append("&grant_options[]=");
		URI uri = null;
		try {
			uri = new URI(sb.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		//setting nonce to hashmap
		ShopifyApp.stores.get(shop).put("install_state",getNonce());

		return uri.toString();
	}

	/**
	 *
	 * @param shop
	 * @return URL for shopify recurring payment api
	 */
	public static String getShopifyReccurPaymentsUrl(String shop) {
		StringBuilder sb = new StringBuilder(shop);
		sb.append("/admin/recurring_application_charges.json");
		URI uri = null;
		try {
			uri = new URI(sb.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri.toString();
	}

	// get nonce
	public int getNonce() {
		return this.nonce;
	}

	/**
	 * 	generate nounce for OAuth for installing shopify app
	 */
	private void generateNounce() {
        Random r = new Random();
        this.setNonce(r.nextInt(65536));
	}

	/**
	 * getting the access token from DB filtered by an email and shop name
	 * @param email
	 * @param shop
	 * @return
	 */
    public static String getAccessToken(String email, String shop) {
        String accessToken = null;
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", 0);
        jsonObj.put("message", "Login failed");
        FindIterable<Document> iterable = MongoDBHandler.find("shopifyAPPTokens", new Document("email", email)
                .append("shop",shop));

        Document document = iterable.first();
        if (document != null) {
            accessToken = document.getString("accessToken");
        }
        return accessToken;
    }

	/**
	 * method extracting the shop name from the shop url
	 * @param shopDomain
	 * @return
	 */
	public static String extractShopName(String shopDomain) {
        int myshopify = shopDomain.lastIndexOf(".myshopify.com");
        int protocol = shopDomain.lastIndexOf("://");
        protocol = (protocol != -1)?(protocol+3):0;
        return  shopDomain.substring(protocol, myshopify);
	}
}
