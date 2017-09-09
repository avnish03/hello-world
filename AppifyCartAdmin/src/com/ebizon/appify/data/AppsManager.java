package com.ebizon.appify.data;

import com.ebizon.appify.builder.BaseBuilder;
import com.ebizon.appify.builder.BuilderConfig;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.Utils;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avnish on 12/04/17.
 */
public class AppsManager {
    private String username;
    private String osType;

    public AppsManager(String username, String osType) {
        this.username = username ;
        this.osType = osType;
    }

    public String getUsername() {
        return username;
    }

    public String getOsType() {
        return osType;
    }

    public JSONObject getAllBuilds(){
        JSONObject resObject = new JSONObject();
        String buildAppId = Utils.getAppId(getUsername(),getOsType());

        System.out.println("username : "+getUsername()+" ; buildAppId : "+buildAppId);
        List<JSONObject> appBuilds = loadAllBuilds(buildAppId);

        int status = (appBuilds.size() <= 0 ) ? 0 : 1;
        resObject.put("status",status);
        resObject.put("osType",getOsType());
        resObject.put("appBuilds",appBuilds);

        return resObject;
    }

    protected  List<JSONObject> loadAllBuilds(String appId) {
        List<JSONObject> jsonList = new ArrayList<>();
        FindIterable<Document> iterable = MongoDBHandler.find("buildIdData", new Document("appId", appId));

        Block<Document> appBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                String buildID = document.getObjectId("_id").toString();
                String fileName = document.get("appname")+"_"+buildID+"."+getBuildExt();
                String downloadLink = "/AppifyCartAdmin/rest/admin/appdownload.json/"+getUsername()+"/"+getOsType()+"/"+fileName;

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", buildID);
                jsonObj.put("appname", document.get("appname"));
                jsonObj.put("bundleId", document.get("bundleId"));
                jsonObj.put("downloadLink", downloadLink);
                jsonObj.put("creationTime", document.get("creationTime"));

                jsonList.add(jsonObj);
            }
        };
        iterable.forEach(appBlock);

        return jsonList;
    }


    public boolean deleteOneBuild(String appId, String docId) {
        Document deleteDoc = new Document("_id", new ObjectId(docId))
                .append("appId",appId);

        System.out.println("to be deleted :" +deleteDoc);

        FindIterable<Document> iterable = MongoDBHandler.find("buildIdData", deleteDoc);
        if (iterable.first() != null) {
            Document doc = iterable.first();

            String appName = doc.get("appname").toString();
            MongoDBHandler.deleteOne("buildIdData", deleteDoc);

            String ext = getBuildExt();
            String fileName = appName+"_"+docId+"."+ext;
            System.out.println("@vnish file to delete :"+fileName);
            return deleteFileOnPhysicalDisk(fileName);
        }
        else
        {
            return false;
        }
    }

    public boolean deleteFileOnPhysicalDisk(String appId){

        int exitValue = 0;
        // code to delete file physically
        String filePath = BaseBuilder.getBuildPath(getUsername(), getOsType());
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(BuilderConfig.getInstance().getDeleteScript());
        commands.add(filePath);
        commands.add(appId);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb = pb.inheritIO();
        Process process = null;

        try {
            process = pb.start();
            exitValue = process.waitFor();
            System.out.println("Deleting Files on physical disk exitvalue "+exitValue);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(exitValue == 0) return true;
        else return false;
    }

    public String getBuildExt() {
        return (getOsType().equalsIgnoreCase("ios"))?"ipa":"apk" ;
    }
}
