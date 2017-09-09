package com.ebizon.appify.shopifyapp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class HMACValidator {

	public static boolean validateNonce(String shop){
		int requestNonce = ShopifyApp.stores.get(shop).getInt("install_state");
		int responseNonce = ShopifyApp.stores.get(shop).getInt("redirected_state");
		if(requestNonce == responseNonce) {
			ShopifyApp.stores.get(shop).put("verified","success");
			return true;
		}
		else
			return false;
	}
//	public static boolean hmacValidation() {
//		boolean flag = false;
//		try {
//				String appApiKey = ShopifyConstants.getAppApiKey();
//				String appApiSecret = ShopifyConstants.getAppApiSecret();
//
//				String key = "hush" ;
//				String message = "code=0907a61c0c8d55e99db179b68161bc00&shop=some-shop.myshopify.com&timestamp=1337178173";
//				String hmac = "4712bf92ffc2917d15a2f5a273e39f0116667419aa4b6ac0b3baaf26fa3c4d20";
//
////				String key = appApiSecret ;
////				String message = "code=4123c48fb99a7e52317c585d4599956c&shop=hellodemotrial2-2.myshopify.com&timestamp=1497337782";
////				String hmac = "3ebcdf73a9984327c11790abb86f8c480a9d521a14026ed3fd35f7887d6b2970";
//
//				System.out.println("hmac originnal  - "+hmac);
//
//			    Mac hasher = Mac.getInstance("HmacSHA256");
//			    hasher.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
//
//			    byte[] hash = hasher.doFinal(message.getBytes());
//			    String temp = DatatypeConverter.printHexBinary(hash);
//			    temp = temp.toLowerCase();
//			    if(hmac.equals(temp))
//			    	flag = true;
//
//			    System.out.println("\n\nResult====="+temp);
//	  	}
//	  	catch (NoSuchAlgorithmException e) {}
//	  	catch (InvalidKeyException e) {}
//		return flag;
//	  }
}
/*
//https://app.appifycart.com/AppifyCartAdmin/rest/admin/appData.json/appifycart@ebizontek.com
shop is set to :hellodemotrial2-2
hmac is set to :6936efb2dfdb4535bf89771c9287092d12bc1c3f6fc21819a5a667addbcb3e21
Parameter Name - hmac, Value - 6936efb2dfdb4535bf89771c9287092d12bc1c3f6fc21819a5a667addbcb3e21
Parameter Name - shop, Value - hellodemotrial2-2.myshopify.com
Parameter Name - timestamp, Value - 1497337770
Nonce is set to :9


Parameter Name - code, Value - 4123c48fb99a7e52317c585d4599956c
Parameter Name - hmac, Value - 3ebcdf73a9984327c11790abb86f8c480a9d521a14026ed3fd35f7887d6b2970
Parameter Name - shop, Value - hellodemotrial2-2.myshopify.com
Parameter Name - state, Value - 9
Parameter Name - timestamp, Value - 1497337782

*/