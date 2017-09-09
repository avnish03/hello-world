package com.ebizon.appify.scheduledjob;

import com.ebizon.appify.data.ShopData;
import com.ebizon.appify.database.ShopAuthenticator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class WebHandler {
    private static final String USER_AGENT = "Mozilla/5.0";
    private ShopData shopData ;
    public WebHandler(ShopData shopData){
        this.shopData = shopData;
        if(!this.shopData.isAccessTokenRequired()){
            Authenticator.setDefault(new ShopAuthenticator(shopData.getApiKey(), shopData.getPassword()));
        }
    }
/*
	public JSONObject readJsonFromUrltemp(String urlToQuery){
		JSONObject json = new JSONObject();
        System.out.println("accessing url : "+urlToQuery);
        try{
            URL url = new URL(urlToQuery);
            URLConnection urlConnection = url.openConnection();
            if(this.shopData.isAccessTokenRequired()){
                    System.out.println("access token to get authorization needed : "+this.shopData.getAccessToken());
                    */
/**
                     *    X -Shopify-Access-Token doesn't work, so password is used as key for passing acess token in header
                     *//*

                    urlConnection.setRequestProperty("password", this.shopData.getAccessToken());
                    urlConnection.setRequestProperty("cache-control", "no-cache");
                }
                InputStream is;
                try{
                    is = urlConnection.getInputStream();
                }catch (IOException e){
                    is = ((HttpURLConnection) urlConnection).getErrorStream();
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(br);
                json = new JSONObject(jsonText);
                is.close();
                String name = json.getJSONObject("shop").getString("name");
            }catch (IOException | NullPointerException | JSONException e) {
                if(! (e instanceof JSONException))
                    e.printStackTrace();
                json = this.readJsonFromShopifyStore(urlToQuery);
        }
		return json;
	}
*/

    public JSONObject readJsonFromUrl(String urlToQuery){
        JSONObject json = new JSONObject();
        try{
            URL url = new URL(urlToQuery);
            URLConnection urlConnection = url.openConnection();
            if(this.shopData.isAccessTokenRequired()){
                System.out.println("Attempting second time for access token to get authorization needed : "+this.shopData.getAccessToken());
                /**
                 *    X -Shopify-Access-Token doesn't work, so password is used as key for passing acess token in header
                 */
                urlConnection.setRequestProperty("X-Shopify-Access-Token", this.shopData.getAccessToken());
                //  urlConnection.setRequestProperty("password", this.shopData.getAccessToken());
                urlConnection.setRequestProperty("cache-control", "no-cache");
            }
            InputStream is;
            try{
                is = urlConnection.getInputStream();
            }catch (IOException e){
                is = ((HttpURLConnection) urlConnection).getErrorStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(br);
            json = new JSONObject(jsonText);
            is.close();
        }catch (IOException |NullPointerException  e) {
            e.printStackTrace();
        }
        return json;
    }


    private int deleteRequest(String urlToQuery, String rawdata){
        int statusCode = 0;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(urlToQuery);
            httpURLConnection =(HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("cache-control", "no-cache");
            httpURLConnection.setRequestMethod("DELETE");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            if(this.shopData.isAccessTokenRequired()){
                //  X -Shopify-Access-Token is used as key for passing acess token in header
                httpURLConnection.setRequestProperty("X-Shopify-Access-Token", this.shopData.getAccessToken());
            }
            statusCode = httpURLConnection.getResponseCode();
        }catch (IOException |NullPointerException  e) {
            e.printStackTrace();
        }finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return statusCode;
	}

    public static String readAll(Reader br)
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		try {
			while ((cp = br.read()) != -1) {
				sb.append((char) cp);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		return sb.toString();
	}
}
