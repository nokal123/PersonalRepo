package edu.yu.cs.com1320.project.Impl;

import java.io.BufferedReader;
import java.io.File;
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
import org.apache.commons.io.FileUtils;

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
import edu.yu.cs.com1320.project.DocumentStore;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;


public class test {

	public static void main(String[] args) throws URISyntaxException {
		
		File baseDir = new File("/Users/Nokal123/Desktop/MainCompSci");
		DocumentStoreImpl docstore = new DocumentStoreImpl(baseDir);
		String doc = "Hello Hello yo yo yo yo yo Hello Hello";
		HashMap<String, Integer> map = new HashMap<>();
		URI uri = new URI("C://www.hello.com../lolzzzz/thisisgreat/stupid/hashemisgreat");
		byte[] docInByte = docstore.compressDoc(CompressionFormat.ZIP, doc);
		DocumentImpl newDoc = new DocumentImpl(docInByte, doc.hashCode(), uri, CompressionFormat.ZIP, map); 
		
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

			String[] splitUp = doc.split("[^a-zA-Z]+");

			for(String word : splitUp) 
			{ //changes the word to lower case so that it is case insensitive
			String wordL = word.toLowerCase(); 
			Integer oldCount = map.get(wordL); 
			if (oldCount == null) {
			  oldCount = 0;
			}
			map.put(wordL, oldCount + 1);
			} 

			
			String jsonString = gson.toJson(newDoc);
			System.out.println(jsonString);
			
			File newFile = null;
			
			try {
				
				String filePath;
				
				if(baseDir != null) {
					filePath = baseDir + File.separator + uri.getHost() + uri.getPath();	
				}
				else {
					filePath = System.getProperty("user.dir") + File.separator + uri.getHost() + uri.getPath();		
				}
				
				System.out.println(filePath);
				String directoryStructure = filePath.substring(0, filePath.lastIndexOf('/')+1);
				File pathToFile = new File(directoryStructure);
				pathToFile.mkdirs();
				
				File file = new File(filePath + ".json");
				file.createNewFile();
				FileWriter writer = new FileWriter(file); 
				writer.write(jsonString);
	            writer.close();
			        
		
			} catch (IOException e) { e.printStackTrace();}
		
			
			
			String filePath;
			
			JsonDeserializer<DocumentImpl> deserializer = new JsonDeserializer<DocumentImpl>() {
				@Override
				public DocumentImpl deserialize(JsonElement json, Type typeOfT, 
						JsonDeserializationContext context) throws JsonParseException {
							JsonObject jsonObject = json.getAsJsonObject();
							//deserilizing the byteArray
							byte[] byteArray = Base64.decodeBase64(jsonObject.get("docInBytes").getAsString());
							System.out.println("BYTES: "+ byteArray);
							//deserilizing the URI
							URI uri = null;
							try { 
								uri = new URI(jsonObject.get("URI").getAsString());  //trycatch for creating the URI
							} catch (URISyntaxException e) { e.printStackTrace();}
							System.out.println("URI: "+uri);
							//deserilizing the compression format
							DocumentStore.CompressionFormat compress = CompressionFormat.valueOf(CompressionFormat.class, jsonObject.get("CompressionFormat").getAsString());
							System.out.println("COMPRESS:" + compress);
							//deserilizing the hashcode
							int hashCode = jsonObject.get("HashCode").getAsInt();
							System.out.println("HASHCODE: "+hashCode);
							//deserializeing the map
							String mapString = jsonObject.get("WordMap").getAsString();
							HashMap<String,Integer> map = new Gson().fromJson(mapString, new TypeToken<HashMap<String, Integer>>(){}.getType());
							System.out.println("MAP:" + map);
							DocumentImpl newDoc = new DocumentImpl(byteArray, hashCode, uri, compress, map);
							
							return newDoc;
					
					
				}
			}; 
			
			
			Gson gson1 = new GsonBuilder()
					.registerTypeAdapter(Document.class, deserializer)
					.registerTypeAdapter(DocumentImpl.class, deserializer)
					.create();
			
			try {
				
				
				if(baseDir != null) {
					filePath = baseDir + File.separator + uri.getHost() + uri.getPath() + ".json";	
				}
				
				else {
					filePath = System.getProperty("user.dir") + File.separator + uri.getHost() + uri.getPath() + ".json";		
				}
				
				Reader reader = new FileReader(filePath);
				JsonReader jReader = new JsonReader(reader);
				DocumentImpl newDoc1 = gson1.fromJson(jReader, DocumentImpl.class); 
		
			} catch (IOException e) { e.printStackTrace();}
			
			
	}

}


/*

Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonString = gson.toJson(newDoc);
		System.out.println(jsonString);
		
		File newFile = null;
		
		try {
			
			String filePath;
			
			if(docstore.baseDir != null) {
				filePath = docstore.baseDir + File.separator + uri.getHost() + uri.getPath() + ".json";	
			}
			
			else {
				filePath = System.getProperty("user.dir") + File.separator + uri.getHost() + uri.getPath() + ".json";		
			}
			
			File file = new File(filePath);
			file.createNewFile();
			FileWriter writer = new FileWriter(file); 
			writer.write(jsonString);
            writer.close();
		        
	
		} catch (IOException e) { e.printStackTrace();}
		
*/