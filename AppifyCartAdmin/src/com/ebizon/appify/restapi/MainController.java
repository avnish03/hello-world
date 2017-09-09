package com.ebizon.appify.restapi;

import com.ebizon.appify.builder.BaseBuilder;
import com.ebizon.appify.data.*;
import com.ebizon.appify.scheduledjob.*;
import com.ebizon.appify.utils.*;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin")
public class MainController {
	static @Context UriInfo uriInfo;

	// returns the UrlAuthority
	public static String getBaseURLAuthority() {
		String protocol = uriInfo.getBaseUri().getScheme();
		String baseURLAuthority = protocol+"://"+uriInfo.getAbsolutePath().getAuthority();
		return baseURLAuthority;
	}

	@Path("/register.json")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postRegister(Register register){
		return register.save().toString();
	}

	@POST
	@Path("/formRegister.json")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response postFormRegister(@FormParam("user_email") String email,
									 @FormParam("user_org") String organization) throws URISyntaxException {
		Register register = new Register();
		register.setEmail(email);
		register.setFullname(organization);
		register.setTimezoneName("Not Availble");
		register.setPassword(null);
		register.setSignupmethod("appifycartwebsite");

		JSONObject response = register.save();
		// java.net.URI location = UriBuilder.fromPath("http://app.appifycart.com/admin/login.html").build();
		java.net.URI location = UriBuilder.fromPath(MainController.getBaseURLAuthority()+"/admin/login.html").build();

		int status = response.getInt("status");

		if(status == 1){
			String token = response.getString("accesstoken");
			// UriBuilder uriBuilder = UriBuilder.fromPath("http://app.appifycart.com/admin/AppCreator.html");
			UriBuilder uriBuilder = UriBuilder.fromPath(MainController.getBaseURLAuthority()+"/admin/AppCreator.html");
			uriBuilder.queryParam("accesstoken", token);
			uriBuilder.queryParam("user", email);
			location = uriBuilder.build();
		}

		return Response.temporaryRedirect(location).build();
	}

    @Path("/login.json")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String postLogin(Login login, @Context HttpServletRequest re){
        ClientSideInfo.logClientInfo(re);
        System.out.println("\n"+getBaseURLAuthority());
        return login.check();
    }

	@Path("/forgotPassword.json/{user}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postForgotPassword(@PathParam("user") String user){
		String response = ResetPassword.resetPassword(user);

		return response;
	}

	@Path("/iOSAppData.json")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postIOSAppData(IOSAppData iOSAppData){
		iOSAppData.save();
		JSONObject jsonObject = iOSAppData.create();
        String message = "Successfully Started Creating IOS App";
		String appId = jsonObject.getString("appId");
		String buildId = jsonObject.getString("buildId");
        String response = this.doPostIOSBuildRequest(iOSAppData,appId,buildId);

        return response;
	}

