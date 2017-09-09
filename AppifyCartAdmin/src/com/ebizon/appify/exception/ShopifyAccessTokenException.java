package com.ebizon.appify.exception;

/**
 * Created by avnish on 25/8/17.
 */
public class ShopifyAccessTokenException extends Exception {
    private final String message="Invalid Header Name for Shopify Access Token";

    ShopifyAccessTokenException(){
        super();
    }

    public String getMessage(){
        return this.message;
    }
}
