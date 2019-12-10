package edu.yu.cs.com1320.project.Impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentIO;
import edu.yu.cs.com1320.project.DocumentStore;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class DocumentIOImpl extends DocumentIO
{
	public DocumentIOImpl(File baseDir)
	 {
		 this.baseDir = baseDir;
		 
	 }
	
	@Override
	/**
	 * @param doc to serialize
	 * @return File object representing file on disk to which document was serialized
	 */
	 public File serialize(Document doc) //may need to change this all back to DocuemntImpl
	 {	 
		JsonSerializer<DocumentImpl> serializer = new JsonSerializer<DocumentImpl>() {  
			@Override
			public JsonElement serialize(DocumentImpl doc, Type typeOfSrc, JsonSerializationContext context) {
			    JsonObject jsonDocument = new JsonObject();
			    String byteArray = Base64.encodeBase64String(doc.getDocument());
			    
			    jsonDocument.addProperty("docInBytes", byteArray);
			    jsonDocument.addProperty("CompressionFormat", doc.getCompressionFormat().toString());
			    jsonDocument.addProperty("URI", doc.getKey().toString());
			    jsonDocument.addProperty("HashCode", doc.getDocumentHashCode());
			    jsonDocument.addProperty("WordMap", doc.getWordMap().toString());
			    
			    return jsonDocument;
			  }
			};
			
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(Document.class, serializer)
					.registerTypeAdapter(DocumentImpl.class, serializer)
					.setPrettyPrinting()
					.create();
		
		
		String jsonString = gson.toJson(doc);

		return writeToFile(jsonString, doc.getKey());
	 }
	
	private File writeToFile(String jsonString, URI uri)
	{
		File newFile = null;
		try {
			String filePath;
			String uriToDirectory = uri.toString().substring(uri.toString().indexOf('/')+1, uri.toString().length());
			
			if(baseDir != null) {
				filePath = baseDir + File.separator + uriToDirectory;
			}
			else {
				filePath = System.getProperty("user.dir") + File.separator + uriToDirectory;		
			}
			
			String directoryStructure = filePath.substring(0, filePath.lastIndexOf('/')+1);
			
			File pathToFile = new File(directoryStructure);
			pathToFile.mkdirs();
			
			File file = new File(filePath + ".json");
			file.createNewFile();
			FileWriter writer = new FileWriter(file); 
			writer.write(jsonString);
            writer.close();
            newFile = file;
		        
	
		} catch (IOException e) { e.printStackTrace();}
		
		return newFile; 
	}
	
	@Override 
	/**
	 * @param uri of doc to deserialize
	 * @return deserialized document object
	 */
	 public Document deserialize(URI uri)
	 {
		String filePath;
		
		JsonDeserializer<Document> deserializer = new JsonDeserializer<Document>() {
			@Override
			public Document deserialize(JsonElement json, Type typeOfT, 
					JsonDeserializationContext context) throws JsonParseException {
				JsonObject jsonObject = json.getAsJsonObject();
				//deserilizing the byteArray
				byte[] byteArray = Base64.decodeBase64(jsonObject.get("docInBytes").getAsString());
				//deserilizing the URI
				URI uri = null;
				try { 
					uri = new URI(jsonObject.get("URI").getAsString());  //trycatch for creating the URI
				} catch (URISyntaxException e) { e.printStackTrace();}
				//deserilizing the compression format
				DocumentStore.CompressionFormat compress = CompressionFormat.valueOf(CompressionFormat.class, jsonObject.get("CompressionFormat").getAsString());
				//deserilizing the hashcode
				int hashCode = jsonObject.get("HashCode").getAsInt();
				//deserializeing the map
				String mapString = jsonObject.get("WordMap").getAsString();
				HashMap<String,Integer> map = new Gson().fromJson(mapString, new TypeToken<HashMap<String, Integer>>(){}.getType());
				DocumentImpl newDoc = new DocumentImpl(byteArray, hashCode, uri, compress, map);
				
				return newDoc;
				
				
			}
		}; 
		
		DocumentImpl newDoc = null;
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Document.class, deserializer)
				.registerTypeAdapter(DocumentImpl.class, deserializer)
				.setPrettyPrinting()
				.create();
		
		try {
			String uriToDirectory = uri.toString().substring(uri.toString().indexOf('/')+1, uri.toString().length());
			if(baseDir != null) {
				filePath = baseDir + File.separator + uriToDirectory + ".json";
			}
			else {
				filePath = System.getProperty("user.dir") + File.separator + uriToDirectory + ".json";		
			}
			
			Reader reader = new FileReader(filePath);
			JsonReader jReader = new JsonReader(reader);
			newDoc = gson.fromJson(jReader, DocumentImpl.class);
			newDoc.setLastUseTime(System.currentTimeMillis());
			Files.deleteIfExists(Paths.get(filePath));
		        
	
		} catch (IOException e) { e.printStackTrace();}
			
			return newDoc;

	
	 }
}
