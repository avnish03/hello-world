package com.ebizon.appify.builder;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BuildPool {
	private static BuildPool instance = null;
	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	
	private BuildPool() {
		//Empty 
	}

	public static BuildPool getInstance() {
		if(instance == null) {
			instance = new BuildPool();
		}
		return instance;
	}
	
	public void addToQueue(Callable<Void> builder){
		executor.submit(builder);
	}
}
