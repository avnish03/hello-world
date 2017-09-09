package com.ebizon.appify.scheduledjob;

import java.util.concurrent.Callable;

abstract class ShopifyJob implements Callable<Void>{
	private String appId = null;
	
	ShopifyJob(String appId) {
		this.appId = appId;
	}

	public Void call() throws Exception {
		if(this.appId != null){
			System.out.println(String.format("ShopifyJob is executing for app %s", this.appId));
			try{
				execute(this.appId);
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
	}
	
	protected abstract void execute(String appId);
}