    public String doPostIOSBuildRequest(IOSAppData iosAppData, String appId, String buildId){
        String response = "Process to Creating IOS App ";
        try {

            Authorization authorization = AuthGenerator.getInstance().getAuth(appId, Integer.MAX_VALUE);

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("accountEmail", iosAppData.getAccountEmail());
            jsonRequest.put("osType",iosAppData.getOsType());
            jsonRequest.put("platform",iosAppData.getPlatform());
            jsonRequest.put("appName",iosAppData.getAppName());
            jsonRequest.put("serviceUrl",iosAppData.serviceUrl());
            jsonRequest.put("website",iosAppData.getWebsite());
            jsonRequest.put("authKey",authorization.getAuthKey());
            jsonRequest.put("authSecret",authorization.getAuthSecret());
            jsonRequest.put("apiKey",iosAppData.getApiKey());
            jsonRequest.put("apiPassword",iosAppData.getApiPassword());
            // jsonRequest.put("shopifyAccessToken",iosAppData.getShopifyAccessToken());	// sending shopify accesstoken
            jsonRequest.put("appIcon",iosAppData.getAppIcon());
            jsonRequest.put("appSplash",iosAppData.getAppSplash());
            jsonRequest.put("bundleId",iosAppData.getBundleId());
            jsonRequest.put("buildType",iosAppData.getBuildType());
            jsonRequest.put("certificate",iosAppData.getCertificate());
            jsonRequest.put("certificatePassword",iosAppData.getCertificatePassword());
            jsonRequest.put("appLanguage",iosAppData.getAppLanguage());
            jsonRequest.put("provisioningProfile",iosAppData.getProvisioningProfile());
            jsonRequest.put("appId",appId);
            jsonRequest.put("buildId",buildId);

            long buildVersion = iosAppData.getBuildNumber();
            jsonRequest.put("buildVersion",Long.toString(buildVersion));

            String postMessage = jsonRequest.toString();
			JSONObject jsonResponse = null;
            String urlToQueryRegisterCustomer = Constants.MacCloudServer+"/AppifyIOSBuild/rest/admin/iOSAppDataBuild.json";
            try {
				jsonResponse = PostJsonData.getPostDataResponse(urlToQueryRegisterCustomer, postMessage);
			} catch (JSONException e){
            	e.printStackTrace();
			}

            System.out.println("JSON RESPONSE FROM MAC CLOUD"+jsonResponse.toString());
            //response = response.concat(jsonResponse.getString("status"));
            response = jsonResponse.toString();

            String content = "PostMessage : "+postMessage+"     \n\n response from macCloud : "+response;
            BaseBuilder.sendIOSBuildCreationMail(content);

        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return response;
    }
    @GET
    @Path("/iOSBuildAcknowledge.json/")
    @Produces(MediaType.APPLICATION_JSON)
    public String iOSBuildAcknowledge(){

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String status = queryParams.getFirst("status");
        String user = queryParams.getFirst("user");
        String fileName = queryParams.getFirst("fileName");
        String filePath = queryParams.getFirst("filePath");
        String ostype = queryParams.getFirst("ostype");
        String appName = queryParams.getFirst("appName");
        String name = queryParams.getFirst("name");
        String website = queryParams.getFirst("website");
        String buildId = queryParams.getFirst("buildId");

        try {

            user = new String(Base64.getDecoder().decode(user.getBytes("utf-8")));
            fileName = new String(Base64.getDecoder().decode(fileName.getBytes("utf-8")));
            filePath = new String(Base64.getDecoder().decode(filePath.getBytes("utf-8")));
            ostype = new String(Base64.getDecoder().decode(ostype.getBytes("utf-8")));
            appName = new String(Base64.getDecoder().decode(appName.getBytes("utf-8")));
            name = new String(Base64.getDecoder().decode(name.getBytes("utf-8")));
            website = new String(Base64.getDecoder().decode(website.getBytes("utf-8")));
            buildId = new String(Base64.getDecoder().decode(buildId.getBytes("utf-8")));
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BaseBuilder baseBuilder = new BaseBuilder(user);

        if(status.equals("success")){
            baseBuilder.sendBuildAsEmail(fileName, user, ostype, appName, name);
            baseBuilder.sendBuildNotification(fileName, user, ostype, appName, name, website);
            System.out.println(String.format("Sent mail for build with expected path %s succeed", filePath));
        }else{
            baseBuilder.sendFailBuildAsEmail(fileName, user, ostype, appName, name);
            baseBuilder.sendBuildFailedNotification(fileName, user, ostype, appName, name, buildId ,website);
            System.out.println(String.format("Sent mail for build with expected path %s failed", filePath));
        }
        return new JSONObject().put("message","acknowlegdement get successfully").toString();
    }


    @Path("/androidAppData.json")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postAndroidAppData(AndroidAppData androidAppData){
		System.out.println("Inside androidappdata.json");
		androidAppData.save();
		String msg = androidAppData.create();

		return msg;
	}

	@GET
	@Path("/appData.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppData(@PathParam("user") String user){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "Login success"); //Add check
		List<JSONObject> apps = Login.loadApps(user);
		List<JSONObject> payments = Payment.loadPayments(user);

		jsonObj.put("apps", apps);
		jsonObj.put("payments", payments);

		return jsonObj.toString();
	}

	@Path("/addAppPaymentMethod.json")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postAppPaymentMethod(AppPaymentMethod appPaymentMethod){
		appPaymentMethod.save();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "success"); //Add check

		return jsonObj.toString();
	}

	@GET
	@Path("/appPaymentMethods.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppPaymentMethods(@PathParam("user") String user){
		JSONObject jsonObj = new JSONObject();
		List<JSONObject> paymentMethods = AppPaymentMethod.loadPaymentMethods(user);

		jsonObj.put("paymentMethods", paymentMethods);

		return jsonObj.toString();
	}

	@GET
	@Path("/shopifyAccessToken.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getshopifyAccessToken(@PathParam("user") String user){
		return ShopData.fetchAccessToken(user);
	}

	@GET
	@Path("/isSiteAccessible.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getSignUpMethod(@PathParam("user") String user){
		JSONObject jsonObject = new JSONObject();
		String signupmethod = Login.getSignUpMethod(user);
		jsonObject.put("signupmethod",signupmethod);
		jsonObject.put("status",1);
		return  jsonObject;
	}

	@Path("/addAppSocialLoginMethod.json")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postAppSocialLoginMethod(AppSocialLoginMethod appSocialLoginMethod){
		appSocialLoginMethod.save();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "success"); //Add check

		return jsonObj.toString();
	}

