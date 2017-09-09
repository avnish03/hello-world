package com.ebizon.appify.scheduledjob;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class InstantDataLoader {
	private static InstantDataLoader instance = null;
	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	
	private InstantDataLoader() {
		//Empty 
	}

	public static InstantDataLoader getInstance() {
		if(instance == null) {
			instance = new InstantDataLoader();
		}
		return instance;
	}
	
	public void addToQueue(String appId){
		ShopifyJob products = new ShopifyProducts(appId); //This should be first, all others depend on this
		ShopifyJob orders = new ShopifyOrders(appId);
		ShopifyJob newProducts = new ShopifyNewProducts(appId);
		ShopifyJob bestSellers = new ShopifyBestSellers(appId);
		
		executor.submit(products);
		executor.submit(orders);
		executor.submit(newProducts);
		executor.submit(bestSellers);
	}
}
