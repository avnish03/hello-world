package com.ebizon.appify.data;

import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.GenericQueue;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import io.netty.util.concurrent.Future;
import org.bson.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class PushNotification implements Callable<Void> {
    private final String fcmURL = "https://fcm.googleapis.com/fcm/send";
    private final String fcmAuthKey = "AAAAbzrxWDo:APA91bFW-iymRjTr1xCHHnO4y5zYJqjo9_9vGtDfNel-WhRo9bQRfSjmjd8CgZH3mSAkfl0RtKxFDS555PCEKL1ypeLd2R6OSjJOk9o5tjqnc6iXVefIa1mD7m92m_AYMcU87362HSVh";
	private String accountEmail;
	private String osType;
	private String pushTitle;
	private String pushText;

	public PushNotification(){
		//Empty
	}

	public void setAccountEmail(String accountEmail){
		this.accountEmail = accountEmail;
	}

	public void setOsType(String osType){
		this.osType = osType;
	}

	public void setPushTitle(String pushTitle) {this.pushTitle = pushTitle;}

	public void setPushText(String pushText){
		this.pushText = pushText;
	}

	public void saveAndPush(){		
		String appId = IOSAppData.getAppId(this.accountEmail, this.osType);
		System.out.println(String.format("Found app %s", appId));
		if(!appId.isEmpty()){
			Document newDoc = new Document("appId", appId)
					.append("osType", this.osType)
                    .append("pushTitle", this.pushTitle)
					.append("pushText", this.pushText);

			MongoDBHandler.insertOne("appPushNotification", newDoc);

			GenericQueue.getInstance().addToQueue(this);
		}
	}

	public Void call() throws Exception {
		if(this.osType.equalsIgnoreCase("ios")) {
			this.sendiOSMessage();
		}else{
			this.sendAndroidMessage();
		}

		return null;
	}

	private void sendAndroidMessage(){
        String appId = IOSAppData.getAppId(this.accountEmail, this.osType);

        System.out.println(String.format("Sending android message for app %s", appId));
        Document filterDoc = new Document("appId", appId);

		FindIterable<Document> iterable = MongoDBHandler.find("appDeviceIds", filterDoc);

		Block<Document> appBlock = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				String registerId = document.getString("registerId");
				try {
                    sendMessageByFCM(registerId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		iterable.forEach(appBlock);
	}

    private void sendMessageByFCM( String registerId){
        try {
            HttpURLConnection httpcon = (HttpURLConnection) ((new URL(fcmURL).openConnection()));
            httpcon.setDoOutput(true);
            httpcon.setRequestProperty("Content-Type", "application/json");
            httpcon.setRequestProperty("Authorization", String.format("key=%s", fcmAuthKey));
            httpcon.setRequestMethod("POST");
            httpcon.connect();

            String notification = String.format("{\"notification\":{\"title\": \"%s\", \"text\": \"%s\", \"sound\": \"default\"}, \"to\": \"%s\"}", this.pushTitle, this.pushText, registerId);

            byte[] outputBytes = notification.getBytes("UTF-8");
            OutputStream os = httpcon.getOutputStream();
            os.write(outputBytes);
            os.close();

            // Reading response
            InputStream input = httpcon.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                for (String line; (line = reader.readLine()) != null;) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendiOSMessage(){
		String appId = IOSAppData.getAppId(this.accountEmail, this.osType);
		Document filterDoc = new Document("appId", appId);
		FindIterable<Document> iterable = MongoDBHandler.find("appPushNotificationConfig", filterDoc);

		Document config = iterable.first();

		if(config != null){
			String certificateType = config.getString("certificateType");
			String certificate = config.getString("certificate");
			String passphrase = config.getString("passphrase");

			System.out.println(String.format("certificateType %s, certifcate %s and passphrase %s", certificateType, certificate, passphrase));

			this.sendiOSMessage(certificateType, certificate, passphrase);
		}
	}

	private void  sendiOSMessage(String certificateType, String certificate, String passphrase) {
		try {
			final ApnsClient apnsClient = new ApnsClientBuilder()
					.setClientCredentials(new File(certificate), passphrase)
					.build();
			
			String hostname = certificateType.toLowerCase().equals("development") ? ApnsClient.DEVELOPMENT_APNS_HOST : ApnsClient.PRODUCTION_APNS_HOST;
			final Future<Void> connectFuture = apnsClient.connect(hostname);
			connectFuture.await();

			final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
			payloadBuilder.setAlertBody(pushText);

			final String payload = payloadBuilder.buildWithDefaultMaximumLength();

			String appId = IOSAppData.getAppId(this.accountEmail, this.osType);
			String bundleId = IOSAppData.getBundleId(this.accountEmail, this.osType);
			Document filterDoc = new Document("appId", appId);

			FindIterable<Document> iterable = MongoDBHandler.find("appDeviceIds", filterDoc);

			Block<Document> appBlock = new Block<Document>() {
				@Override
				public void apply(final Document document) {
					String deviceToken = document.getString("deviceId");
					try {
						PushNotification.sendAPN(apnsClient, deviceToken, payload, bundleId);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			iterable.forEach(appBlock);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void sendAPN(final ApnsClient apnsClient, final String deviceToken, final String payload, final String bundleId) throws InterruptedException {
		final SimpleApnsPushNotification pushNotification;
		final String token = TokenUtil.sanitizeTokenString(deviceToken);

		pushNotification = new SimpleApnsPushNotification(token, bundleId, payload);

		final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
				apnsClient.sendNotification(pushNotification);
		System.out.println(String.format("Sending push notification for bunlde id %s", bundleId));
		System.out.println(String.format("Sending push notification for %s", deviceToken));
		try {
			final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
					sendNotificationFuture.get();

			if (pushNotificationResponse.isAccepted()) {
				System.out.println("Push notification accepted by APNs gateway.");
			} else {
				System.out.println("Notification rejected by the APNs gateway: " +
						pushNotificationResponse.getRejectionReason());

				if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
					System.out.println("\t…and the token is invalid as of " +
							pushNotificationResponse.getTokenInvalidationTimestamp());
				}
			}
		} catch (final ExecutionException e) {
			System.err.println("Failed to send push notification.");
			e.printStackTrace();

			if (e.getCause() instanceof ClientNotConnectedException) {
				System.out.println("Waiting for client to reconnect…");
				apnsClient.getReconnectionFuture().await();
				System.out.println("Reconnected.");
			}
		}
	}
}
