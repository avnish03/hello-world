package com.ebizon.appify.utils;

import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.*;

public class Utils {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map,  boolean ascending){
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getValue()).compareTo( o2.getValue() );
			}
		} );

		if(!ascending){
			Collections.reverse(list);
		}
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public static String getAppId(String user, String osType){
		String appID = "";
		Document filterDoc = new Document("accountemail", user)
				.append("ostype", osType);

		FindIterable<Document> iterable = MongoDBHandler.find("appData", filterDoc);

		if (iterable.first() != null){
			Document doc = iterable.first();
			appID = doc.getObjectId("_id").toString();
		}

		return appID;
	}
}
