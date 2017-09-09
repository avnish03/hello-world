package com.ebizon.appify.database;

import com.ebizon.appify.scheduledjob.ShopifyBestSellers;
import com.ebizon.appify.scheduledjob.ShopifyNewProducts;
import com.ebizon.appify.scheduledjob.ShopifyOrders;
import com.ebizon.appify.scheduledjob.ShopifyProducts;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class MongoDBContextListener implements ServletContextListener {
	private static final int ScheduledTimeGap = 6; //In hours
	
	private static MongoClient mongo;
	private static Scheduler scheduler;
	private static String dbName;
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		 mongo = (MongoClient) sce.getServletContext()
				.getAttribute("MONGO_CLIENT");
		
		Logger logger = Logger.getLogger("MongoDB");
		
		mongo.close();
		try {
			scheduler.shutdown(true);
			logger.log(Level.INFO, "scheduler shutdown successfully");
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
		logger.log(Level.INFO, "MongoClient closed successfully");
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		mongo = new MongoClient(
				ctx.getInitParameter("MONGODB_HOST"), 
				Integer.parseInt(ctx.getInitParameter("MONGODB_PORT")));
		
		dbName = ctx.getInitParameter("MONGODB_NAME");
		
		startScheduler();
		  
		Logger logger = Logger.getLogger("MongoDB");
		
		logger.log(Level.INFO, "MongoClient initialized successfully");
		
		sce.getServletContext().setAttribute("MONGO_CLIENT", mongo);
	}

	private void startScheduler() {
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			  // and start it off
			scheduler.start();
			scheduleFetchProducts(scheduler);
			scheduleFetchOrders(scheduler);
			scheduleBestSellersCalculator(scheduler);
			scheduleNewProductsCalculator(scheduler);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private void scheduleFetchProducts(Scheduler scheduler) throws SchedulerException {
		scheduleJob(scheduler, ShopifyProducts.class, ShopifyProducts.class.getSimpleName());
	}
	
	private void scheduleFetchOrders(Scheduler scheduler) throws SchedulerException {
		scheduleJob(scheduler, ShopifyOrders.class, ShopifyOrders.class.getSimpleName());
	}
	
	private void scheduleBestSellersCalculator(Scheduler scheduler) throws SchedulerException {
		scheduleJob(scheduler, ShopifyBestSellers.class, ShopifyBestSellers.class.getSimpleName());
	}
	
	private void scheduleNewProductsCalculator(Scheduler scheduler) throws SchedulerException {
		scheduleJob(scheduler, ShopifyNewProducts.class, ShopifyNewProducts.class.getSimpleName());
	}
	
	private void scheduleJob(Scheduler scheduler, Class <? extends Job> jobClass, String className) throws SchedulerException {
		String group = className + "Group";
		String triggerIdentity = className + "Trigger";
		
		JobDetail job = JobBuilder.newJob(jobClass)
				.withIdentity(className, group)
				.build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(triggerIdentity, group)
				.startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
						.withIntervalInHours(ScheduledTimeGap)
						.repeatForever())
				.build();

		// Tell quartz to schedule the job using our trigger
		scheduler.scheduleJob(job, trigger);
	}

	public static MongoDatabase getMongoDBInstance(){
		MongoDatabase mongoDB = MongoDBContextListener.mongo.getDatabase(MongoDBContextListener.dbName);
		
		assert(mongoDB != null);
		
		return mongoDB;
	}
}

