package com.ebizon.appify.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

/**
 * Created by avnish on 25/5/17.
 */

public class PostJsonData {


    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";

    /**
     * method used for creating new customer, for registration of a new user
     *
     * Send a POST request to itcuties.com
     * @param urlTOQuery
     * @throws IOException
     */
    public static JSONObject getPostDataResponse(String urlTOQuery, String rawData) throws IOException
    {

        // This is the data that is going to be send to itcuties.com via POST request
        String postData = rawData ;
        String jsonText;
        JSONObject json ;
        BufferedReader br ;
        // Connect
        URL url = new URL(urlTOQuery);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");                                                                                                      //Way of submitting data(e.g. GET, POST)
        //	Setting content type-JSON
        connection.setRequestProperty("Content-Type", "application/json");
        //	Setting length of the request body
        connection.setRequestProperty("Content-Length", Integer.toString(postData.getBytes().length));


        // Write data
        DataOutputStream dataoutput = new DataOutputStream(connection.getOutputStream());
        dataoutput.write(postData.getBytes("UTF-8"));
        dataoutput.flush();

        // handling http respose error 422 - Unprocessable Entity

        int status = connection.getResponseCode();
        if(status == 422 || status == 403)
        {
            // System.out.println("error 422 occured");
            br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            jsonText = readAll(br);
            json = new JSONObject().put("error",jsonText);
        }
        else
        {
            //Retrieving response from the webservice
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            jsonText = readAll(br);
            json = new JSONObject(jsonText);

        }


        // Close streams
        br.close();
        dataoutput.close();

        return json ;
    }

    /**
     * method used for creating new customer, for registration of a new user
     *
     * Send a POST request to itcuties.com
     *
     * @param urlTOQuery
     * @param rawData
     * @param accessToken
     * @return
     * @throws IOException
     */
    public static JSONObject getPostDataResponseWithAccessToken(String urlTOQuery, String rawData, String accessToken) throws IOException
    {
        System.out.println("--------------------------------------------------");
        System.out.println("@vnish : getPostDataResponseWithAccessToken ");
        System.out.println("urlTOQuery : "+urlTOQuery);
        System.out.println("rawData : "+rawData);
        System.out.println("accessToken : "+accessToken);
        System.out.println("--------------------------------------------------");

        // This is the data that is going to be send to itcuties.com via POST request
        String postData = rawData ;
        String jsonText;
        JSONObject json = null ;
        BufferedReader br = null;
        DataOutputStream dataoutput = null;
        try {
            // Connect
            URL url = new URL(urlTOQuery);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("User-Agent", USER_AGENT);
            //Way of submitting data(e.g. GET, POST)
            //	Setting content type-JSON
            connection.setRequestProperty("Content-Type", "application/json");
            // set accesstoken
            connection.setRequestProperty("X-Shopify-Access-Token", accessToken);

//            	Setting length of the request body
//            connection.setRequestProperty("Content-Length", Integer.toString(postData.getBytes().length));
            System.out.println("Header X-Shopify-Access-Token"+ connection.getRequestProperty("X-Shopify-Access-Token"));

            // Write data
            dataoutput = new DataOutputStream(connection.getOutputStream());
            dataoutput.write(postData.getBytes("UTF-8"));
            dataoutput.flush();

            Map<String, List<String>> hdrs = connection.getHeaderFields();
            Set<String> hdrKeys = hdrs.keySet();

            // handling http respose error 422 - Unprocessable Entity

            int status = connection.getResponseCode();
            System.out.println("----------->status : " + status);
            if (status == 422 | status == 403) {
                // System.out.println("error 422 occured");
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                jsonText = readAll(br);
                json = new JSONObject().put("error",jsonText);
            } else {
                //Retrieving response from the webservice
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                jsonText = readAll(br);
                json = new JSONObject(jsonText);
            }
            System.out.println("\n\n\n Response from PostData : "+json);
        }catch (Exception e){
            System.out.println("\n\nexception occurred");
            System.out.println(e.getClass());
            e.printStackTrace();
           // json = getPostDataResponseWithAccessTokenTwice(urlTOQuery, rawData, accessToken);
        }

        // Close streams
        br.close();
        dataoutput.close();

        return json ;
    }

    /**
     * method used for creating new customer, for registration of a new user
     *
     * @param urlTOQuery
     * @param rawData
     * @param accessToken
     * @return
     * @throws IOException
     */
    public static JSONObject getPostDataResponseWithAccessTokenTwice(String urlTOQuery, String rawData, String accessToken) throws IOException
    {
        System.out.println("\n\n\nINSIDE TRIAL 2");

        // This is the data that is going to be send to itcuties.com via POST request
        String postData = rawData ;
        String jsonText;
        JSONObject json = null ;
        BufferedReader br = null;
        DataOutputStream dataoutput = null;
        // Connect
        try {
            URL url = new URL(urlTOQuery);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");                                                                                                      //Way of submitting data(e.g. GET, POST)
            //	Setting content type-JSON
            connection.addRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Content-Type", "application/json");
            // set accesstoken
            connection.setRequestProperty("password", accessToken);

            //	Setting length of the request body
            connection.setRequestProperty("Content-Length", Integer.toString(postData.getBytes().length));


            // Write data
            dataoutput = new DataOutputStream(connection.getOutputStream());
            dataoutput.write(postData.getBytes("UTF-8"));
            dataoutput.flush();

            // handling http respose error 422 - Unprocessable Entity

            int status = connection.getResponseCode();
            System.out.println("----------->status : " + status);
            if (status == 422) {
                // System.out.println("error 422 occured");
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                jsonText = readAll(br);
                json = new JSONObject(jsonText);
            } else {
                //Retrieving response from the webservice
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                jsonText = readAll(br);
                json = new JSONObject(jsonText);

            }

        }catch (Exception e){
            System.out.println("INSIDE TRIAL @ EXCDEPTION CLASS : "+e.getClass());
        }
        // Close streams
        br.close();
        dataoutput.close();

        return json ;
    }

    /*
	   *  method reads all data and return as string
	   * - helper function for readJSonFromUrl()
	   * */
    private static String readAll(Reader rd) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


}
