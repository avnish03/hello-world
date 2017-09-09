package com.ebizon.appify.builder;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.restapi.MainController;
import com.ebizon.appify.utils.EmailUtils;
import com.ebizon.appify.utils.TransferFilesToIOSServer;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import sun.applet.Main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class BaseBuilder implements Callable<Void>{
	private static final String baseServiceURL = MainController.getBaseURLAuthority()+"/%s/rest/%s/api.json"; //1st arg = War name of storetype, 2nd arg = app id
	private static final String emailTemplate = "webapps/AppifyCartAdmin/emailTemplate/build.html";
	private static final String emailFailedTemplate = "webapps/AppifyCartAdmin/emailTemplate/buildFailUser.html";
	private static final String buildNotifEmailTemplate = "webapps/AppifyCartAdmin/emailTemplate/buildSuccess.html";
	private static final String buildFailedNotifEmailTemplate = "webapps/AppifyCartAdmin/emailTemplate/buildFailed.html";
//	public static final String[] notifEmails = { "sudeepgoyal@ebizontek.com" , "manish.kumar@ebizontek.com", "rohit.saurav@ebizontek.com","avnish.kumar@ebizontek.com"};
	public static final String[] notifEmails = {"avnish.kumar@ebizontek.com"};
	protected String buildId = "";
	protected String bundleId = "";
	protected String appId = "";
	protected String appName = "";
	protected String user = "";
	protected String ostype = "";
	private String appExt = "";
	private String website = "";

	public static final String     DefaultBundleId = "com.appifycart.demo"  ;
    public static final String     DefaultIOSCertificate = "/AppifyCart/uploadFiles/defaultBuildFiles/EnterpriseInHouseDistCertificate.p12" ;
    public static final String     DefaultIOSProvisonalProfile = "/AppifyCart/uploadFiles/defaultBuildFiles/EnterpriseInHouseDistProv.mobileprovision" ;
    public static final String     DefaultIOSPassphrase = "123456" ;
    public static final String     DefaultAndroidKeystore = "/AppifyCart/uploadFiles/defaultBuildFiles/AndroidMofluid.keystore" ; ;
    public static final String     DefaultAndroidPassphrase = "shashimofluid123" ;
    public static final String     DefaultAndroidAliasName = "moFluid" ;


    public BaseBuilder(String buildId, String osType, String appExt){
		this.buildId = buildId;
		this.ostype = osType;
		this.appExt = appExt;
	}

    public BaseBuilder(String user) {
        this.user = user;
    }

    protected void loadUser(){
		FindIterable<Document> appIterable = MongoDBHandler.find("appData", new Document("_id", new ObjectId(this.appId)));

		if (appIterable.first() != null){
			Document document = appIterable.first();
			this.user = document.getString("accountemail");
		}

		// get the website for which build will be building
		this.website = getWebsite(this.user);
	}

	protected String getServiceURL(){
        String storeConnector = "ShopifyConnect"; //Add cases for others, only Shopify supported now
        return String.format(BaseBuilder.baseServiceURL, storeConnector, this.appId);
	}

	protected long getBuildNumber(){
		Document document = new Document("appId", this.appId)
				.append("bundleId", this.bundleId);
        long count = MongoDBHandler.count("buildIdData", document);

        return count + 1;
	}

	public static String getBuildPath(String user, String ostype){
		StringBuilder stringBuilder = new StringBuilder(BuilderConfig.getInstance().getBaseDir());
		stringBuilder.append("/");
		stringBuilder.append(user);
		stringBuilder.append("/");
		stringBuilder.append(ostype);
		
		return stringBuilder.toString();
	}
	
	public static String getBuildDataPath(String user, String ostype){
		StringBuilder stringBuilder = new StringBuilder(BaseBuilder.getBuildPath(user, ostype));
		stringBuilder.append("/");
		stringBuilder.append("buildData");
		
		return stringBuilder.toString();
	}
	
	public String getBuildDir(){
		return BaseBuilder.getBuildPath(this.user, this.ostype);
	}
	
	public String getBuildDataDir(){
		return BaseBuilder.getBuildDataPath(this.user, this.ostype);
	}
	
	public void execute(){
		//Empty
	}

	/**
	 * Method will configure mails sending process, which mails should be sent to whom
	 */
	public void sendEmail(){
		String name = "";
		FindIterable<Document> iterable = MongoDBHandler.find("users", new Document("email", user));
				
		
		Document document = iterable.first();
		
		if(document != null){
			name = document.getString("fullname");
		}
		
		String fileName = this.appName + "_" + this.buildId + "." + this.appExt;
		
		String filePath = BaseBuilder.getBuildPath(user, ostype) + "/" + fileName;
		File file = new File(filePath);
		if(file.exists()){
			this.sendBuildAsEmail(fileName, user, ostype, this.appName, name);
			this.sendBuildNotification(fileName, user, ostype, this.appName, name, this.website);
		}else{
			this.sendFailBuildAsEmail(fileName, user, ostype, this.appName, name);
			this.sendBuildFailedNotification(fileName, user, ostype, this.appName, name, this.buildId ,this.website);
			System.out.println(String.format("build with expected path %s failed", filePath));
		}
	}
	
	
	public Void call() {
		try{
			this.execute();
			
			this.sendEmail();

			this.copyAndroidConfig();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}

		return null;
	}

	private void copyAndroidConfig() {
		String fileName = this.appName + "_" + this.buildId ;
		TransferFilesToIOSServer.copyAndroidConfig(fileName);
	}

	/**
	 * Method will send the Notification mail to user for build success
	 *
	 * @param filename
	 * @param email
	 * @param ostype
	 * @param appname
	 * @param fullname
	 */
    public void sendBuildAsEmail(String filename, String email, String ostype, String appname, String fullname){
		String subject = appname + " " + ostype + " build";
		String buildLink = MainController.getBaseURLAuthority()+"/rest/admin/appdownload.json/" + email + "/" + ostype + "/" + filename;


		Map<String, String> input = new HashMap<String, String>();
		input.put("t_name", fullname);
		input.put("t_appname", appname);
		input.put("t_osname", ostype);
		input.put("t_action_url", buildLink);

		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File emailTempl = new File( catalinaBase, BaseBuilder.emailTemplate);

		String filePath = emailTempl.getAbsolutePath();
		String htmlText = EmailUtils.readEmailFromHtml(filePath, input);

		EmailUtils.sendEmail(email, subject, htmlText);
	}

	/**
	 * Method will send the Notification mail to user for build failed
	 *
	 * @param filename
	 * @param email
	 * @param ostype
	 * @param appname
	 * @param fullname
	 */
    public void sendFailBuildAsEmail(String filename, String email, String ostype, String appname, String fullname){
		String subject = appname + " " + ostype + " build";

		Map<String, String> input = new HashMap<String, String>();
		input.put("t_name", fullname);
		input.put("t_appname", appname);
		input.put("t_osname", ostype);

		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File emailTempl = new File( catalinaBase, BaseBuilder.emailFailedTemplate);

		String filePath = emailTempl.getAbsolutePath();
		String htmlText = EmailUtils.readEmailFromHtml(filePath, input);

		EmailUtils.sendEmail(email, subject, htmlText);
	}

	/**
	 * Method will send the Notification mail to Ebizon employees for build success
	 * @param filename
	 * @param email
	 * @param ostype
	 * @param appname
	 * @param fullname
	 * @param website
	 */
    public void sendBuildNotification(String filename, String email, String ostype, String appname, String fullname, String website){
		String subject = String.format("[Success]User %s created %s %s build", email, appname, ostype);
		String buildLink =  MainController.getBaseURLAuthority()+"/rest/admin/appdownload.json/" + email + "/" + ostype + "/" + filename;


		Map<String, String> input = new HashMap<String, String>();
		input.put("t_user", email);
		input.put("t_appname", appname);
		input.put("t_osname", ostype);
		input.put("t_action_url", buildLink);
		input.put("t_website", website);

		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File emailTempl = new File( catalinaBase, BaseBuilder.buildNotifEmailTemplate);

		String filePath = emailTempl.getAbsolutePath();
		String htmlText = EmailUtils.readEmailFromHtml(filePath, input);

//		System.out.println("EMAIL SENDING PAUSED");
		EmailUtils.sendEmail(BaseBuilder.notifEmails, subject, htmlText);
	}

	/**
	 * Method will send the Notification mail to Ebizon employees for build failed
	 *
	 * @param filename
	 * @param email
	 * @param ostype
	 * @param appname
	 * @param fullname
	 * @param buildId
	 */
    public void sendBuildFailedNotification(String filename, String email, String ostype, String appname, String fullname, String buildId, String website){
		String subject = String.format("[Failed]User %s %s %s build failed", email, appname, ostype);

		Map<String, String> input = new HashMap<String, String>();
		input.put("t_user", email);
		input.put("t_appname", appname);
		input.put("t_osname", ostype);
		input.put("t_build_id", buildId);
		input.put("t_website", website);

		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File emailTempl = new File( catalinaBase, BaseBuilder.buildFailedNotifEmailTemplate);

		String filePath = emailTempl.getAbsolutePath();
		String htmlText = EmailUtils.readEmailFromHtml(filePath, input);
//		System.out.println("EMAIL SENDING PAUSED");
		EmailUtils.sendEmail(BaseBuilder.notifEmails, subject, htmlText);
	}

	/**
	 *
	 * @return website for which user requests the app build
	 */
	public String getWebsite(String user) {
		String website = null;
		Document filterDoc = new Document("accountemail", user);
		FindIterable<Document> iterable = MongoDBHandler.find("appData", filterDoc);
		if (iterable.first() != null){
			Document doc = iterable.first();
			website = doc.getString("website");
		}

		return website;
	}

	/**
	 *
	 * @return
	 */
	public HashMap<String, String> generateDemoAppImages(String appName) {
		GenerateDemoAppImages gdai = new GenerateDemoAppImages(this.user);
		return gdai.generate(appName);
	}

	public static void sendIOSBuildCreationMail(String htmlText){
        String[] notifDevEmails = {"avnish.kumar@ebizontek.com"};
        EmailUtils.sendEmail(notifDevEmails, "IOSBUILD request generated", htmlText);
    }

}
