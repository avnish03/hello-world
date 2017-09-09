package com.ebizon.appify.utils;

public class TableNameGenerator {
	public static String getShopifyTable(String appId, String type){
		String tableName = String.format("shopify_%s_%s", appId, type);
		
		return tableName;
	}
}
