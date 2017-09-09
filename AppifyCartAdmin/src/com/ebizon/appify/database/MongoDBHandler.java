package com.ebizon.appify.database;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.time.Instant;

public class MongoDBHandler {
	public static void insertOne(String collectionName, Document doc) {
		long now = Instant.now().getEpochSecond();
		doc.append("creationTime", now);
		doc.append("updateTime", now);
		
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();
		db.getCollection(collectionName).insertOne(doc);
	}

	public static UpdateResult updateOne(String collectionName, Document checkDoc, Document newDoc) {
		long now = Instant.now().getEpochSecond();
		newDoc.append("updateTime", now);
		Document updateDoc = new Document("$set", newDoc);
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();

		return db.getCollection(collectionName).updateOne(checkDoc, updateDoc);
	}
	
	public static FindIterable<Document> find(String collectionName, final Document filterDoc){
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();
		return db.getCollection(collectionName).find(filterDoc);
	}

	public static long count(String collectionName, final Document filterDoc){
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();
		return db.getCollection(collectionName).count(filterDoc);
	}

	public static boolean isExist(String collectionName, final Document document) {
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();
		return db.getCollection(collectionName).count(document) > 0;
	}

	public static boolean deleteOne(String collectionName, final Document document) {
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();
        System.out.println("o/p-------"+ db.getCollection(collectionName).deleteOne(document) );
		return true;
	}
	public static boolean deleteManyOne(String collectionName, final Document document) {
		MongoDatabase db = MongoDBContextListener.getMongoDBInstance();
		System.out.println("o/p-------"+ db.getCollection(collectionName).deleteMany(document) );
		return true;
	}



}