	@GET
	@Path("/appSocialLoginMethods.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppSocialLoginMethods(@PathParam("user") String user){
		JSONObject jsonObj = new JSONObject();
		List<JSONObject> socialLoginMethods = AppSocialLoginMethod.loadSocialLoginMethods(user);

		jsonObj.put("socialLoginMethods", socialLoginMethods);

		return jsonObj.toString();
	}

	@POST
	@Path("/updateCategoryMaps.json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateAppCategoryMap(CategoryMapData categoryMapData){
		System.out.println("updateAppCategoryMap");
		categoryMapData.update();

		JSONObject resultJSON = new JSONObject();
		resultJSON.put("status", 1);
		resultJSON.put("message", "success"); //Add check

		return resultJSON.toString();
	}

	@POST
	@Path("/updateAppHomepageSections.json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateAppHomepageSections(MapMobileHomePageCategories categoryMapData){
		System.out.println("updateAppCategoryMap");
		categoryMapData.updateAppHomepageSections();

		JSONObject resultJSON = new JSONObject();
		resultJSON.put("status", 1);
		resultJSON.put("message", "success"); //Add check

		return resultJSON.toString();
	}

	@GET
	@Path("/appCategoryMaps.json/{user}/{ostype}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppCategoryMap(@PathParam("user") String user, @PathParam("ostype") String osType){
		JSONObject jsonObj = new JSONObject();
		String appId = Utils.getAppId(user, osType);
		List<JSONObject> categoryMapData = CategoryMapData.getCategoryMapData(appId);

		jsonObj.put("categoryMaps", categoryMapData);

		return jsonObj.toString();
	}

	@GET
	@Path("/appHomepageSections.json/{user}/{ostype}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppHomepageSectionsMap(@PathParam("user") String user, @PathParam("ostype") String osType){
		JSONObject jsonObj = new JSONObject();
		String appId = Utils.getAppId(user, osType);
        MapMobileHomePageCategories mapMobileHomePageCategories = new MapMobileHomePageCategories();
		JSONObject categoryMapData = mapMobileHomePageCategories.getAppHomepageSectionsMapping(appId);

		jsonObj.put("result", categoryMapData);

		return jsonObj.toString();
	}


 	@POST
	@Path("/bannerLogo.json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String addBannerLogo(BannerLogo bannerLogo){
		bannerLogo.save();

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "success"); //Add check

