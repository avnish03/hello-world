package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.restapi.MainController;
import com.ebizon.appify.utils.EmailUtils;
import com.ebizon.appify.utils.SecurityUtils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResetPassword {
	private static final String emailTemplate = "webapps/AppifyCartAdmin/emailTemplate/resetPassword.html";
	private String resetId;
	private String password;

	public ResetPassword(){
		//Empty
	}

	public void setResetId(String resetId) {
		this.resetId = resetId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String save() {
		Document resetCheckDoc = new Document("_id", new ObjectId(this.resetId));
		FindIterable<Document> iterable = MongoDBHandler.find("passwordReset", resetCheckDoc);

		JSONObject jsonObj = new JSONObject();

		Document resetData = iterable.first();
		//Check time
		if (resetData != null){
			int used = resetData.getInteger("used");
			if (used == 0){
				String email = resetData.getString("email");
				Document filterDoc = new Document("email", email);
				Document newDoc = new Document("password", SecurityUtils.encryptWithMD5(this.password));

				UpdateResult result = MongoDBHandler.updateOne("users", filterDoc, newDoc);

				if (result.getMatchedCount() > 0){
					Document resetDoc = new Document("used", 1);
					MongoDBHandler.updateOne("passwordReset", resetCheckDoc, resetDoc);
					jsonObj.put("status", 1);
					jsonObj.put("email", email);
					jsonObj.put("message", "Password reset success");
				}else{
					jsonObj.put("status", 0);
					jsonObj.put("message", "Password reset failed");
				}
			}else{
				jsonObj.put("status", 0);
				jsonObj.put("message",  "Reset link expired");
			}
		}else{
			jsonObj.put("status", 0);
			jsonObj.put("message",  "Password reset failed");
		}

		return jsonObj.toString();
	}

	public static String resetPassword(String email){
		FindIterable<Document> iterable = MongoDBHandler.find("users", new Document("email", email));

		Boolean isValid = iterable.first() != null;

		JSONObject jsonObj = new JSONObject();

		if (isValid){
			Document userDoc = iterable.first();
			
			Document document = new Document("email", email)
					.append("used", 0);

			MongoDBHandler.insertOne("passwordReset", document);

			String id = document.getObjectId("_id").toString();
			sendResetPasswordEmail(id, userDoc.getString("fullname"), email);
			jsonObj.put("status", 1);
			jsonObj.put("message", "Reset passowrd success");
		}else{
			jsonObj.put("status", 0);
			jsonObj.put("message", "Reset passowrd failed");
		}

		return jsonObj.toString();
	}

	public static boolean checkValidId(String restId) {
		Document resetCheckDoc = new Document("_id", new ObjectId(restId));
		FindIterable<Document> iterable = MongoDBHandler.find("passwordReset", resetCheckDoc);

		boolean isValid = false;
		Document document = iterable.first();

		if(document != null){
			int used = document.getInteger("used");
			isValid = used == 0;
		}

		return isValid;
	}

	private static void sendResetPasswordEmail(String id, String name, String email) {
		//String url = "http://app.appifycart.com/rest/admin/resetPassword/" + id;
		String url = MainController.getBaseURLAuthority()+"/rest/admin/resetPassword/" + id;
		String subject = "Reset Password";

		Map<String, String> input = new HashMap<>();
		input.put("name", name);
		input.put("action_url", url);
		
		File catalinaBase = new File( System.getProperty( "catalina.base" ) ).getAbsoluteFile();
		File emailTempl = new File( catalinaBase, ResetPassword.emailTemplate);
		
		String filePath = emailTempl.getAbsolutePath();
		String htmlText = EmailUtils.readEmailFromHtml(filePath, input);
		
		EmailUtils.sendEmail(email, subject, htmlText);
	}

/*	below code added by avnish	*/

	/**
	 *
	 * @return
	 */
	public String resetSave(String user,String password) {
	    Document resetCheckDoc = new Document("_id", "00000");

		JSONObject jsonObj = new JSONObject();

				String email = user;
				Document filterDoc = new Document("email", email);
				Document newDoc = new Document("password", SecurityUtils.encryptWithMD5(password));

				UpdateResult result = MongoDBHandler.updateOne("users", filterDoc, newDoc);

				if (result.getMatchedCount() > 0){
					Document resetDoc = new Document("used", 1);
								resetDoc.append("reset mode","manually");
					MongoDBHandler.updateOne("passwordReset", resetCheckDoc, resetDoc);
					jsonObj.put("status", 1);
					jsonObj.put("email", email);
					jsonObj.put("message", "Password reset success");

				}else{
					jsonObj.put("status", 0);
					jsonObj.put("message", "Password reset failed");
				}

		return jsonObj.toString();
	}
/*	code added by avnish ends here	*/
}
