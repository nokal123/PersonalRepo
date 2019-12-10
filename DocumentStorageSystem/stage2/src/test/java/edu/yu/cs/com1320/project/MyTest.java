package edu.yu.cs.com1320.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import edu.yu.cs.com1320.project.*;
import static org.junit.Assert.*;
import edu.yu.cs.com1320.project.Impl.*;
import edu.yu.cs.com1320.project.Impl.HashTableImpl.HashElement;

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
		
		String key = "hi0";
		int value = 3823980;
		String key1 = "hi1";
		int value1 = 3823981;
		String key2 = "hi2";
		int value2 = 3823982;
		String key3 = " ";
		int value3 = 3823983;
		String key4 = "";
		int value4 = 3823984;
		
		try {
		map.put(key, value);
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		
		} catch (Exception e) {
		e.printStackTrace();
		}
		
		
		assertEquals("getting the correct object from the map",value, map.get(key));
		assertEquals("getting the correct object from the map",value1, map.get(key1));
		assertEquals("getting the correct object from the map",value2, map.get(key2));
		assertEquals("getting the correct object from the map",value3, map.get(key3));
		assertEquals("getting the correct object from the map",value4, map.get(key4));
	
		
	}
	
	//Documentstore tests
	
	@Test
	public void putingADocumentAlreadyThere() throws URISyntaxException 
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		String doc1 = "hellotherehellotherehellotherehellotherehellothere";
		URI uri1 = new URI(doc);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		docstore.putDocument(inputStream1, uri1);
		
		docstore.deleteDocument(uri);
		
		assertEquals("nothing is in the hashtable",null, docstore.getDocument(uri1));
		
		
	}
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
		
		assertEquals("that the document was deleted", null, docstore.getDocument(uri));
		
	}
	@Test
	public void jarcompression()
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";

	
		byte[] compressResult = docstore.compressDoc(DocumentStore.CompressionFormat.JAR, j);
		
		byte[] decompResult = docstore.decompressDoc(compressResult, DocumentStore.CompressionFormat.JAR);
		String newString = new String(decompResult);
		
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
	
	//Tests for undo redo logic
	@Test
	public void undoLogicForAdd() throws URISyntaxException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		String doc1 = "hihihihihihihihihihihi";
		URI uri1 = new URI(doc1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		docstore.putDocument(inputStream1, uri1);
		
		String doc2 = "lololololololololololololol";
		URI uri2 = new URI(doc2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());
		docstore.putDocument(inputStream2, uri2);
		
		docstore.undo();
		docstore.undo();
		docstore.undo();
		
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri2));
		
	}
	@Test
	public void undoLogicForDelete() throws URISyntaxException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		String doc1 = "hihihihihihihihihihihi";
		URI uri1 = new URI(doc1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		docstore.putDocument(inputStream1, uri1);
		
		String doc2 = "lololololololololololololol";
		URI uri2 = new URI(doc2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());
		docstore.putDocument(inputStream2, uri2);
		
		docstore.deleteDocument(uri);
		docstore.deleteDocument(uri1);
		docstore.deleteDocument(uri2);
		
		docstore.undo();
		docstore.undo();
		docstore.undo();
		
		assertEquals("checking to see all puts where undon", doc,  docstore.getDocument(uri));
		assertEquals("checking to see all puts where undon", doc1, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon", doc2, docstore.getDocument(uri2));
		
	}
	@Test
	public void undoLogicForDeleteWithUri() throws URISyntaxException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		String doc1 = "hihihihihihihihihihihi";
		URI uri1 = new URI(doc1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		docstore.putDocument(inputStream1, uri1);
		
		String doc2 = "lololololololololololololol";
		URI uri2 = new URI(doc2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());
		docstore.putDocument(inputStream2, uri2);
		
		docstore.deleteDocument(uri);
		docstore.deleteDocument(uri1);
		docstore.deleteDocument(uri2);
		
		docstore.undo(uri2);
		
	
		assertEquals("checking to see all puts where undon", doc2, docstore.getDocument(uri2));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri));
	}
	@Test
	public void undoLogicForAddWithUri() throws URISyntaxException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		String doc1 = "hihihihihihihihihihihi";
		URI uri1 = new URI(doc1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		docstore.putDocument(inputStream1, uri1);
		
		String doc2 = "lololololololololololololol";
		URI uri2 = new URI(doc2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());
		docstore.putDocument(inputStream2, uri2);
		

		docstore.undo(uri2);
		
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri2));
		assertEquals("checking to see all puts where undon",doc1, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon",doc, docstore.getDocument(uri));
	}
	
}	