		return jsonObj.toString();
	}

	@GET
	@Path("/appBannerLogos.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppBannerLogos(@PathParam("user") String user){
		JSONObject jsonObj = new JSONObject();
		List<JSONObject> bannerLogos = BannerLogo.loadBannerLogo(user);

		jsonObj.put("bannerLogos", bannerLogos);

		return jsonObj.toString();
	}

	@POST
	@Path("/pushNotificationConfig.json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String addPushNotificationConfig(PushNotificationConfigData pushNotificationConfigData){
		pushNotificationConfigData.save();

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "success"); //Add check

		return jsonObj.toString();
	}

	@POST
	@Path("/pushNotification.json")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String sendPushNotification(PushNotification pushNotification){
		pushNotification.saveAndPush();

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "success"); //Add check

		return jsonObj.toString();
	}

	@GET
	@Path("/appdownload.json/{user}/{ostype}/{filename}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadBuildFile(@PathParam("user") String user, @PathParam("ostype") String ostype, @PathParam("filename") String filename){
		StreamingOutput fileStream =  new StreamingOutput()
		{
			@Override
			public void write(java.io.OutputStream output) throws IOException, WebApplicationException
			{
				try
				{
					String filePath = BaseBuilder.getBuildPath(user, ostype) + "/" + filename;
					java.nio.file.Path path = Paths.get(filePath);
					byte[] data = Files.readAllBytes(path);
					output.write(data);
					output.flush();
				}
				catch (Exception e)
				{
					throw new WebApplicationException();
				}
			}
		};
		return Response
				.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
				.header("content-disposition","attachment; filename =" + filename)
				.build();

	}

	private static String getFileExtension(String fileName) {
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".")+1);
		else return "";
	}

	@POST
	@Path("/fileupload.json/{user}/{ostype}/{filetype}/{index}")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public String uploadFile(@PathParam("user") String user, @PathParam("ostype") String ostype, @PathParam("filetype") String filetype, @PathParam("index") String index,
							 @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		String extension = getFileExtension(fileMetaData.getFileName());

		UploadFileData uploadFileData = new UploadFileData(user, ostype, filetype, index, extension);
		String filePath = uploadFileData.save(fileInputStream);
		if(ostype.equalsIgnoreCase("ios")){
//            int temp = filePath.lastIndexOf("file_");
//            String path = filePath.substring(0, temp);
//            String fileName = filePath.substring(temp, filePath.length());
//            // execute to transfer files from one server to another
//            System.out.println("PATHHHHHHHH:"+path);
//            System.out.println("FILEEEEEEEE:"+fileName);
//            boolean isTransfer = TransferFilesToIOSServer.copyFiles(path,fileName);
//            System.out.println("result : "+isTransfer);
			String uploadResponse = uploadFileData.uploadToIOSServer(filePath);
			System.out.println("\n\n\n\n@@@@@@upload to ios server reponse : "+uploadResponse);
		}

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", 1);
		jsonObj.put("message", "File uploaded successfully");
		jsonObj.put("file", filePath);

		return jsonObj.toString();
	}
	@GET
	@Path("/return.json")
	@Produces(MediaType.TEXT_HTML)
	public Response getReturnData(@Context UriInfo uriInfo) throws URISyntaxException{
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String accountEmail = queryParams.getFirst("accountemail");
		String orderNumber = queryParams.getFirst("order_number");
		String productID = queryParams.getFirst("li_0_product_id");
		String invoiceID = queryParams.getFirst("invoice_id");
		String currencyCode = queryParams.getFirst("currency_code");
		String amount = queryParams.getFirst("total");
		String paymentMethod = queryParams.getFirst("pay_method");

		Payment payment = new Payment(accountEmail, productID, orderNumber, invoiceID, currencyCode, amount, paymentMethod);
		payment.save();

		Logger logger = Logger.getLogger("Payment");
		logger.log(Level.INFO, String.format("queryParamsfor this is %s", queryParams.toString()));

		java.net.URI location = new java.net.URI("../admin/DevelopmentAppCreator.html");  // before /admin/AppifyMain.html"

		return Response.temporaryRedirect(location).build();
	}

    @GET
    @Path("/payment-callback/")
    @Produces(MediaType.TEXT_HTML)
    public Response getCallBackReturnData(@Context UriInfo uriInfo) throws URISyntaxException{
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        //?account={{account_code}}&plan={{plan_code}}
        String accountEmail = queryParams.getFirst("account");
        String plan = queryParams.getFirst("plan");

        PaymentRecurly payment = new PaymentRecurly(accountEmail, plan);
        payment.save();

        Logger logger = Logger.getLogger("Payment");
        logger.log(Level.INFO, String.format("queryParamsfor this Recurly Payment is %s", queryParams.toString()));

        java.net.URI location = new java.net.URI("../admin/DevelopmentAppCreator.html");  // before /admin/AppifyMain.html"

        return Response.temporaryRedirect(location).build();
    }


    @GET
	@Path("/resetPassword/{resetId}")
	@Produces(MediaType.TEXT_HTML)
	public Response getResetPasswordURL(@PathParam("resetId") String resetId) throws URISyntaxException{
		if (ResetPassword.checkValidId(resetId)){
			java.net.URI location = new java.net.URI("../admin/ResetPassword.html");
			URIBuilder uriBuilder = new URIBuilder(location);
			uriBuilder.addParameter("restId", resetId);

			return Response.temporaryRedirect(uriBuilder.build()).build();
		}else{
			return Response.status(200).
					entity("This link is expired, please reset again").
					type("text/plain").build();
		}
	}

	@Path("/resetPassword.json")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String postResetPassword(ResetPassword resetPassword){
		return resetPassword.save();
	}

