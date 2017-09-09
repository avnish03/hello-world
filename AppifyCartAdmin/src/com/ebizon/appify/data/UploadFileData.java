package com.ebizon.appify.data;

import com.ebizon.appify.builder.BuilderConfig;
import com.ebizon.appify.database.MongoDBHandler;
import com.ebizon.appify.utils.Constants;
import com.ebizon.appify.utils.GetCurlResponse;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.json.JSONObject;

import javax.ws.rs.WebApplicationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UploadFileData {
	private final String user;
	private final String osType;
	private final String fileType;
	private final String index;
	private final String fileExtension;
	
	public UploadFileData(String user, String osType, String fileType, String index, String fileExtension){
		this.user = user;
		this.osType = osType;
		this.fileType = fileType;
		this.index = index;
		this.fileExtension = fileExtension;
	}
	
	private String createDBEntry(){		
		Document doc = new Document()
				.append("user", this.user)
				.append("ostype", this.osType)
				.append("filetype", this.fileType)
				.append("index", this.index)
				.append("fileextension", this.fileExtension);
		
		MongoDBHandler.insertOne("uploadFileData", doc);			

		return doc.getObjectId("_id").toString();
	}

	private java.nio.file.Path getFilePath(){
		StringBuilder stringBuilder = new StringBuilder(BuilderConfig.getInstance().getBaseDir());
		stringBuilder.append("/");
		stringBuilder.append("uploadFiles");
		stringBuilder.append("/");
		stringBuilder.append(user);

		java.nio.file.Path path = Paths.get(stringBuilder.toString());
		try
		{
			Files.createDirectories(path);
		}catch(IOException e){
			e.printStackTrace();
		}

		return path;
	}
	
	public String save(InputStream fileInputStream){
		String fileId = createDBEntry();
		
		java.nio.file.Path path = getFilePath();
		String filePath = path + "/" + "file_" + fileId + "." + this.fileExtension;

		System.out.println("UploadFileData:save; Path:"+filePath);

		try{
			int read;
			byte[] bytes = new byte[1024];
				
			OutputStream out = new FileOutputStream(new File(filePath));
			while ((read = fileInputStream.read(bytes)) != -1) 
			{
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e){
			throw new WebApplicationException();
		}
		
		return filePath;
	}

    /**
     * uploading files for ios build to server
     *
     * @param filepath
     * @return
     */
	public String uploadToIOSServer(String filepath){
        String email = this.user;
        String ostype = this.osType;
        String index = this.index;

		String url = Constants.MacCloudServer+"/AppifyIOSBuild/rest/admin/fileupload.json/"+email+"/"+ostype+"/build/"+index;
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("curl");
		commands.add("-X");
		commands.add("POST");
		commands.add(url);
		commands.add("-H");
		commands.add("'accept: application/json;odata=verbose'");
		commands.add("-H");
		commands.add("'cache-control: no-cache'");
		commands.add("-H");
		commands.add("'content-type: multipart/form-data;'");
		commands.add("-F");
		commands.add(String.format("file=@%s", filepath));

        String response = null;
        try {
            response = GetCurlResponse.executeCurlRequest(commands);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
	}

	/**
	 *
	 * @param user
	 * @param ostype
     * @return app icons and splash as List
     *
	 */
	public static List<JSONObject> loadIconSplash(String user, String ostype) {

        String appId = IOSAppData.getAppId(user, ostype);

		List<JSONObject> jsonList = new ArrayList<>();

		Document filterDoc = new Document("accountemail", user)
				.append("ostype",ostype);
		FindIterable<Document> iterable = MongoDBHandler.find("appData", filterDoc);
		Block<Document> appBlock = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				JSONObject jsonObj = new JSONObject();
		        String appIconImagePath = document.getString("appIcon");
				String appSplashImagePath = document.getString("appSplash");
				Path p1 = Paths.get(appIconImagePath);
				Path p2 = Paths.get(appSplashImagePath);
				String appIconfile = p1.getFileName().toString();
				String appSplashfile = p2.getFileName().toString();
				jsonObj.put("appIcon", appIconfile);
				jsonObj.put("appSplash", appSplashfile);

				jsonList.add(jsonObj);
			}
		};
		iterable.forEach(appBlock);

        System.out.println("@vnish : icons and splash found : "+jsonList.toString());
		return jsonList;
	}
}
