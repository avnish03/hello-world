package com.ebizon.appify.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtils {
	static final String SMTP_USERNAME = "AKIAJWAK4FHNMIOLIGVQ";  // SMTP username for Amazon SES
	static final String SMTP_PASSWORD = "AqGtjUugpA1jme+NKcRPcLbJhbOZl6SVogHowZbrJp99";  // SMTP password for Amazon SES
	static final String HOST = "email-smtp.us-east-1.amazonaws.com";
	static final int PORT = 25;

	public static void sendEmail(final String[] emails, String subject, String htmlText){
		StringBuilder toAddress = new StringBuilder();
		for(String email : emails){
			toAddress.append(email);
			toAddress.append(",");
		}
		
		System.out.println(toAddress.toString());
		sendEmail(toAddress.toString(), subject, htmlText);
	}
	
	public static void sendEmail(String to, String subject, String htmlText){		
		String from = "Elle Sharma <info@appifycart.com>";
		
		final String username = "info@appifycart.com";
		final String password = "uZ!MgPq3";

		Properties properties = new Properties();
		properties.put("mail.transport.protocol", "smtps");
		properties.put("mail.smtp.port", PORT);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.starttls.required", "true");
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// Set Subject: header field
			message.setSubject(subject);

			// Now set the actual message
			//message.setText(body);

			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(htmlText, "text/html");
			MimeMultipart mimeMultipart = new MimeMultipart();
			mimeMultipart.addBodyPart(bodyPart);
			message.setContent(mimeMultipart);
			// Send message
			Transport transport = session.getTransport();
			try
			{
				// Connect to Amazon SES using the SMTP username and password you specified above.
				transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
				// Send the email.
				transport.sendMessage(message, message.getAllRecipients());
                System.out.println(String.format("Sent message successfully to %s with subject %s",to, subject));
            }
			catch (Exception ex) {
				System.out.println("The email was not sent.");
				System.out.println("Error message: " + ex.getMessage());
			}
			finally
			{
				// Close and terminate the connection.
				transport.close();
			}
		}catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	
	//Method to replace the values for keys
	public static String readEmailFromHtml(String filePath, Map<String, String> input)
	{
		String msg = readContentFromFile(filePath);
		try
		{
			Set<Entry<String, String>> entries = input.entrySet();
			for(Map.Entry<String, String> entry : entries) {
				msg = msg.replace(entry.getKey().trim(), entry.getValue().trim());
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
		return msg;
	}

	//Method to read HTML file as a String 
	private static String readContentFromFile(String fileName)
	{
		StringBuffer contents = new StringBuffer();

		try {
			//use buffering, reading one line at a time
			BufferedReader reader =  new BufferedReader(new FileReader(fileName));
			try {
				String line = null; 
				while (( line = reader.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				reader.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		return contents.toString();
	}
}