/*   from here codes added by avnish       */

	/**
	 *
	 * @param user
	 * @return
	 */
	@GET
	@Path("/appIconSplash.json/{user}/{ostype}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppIconSplash(@PathParam("user") String user,@PathParam("ostype") String ostype){
		System.out.println("@vnish app icon splash : user->"+user+" : ostype->"+ostype);
		JSONObject jsonObj = new JSONObject();
		List<JSONObject> iconSplash = UploadFileData.loadIconSplash(user,ostype);
		jsonObj.put("iconSplash", iconSplash);
		return jsonObj.toString();
	}

	/**
	 *
	 * @param user
	 * @return json - the customer details
	 * @throws IOException
	 */
	@GET
	@Path("/profile.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserProfile(@PathParam("user") String user) throws IOException{
		JSONObject jsonObj = new JSONObject();
		JSONObject userDetails = Login.getUserDetails(user);
		jsonObj.put("userDetails", userDetails);
		return Response.ok(jsonObj.toString()).build();

	}

	/**
	 *
	 * @param user
	 * @return json - customers app details
	 * @throws IOException
	 */
	@GET
	@Path("/myapps.json/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomersAppData(@PathParam("user") String user) throws IOException{
		JSONObject jsonObj = new JSONObject();
		JSONObject userDetails = Login.getUserDetails(user);
		jsonObj.put("userDetails", userDetails);
		return Response.ok(jsonObj.toString()).build();

	}

	/**
	 *
	 * @param user
	 * @param password
	 * @param resetPassword
	 * @return
	 */
	@Path("/resetSavePassword.json/{user}/{password}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String resetPassword(@PathParam("user") String user,@PathParam("password") String password,ResetPassword resetPassword){
		return resetPassword.resetSave(user,password);
	}

	@Path("/updateCMSPages.json/")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String saveCMS(CMSPages cmsPages){
		JSONObject obj = cmsPages.saveData();

		return obj.toString();
	}

	@Path("/deleteCMSPages.json/")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Boolean deleteCMS(CMSPages cmsPages){
		Boolean obj = cmsPages.deleteCMS();

		return obj;
	}

	@Path("/fetchCMSPages.json/{user}/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchCMS(@PathParam("user") String user){
		CMSPages cmsPages = new CMSPages();
		JSONObject jsonObj = cmsPages.fetchData(user);
		System.out.print("CMS Response :\n"+jsonObj.toString());
		cmsPages = null;
		return jsonObj.toString();
	}

	@Path("/dashboard.json/{user}/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchDashboardContent(@PathParam("user") String user){
		Dashboard dboard = new Dashboard();
		JSONObject jsonObj = dboard.fetchData(user);
		System.out.print("CMS Response :\n"+jsonObj.toString());
		return jsonObj.toString();
	}

	@GET
	@Path("/allBuildData.json/{user}/{osType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getallBuildData(@PathParam("user") String user, @PathParam("osType") String osType){
		JSONObject jsonObj = new JSONObject();
		AppsManager appsManager = new AppsManager(user,osType);
		JSONObject allBuildApps = appsManager.getAllBuilds();
		jsonObj.put("result", allBuildApps);
		return Response.ok(jsonObj.toString()).build();
	}

	@GET
	@Path("/deleteOneBuild.json/{user}/{osType}/{docId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteBuildData(@PathParam("user") String user, @PathParam("osType") String osType, @PathParam("docId") String docId){
		JSONObject jsonObj = new JSONObject();
		String appId = Utils.getAppId(user,osType);
		AppsManager appsManager = new AppsManager(user,osType);
		boolean res = appsManager.deleteOneBuild(appId,docId);
		int status ;
		String message;
		if(res == true){
			status = 1;
			message = "deleted successfully";
		}else{
			status = 0;
			message = "error occurred";
		}
		jsonObj.put("status", status);
		jsonObj.put("message", message);
		return Response.ok(jsonObj.toString()).build();
	}

	@GET
	@Path("/deleteImage.json/{user}/{image}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteImage(@PathParam("user") String user, @PathParam("image") String image){
		JSONObject jsonObj = new JSONObject();
		BannerLogo.deleteImage(user,image);

		System.out.println("email : "+user);
		System.out.println("image : "+image);

		boolean res = true;
		int status ;
		String message;
		if(res == true){
			status = 1;
			message = "deleted successfully";
		}else{
			status = 0;
			message = "error occurred";
		}
		jsonObj.put("status", status);
		jsonObj.put("message", message);
		return Response.ok(jsonObj.toString()).build();
	}


    @GET
    @Path("/updateShopifyBestSeller/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateShopifyBestSeller(@PathParam("appId") String appId){
        JSONObject jsonObj = new JSONObject();
        ShopifyBestSellers bestSellers = new ShopifyBestSellers(appId);
        bestSellers.executeDuplicate(appId);
        jsonObj.put("message", "updating BestSeller");
        return jsonObj.toString();
    }

    @GET
    @Path("/updateShopifyNewProducts/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateShopifyNewProducts(@PathParam("appId") String appId){
        JSONObject jsonObj = new JSONObject();
        ShopifyNewProducts newProducts = new ShopifyNewProducts(appId);
        newProducts.executeDuplicate(appId);
        jsonObj.put("message", "updating NewProducts");
        return jsonObj.toString();
    }

    @GET
    @Path("/updateShopifyProducts/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateShopifyProducts(@PathParam("appId") String appId){
        JSONObject jsonObj = new JSONObject();
        ShopifyProducts shopifyProducts = new ShopifyProducts(appId);
        shopifyProducts.executeDuplicate(appId);
        jsonObj.put("message", "updating NewProducts");
        return jsonObj.toString();
    }

    @GET
    @Path("/updateShopifyOrders/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateShopifyOrders(@PathParam("appId") String appId){
        JSONObject jsonObj = new JSONObject();
        ShopifyOrders shopifyOrders = new ShopifyOrders(appId);
        shopifyOrders.executeDuplicate(appId);
        jsonObj.put("message", "updating Orders");
        return jsonObj.toString();
    }

    @GET
    @Path("/instantDataLoader/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String instantDataLoader(@PathParam("appId") String appId){
        JSONObject jsonObj = new JSONObject();
        InstantDataLoader.getInstance().addToQueue(appId);
        jsonObj.put("message", "updating Orders");
        return jsonObj.toString();
    }

	@Path("/validateApi.json/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String validateApi(@Context UriInfo uriInfo, @Context HttpServletRequest re) throws URISyntaxException{
        Logger logger = Logger.getLogger("validateApi");
        ClientSideInfo.logClientInfo(re);
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String storeUrl = queryParams.getFirst("storeUrl");
        String apiKey = queryParams.getFirst("apiKey");
        String apiPassword = queryParams.getFirst("apiPassword");
        logger.log(Level.INFO, "verifying the store url:"+storeUrl+" apikey:"+apiKey+" & apisecret:"+apiPassword);
		System.out.println("ip : "+re.getRemoteAddr());
		JSONObject jsonObject = null;
		JSONObject response = new JSONObject();
        response.put("status", "failed");


        if(apiKey.equalsIgnoreCase("shopify_app_user")&&apiPassword.equalsIgnoreCase("shopify_app_user")){
            response.put("status", "success");
            response.put("message", "validation skipped for shopify app user");
        }
		String urlToQuery = storeUrl+"/admin/shop.json";

        try {
            URL url = new URL (urlToQuery);
            String str = apiKey+":"+apiPassword;
            String encoding = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader(content));
            String jsonText = WebHandler.readAll(in);
            jsonObject = new JSONObject(jsonText);
        } catch(Exception e) {
            e.printStackTrace();
        }
        String shop = null;
        try {
            if (!jsonObject.isNull("shop"))
                response.put("status", "success");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        System.out.println(response.toString());
        return response.toString();
	}

    /*   codes added by avnish ends here      */

}
