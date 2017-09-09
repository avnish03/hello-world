package com.ebizon.appify.database;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ShopAuthenticator extends Authenticator 
{
	private final String username;
	private final String password;

	public ShopAuthenticator(String user, String pass){
		System.out.println("Authenticating in ShopAuthenticator using user:"+user+" ; password:"+pass);
		username = user;
		password = pass;
	}

	protected PasswordAuthentication getPasswordAuthentication(){
		return new PasswordAuthentication(username, password.toCharArray());
	}
}

