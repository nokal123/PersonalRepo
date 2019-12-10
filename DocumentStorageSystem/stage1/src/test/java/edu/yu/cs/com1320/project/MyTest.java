package edu.yu.cs.com1320.project;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class MyTest {

	//Hash Map Tests 
	
	@Test (expected = IllegalArgumentException.class)
	public void nullKey() 
	{
		HashTableImpl map = new HashTableImpl();
		
		String key = null;
		int value = 3823984; 
		map.put(key, value);
		
	}
	@Test 
	public void emptyKey() 
	{
		HashTableImpl map = new HashTableImpl();
		
		String key = "";
		int value = 3823984; 
		map.put(key, value);
		assertEquals("getting the correct object from the map",value, map.get(key));
		
	}
	//Happy Path
	@Test
	public void gettingCorrectObject() 
	{
		HashTableImpl map = new HashTableImpl();
		
		String key = "hi";
		int value = 3823984; 
		map.put(key, value);
		assertEquals("getting the correct object from the map",value, map.get(key));
	}
	
	@Test
	public void manyInputs() 
	{
		HashTableImpl map = new HashTableImpl();
		
		String key = "hi";
		int value = 3823984; 
		String key1 = "bye";
		int value1 = 3823984; 
		String key2 =  "si";
		int value2 = 3823984; 
		String key3 = " ";
		int value3 = 3823984; 
		String key4 = "";
		int value4 = 3823984; 
		map.put(key, value);
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		
		assertEquals("getting the correct object from the map",value, map.get(key));
		assertEquals("getting the correct object from the map",value1, map.get(key1));
		assertEquals("getting the correct object from the map",value2, map.get(key2));
		assertEquals("getting the correct object from the map",value3, map.get(key3));
		assertEquals("getting the correct object from the map",value4, map.get(key4));
	
		
	}
	
	//Documentstore tests
	@Test
	public void putDocumentWithoutCompresionFormat() throws URISyntaxException 
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		assertEquals("putting in a document and getting the string back",doc, docstore.getDocument(uri));
	}	
	@Test
	public void deleatingadocument() throws URISyntaxException  
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hihihihihihihihihihi";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		//docstore.deleteDocument(uri);

		
		assertEquals("that the document was deleted", true, docstore.deleteDocument(uri));
		
		//assertEquals("that the document was deleted", null, docstore.getDocument(uri));
		
	}
	@Test
	public void jarcompression()
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";

	
		byte[] compressResult = docstore.compressDoc(DocumentStore.CompressionFormat.JAR, j);
		
		byte[] decompResult = docstore.decompressDoc(compressResult, DocumentStore.CompressionFormat.JAR);
		String newString = new String(decompResult);
		System.out.println("show: " + newString);
		
		assertEquals("checking to see if they are equal after compression and decompression",j, newString);		 
	}
	@Test
	public void zipcompression()
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";
	
		byte[] compressResult = docstore.compressDoc(DocumentStore.CompressionFormat.ZIP, j);
		
		byte[] decompResult = docstore.decompressDoc(compressResult, DocumentStore.CompressionFormat.ZIP);
		String newString = new String(decompResult);
		
		assertEquals("checking to see if they are equal after compression and decompression",j, newString);		 
	}
	@Test
	public void gzipcompression()
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";
	
		byte[] compressResult = docstore.compressDoc(DocumentStore.CompressionFormat.GZIP, j);
		
		byte[] decompResult = docstore.decompressDoc(compressResult, DocumentStore.CompressionFormat.GZIP);
		String newString = new String(decompResult);
		
		assertEquals("checking to see if they are equal after compression and decompression",j, newString);		 
	}
	@Test
	public void bzip2compression()
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I love Donuts I love DoununtsI love Donuts I love Doununts I love Donuts I love Doununts I love Donuts I love Doununts I love Donuts I love Doununts  and icecreamand icecreamand icecream and icecreamand icecream and icecream and icecream and icecream ";
	
		byte[] compressResult = docstore.compressDoc(DocumentStore.CompressionFormat.BZIP2, j);
		
		byte[] decompResult = docstore.decompressDoc(compressResult, DocumentStore.CompressionFormat.BZIP2);
		String newString = new String(decompResult);
		
		assertEquals("checking to see if they are equal after compression and decompression",j, newString);		 
	}
	@Test
	public void sevenzcompression()
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";
		byte[] input = docstore.sevenZCompress(j);
		byte[] in = docstore.sevenZDecompress(input);
		String newString = new String(in);
		
		assertEquals("checking to see if they are equal after compression and decompression",j, newString);		 
	}
	
}	
