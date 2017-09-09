package com.ebizon.appify.shopifyapp;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class DisplayRequestAttributes {
	
	public static void printHeaders(HttpServletRequest request){
		Enumeration<?> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
		  String headerName = (String)headerNames.nextElement();
		  System.out.println("Header Name - " + headerName + ", Value - " + request.getHeader(headerName));
		}
	}
	
	public static void printParams(HttpServletRequest request){
		Enumeration<?> params = request.getParameterNames(); 
		while(params.hasMoreElements()){
		 String paramName = (String)params.nextElement();
		 System.out.println("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
		}
	}
	
	
}
