package com.ebizon.appify.data;

import com.ebizon.appify.builder.BaseBuilder;
import com.ebizon.appify.builder.BuilderConfig;
import com.ebizon.appify.database.MongoDBHandler;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BannerLogo {
    private String accountEmail;
    private String file;
    private String fileType; //Banner Or logo

    public BannerLogo() {
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void save() {
        Document doc = new Document("accountEmail", this.accountEmail)
                .append("file", this.file)
                .append("fileType", this.fileType);

        MongoDBHandler.insertOne("bannerLogoData", doc);
    }

    public static List<JSONObject> loadBannerLogo(String accountEmail) {
        List<JSONObject> jsonList = new ArrayList<>();

        Document filterDoc = new Document("accountEmail", accountEmail);

        FindIterable<Document> iterable = MongoDBHandler.find("bannerLogoData", filterDoc);

        Block<Document> appBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("accountEmail", document.get("accountEmail"));
                jsonObj.put("fileType", document.get("fileType"));
                String imagePath = document.getString("file");
                Path p = Paths.get(imagePath);
                String file = p.getFileName().toString();
                jsonObj.put("file", file);
                jsonList.add(jsonObj);
            }
        };

        iterable.forEach(appBlock);

        System.out.println("@vnish : Laoding banner and logos");
        return jsonList;
    }

    public static boolean deleteImage(String user, String image) {
        String filePath = "/AppifyCart/uploadFiles/"+user;
        String file = "/AppifyCart/uploadFiles/"+user+"/"+image;
        Document deleteDoc = new Document("accountEmail", user)
                .append("file", file);

        FindIterable<Document> iterable = MongoDBHandler.find("bannerLogoData", deleteDoc);
        if (iterable.first() != null) {
            Document doc = iterable.first();
            MongoDBHandler.deleteOne("bannerLogoData", deleteDoc);

            return deleteFileOnPhysicalDisk(filePath,image);
        }
        else
        {
            return false;
        }
    }

    public static boolean deleteFileOnPhysicalDisk(String filePath, String image){

        int exitValue = 0;
        // code to delete file physically
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(BuilderConfig.getInstance().getDeleteScript());
        commands.add(filePath);
        commands.add(image);

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
}
