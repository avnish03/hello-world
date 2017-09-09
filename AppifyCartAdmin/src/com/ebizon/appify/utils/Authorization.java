    package com.ebizon.appify.utils;

    import com.ebizon.appify.database.MongoDBHandler;
    import com.mongodb.client.FindIterable;
    import org.bson.Document;

    import java.time.Instant;

    /**
     * Created by manish on 24/12/16.
     */
    public class Authorization {
        private static String collectionName = "authorization";
        private static long oneDaySecond = 24 * 60 * 60;
        private String identity;
        private String authKey;
        private String authSecret;
        private long validityEnd;

        public Authorization(String identity, String authKey, String authSecret) {
            this.identity = identity;
            this.authKey = authKey;
            this.authSecret = authSecret;
            this.validityEnd = Instant.now().getEpochSecond() + oneDaySecond;
        }

        public Authorization(String identity, String authKey, String authSecret, long validityEnd) {
            this.identity = identity;
            this.authKey = authKey;
            this.authSecret = authSecret;
            this.validityEnd = validityEnd;
        }

        public String getToken(){
           return String.format("%s:%s:%s", this.identity, this.authKey, this.authSecret);
        }

        public String getAuthKey(){
            return this.authKey;
        }

        public String getAuthSecret(){
            return this.authSecret;
        }

        public void saveToDB(){
            if(identity != null && authKey != null && authSecret != null){
                Document document = new Document("identity", this.identity)
                        .append("authKey", this.authKey)
                        .append("authSecret", this.authSecret)
                        .append("validityEnd", this.validityEnd);
                MongoDBHandler.insertOne(collectionName, document);
            }
        }

        public static boolean isValid(String token){
            boolean isValid = false;
            if(token != null){
                String[] result = token.split(":");
                if(result.length == 3){
                    String identity = result[0];
                    String key = result[1];
                    String secret = result[2];

                    isValid = isValid(identity, key, secret);
                }
            }

            return  isValid;
        }

        public static boolean isValid(String identity, String authKey, String authSecret){
            boolean isValid = false;
            Document filterDoc = new Document("identity", identity)
                    .append("authKey", authKey)
                    .append("authSecret", authSecret);

            FindIterable<Document> documents = MongoDBHandler.find(collectionName, filterDoc);

            Document document = documents.first();
            if(document != null){
                long validityTime = document.getLong("validityEnd");
                isValid = (validityTime > Instant.now().getEpochSecond());
            }
            return isValid;
        }
    }
