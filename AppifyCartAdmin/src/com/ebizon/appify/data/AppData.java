package com.ebizon.appify.data;

/**
 * Created by manish on 07/01/17.
 */
public class AppData {
    protected String accountEmail;
    protected String osType;
    protected String platform;
    protected String website;
    protected String appName;
    protected String shopifyAccessToken;
    protected String apiKey;
    protected String apiPassword;
    protected String appIcon;
    protected String appSplash;
    protected String bundleId;
    protected String appLanguage;

    public String getAccountEmail() {
        return accountEmail;
    }

    public String getOsType() {
        return osType;
    }

    public String getPlatform() {
        return platform;
    }

    public String getWebsite() {
        return website;
    }

    public String getAppName() {
        return appName;
    }

    public String getShopifyAccessToken() {
        return shopifyAccessToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public String getAppSplash() {
        return appSplash;
    }

    public String getBundleId() {
        return bundleId;
    }

    public String getAppLanguage() {
        return appLanguage;
    }

    public void setAccountEmail(String accountEmail){
        this.accountEmail = accountEmail;
    }

    public void setOsType(String osType){
        this.osType = osType;
    }

    public void setPlatform(String platform){
        this.platform = platform;
    }

    public void setAppLanguage(String appLanguage) {
        this.appLanguage = appLanguage;
    }

    public void setWebsite(String website){
        this.website = website;

        if(!this.website.startsWith("https://")){
            this.website = "https://" + website;
        }

        if(!this.website.endsWith("/")){
            this.website = this.website + "/";
        }
    }

    public void setAppName(String appName){
        this.appName = appName;
    }

    public void setShopifyAccessToken(String shopifyAccessToken) {
        this.shopifyAccessToken = shopifyAccessToken;
    }

    public void setApiKey(String apiKey){
        this.apiKey = apiKey;
    }

    public void setApiPassword(String apiPassword){
        this.apiPassword = apiPassword;
    }

    public void setAppIcon(String appIcon){
        this.appIcon = appIcon;
    }

    public void setAppSplash(String appSplash){
        this.appSplash = appSplash;
    }

    public void setBundleId(String bundleId){
        this.bundleId = bundleId;
    }
}
