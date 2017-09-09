package com.ebizon.appify.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class EmailUtilsBKP {
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
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.appifycart.com.");
		properties.put("mail.smtp.port", "587");

		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

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
			Transport.send(message);
			System.out.println(String.format("Sent message successfully to %s with subject %s",to, subject));
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
			for(Entry<String, String> entry : entries) {
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
