package com.ebizon.appify.data;

import org.bson.Document;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.client.result.UpdateResult;

public class PushNotificationConfigData {
	private String accountEmail;
	private String osType;
	private String certificate;
	private String passphrase;
	private String certificateType;
	
	public PushNotificationConfigData(){
		//Empty
	}

	public void setAccountEmail(String accountEmail){
		this.accountEmail = accountEmail;
	}
	
	public void setOsType(String osType){
		this.osType = osType;
	}
	
	public void setCertificateType(String certificateType){
		this.certificateType = certificateType;
	}
	
	public void setCertificate(String certificate){
		this.certificate = certificate;
	}
	
	public void setPassphrase(String passphrase){
		this.passphrase = passphrase;
	}
		
	public void save(){		
		String appId = IOSAppData.getAppId(this.accountEmail, this.osType);

		if(!appId.isEmpty()){
			Document checkDoc = new Document("appId", appId);

			Document newDoc = new Document("appId", appId)
					.append("osType", this.osType)
					.append("certificateType", this.certificateType)
					.append("certificate", this.certificate)
					.append("passphrase", this.passphrase);

			UpdateResult result = MongoDBHandler.updateOne("appPushNotificationConfig", checkDoc, newDoc);

			if (result.getMatchedCount() == 0){
				MongoDBHandler.insertOne("appPushNotificationConfig", newDoc);
			}
		}
	}
}
