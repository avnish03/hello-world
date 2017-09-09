package com.ebizon.appify.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.Callable;

/**
 * Created by manish on 24/12/16.
 */
public class AuthGenerator implements Callable<Void>{

    private static AuthGenerator ourInstance = new AuthGenerator();

    public static AuthGenerator getInstance() {
        return ourInstance;
    }

    private SecureRandom random = new SecureRandom();

    private Authorization authorization = null;

    private AuthGenerator() {
    }

    public Authorization getAuth(String identity){
        String authKey = randomKey();
        String authSecret = randomKey();
        this.authorization = new Authorization(identity, authKey, authSecret);
        GenericQueue.getInstance().addToQueue(this);

        return this.authorization;
    }

    public Authorization getAuth(String identity, long validityEnd){
        String authKey = randomKey();
        String authSecret = randomKey();
        this.authorization = new Authorization(identity, authKey, authSecret, validityEnd);

        GenericQueue.getInstance().addToQueue(this);
        return this.authorization;
    }


    public Void call() {
        try{
            if (this.authorization != null){
                this.authorization.saveToDB();
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    private String randomKey() {
        BigInteger id1 = new BigInteger(130, random);
        BigInteger id2 = BigInteger.valueOf(Instant.now().toEpochMilli());
        BigInteger id = id1.add(id2);

        return id.toString(32);
    }
}
