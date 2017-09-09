package com.ebizon.appify.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecurityUtils {
	private static MessageDigest md;
	public static String encryptWithMD5(String rawString){
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] passBytes = rawString.getBytes();
			md.reset();
			byte[] digested = md.digest(passBytes);
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<digested.length;i++){
				sb.append(Integer.toHexString(0xff & digested[i]));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			Logger logger = Logger.getLogger(SecurityUtils.class.getName());
			logger.log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
