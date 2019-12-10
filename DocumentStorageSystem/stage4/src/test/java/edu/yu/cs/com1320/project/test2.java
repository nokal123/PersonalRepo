package edu.yu.cs.com1320.project;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

import static org.junit.Assert.*;
import edu.yu.cs.com1320.project.Impl.*;

import edu.yu.cs.com1320.project.Impl.HashTableImpl.HashElement;

import org.junit.Test;

public class test2 
{ 
	
	@Test
	public void undoLogicForDeleteWithUriandLimit() throws URISyntaxException, InterruptedException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());

		String doc1 = "hihihihihihihihihihihi";
		URI uri1 = new URI(doc1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());

		String doc2 = "lololololololololololololol";
		URI uri2 = new URI(doc2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());

		String doc3 = "hehehehehehehehehhehehehe";
		URI uri3 = new URI(doc3);
		InputStream inputStream3 = new ByteArrayInputStream(doc3.getBytes());
		
		String doc4 = "hohohohohohohohohohohohohoh";
		URI uri4 = new URI(doc4);
		InputStream inputStream4 = new ByteArrayInputStream(doc4.getBytes());
		
		docstore.putDocument(inputStream, uri);
		Thread.sleep(10);
		docstore.putDocument(inputStream1, uri1);
		Thread.sleep(10);
		docstore.putDocument(inputStream2, uri2);
		Thread.sleep(10);
		docstore.putDocument(inputStream3, uri3);
		Thread.sleep(10);
		docstore.putDocument(inputStream4, uri4);
		
		docstore.deleteDocument(uri);
		docstore.deleteDocument(uri1);
		docstore.deleteDocument(uri2);
		
		docstore.setMaxDocumentCount(2);
		
		System.out.println("CURRENT DOC SIZE: " + docstore.myMap.getSize());
		
		assertEquals("checking to see if in before undo",doc3, docstore.getDocument(uri3));
		assertEquals("checking to see if in before undo",doc4, docstore.getDocument(uri4));
		assertEquals("checking to see if deleted before undo",null, docstore.getDocument(uri));
		
		docstore.undo(uri);
		//its taking out the first document cause its original time was set back 
		//when it was readded which was the oldest time
	
		assertEquals("checking to see all puts where undon", null, docstore.getDocument(uri2));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon",doc4, docstore.getDocument(uri4));
		assertEquals("checking to see all puts where undon",doc, docstore.getDocument(uri)); 
		
		//the one that gets deleted cause of space 
		assertEquals("checking to see if doc3 was deleted",null, docstore.getDocument(uri3));
		
	}
	/**
	@Test
	public void puttingOverTheLimit() throws URISyntaxException, InterruptedException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String doc = "hellotherehellotherehellotherehellotherehellothere";
		URI uri = new URI(doc);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());

		String doc1 = "hihihihihihihihihihihi";
		URI uri1 = new URI(doc1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		
		String doc2 = "lololololololololololololol";
		URI uri2 = new URI(doc2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());

		String doc3 = "hehehehehehehehehhehehehe";
		URI uri3 = new URI(doc3);
		InputStream inputStream3 = new ByteArrayInputStream(doc3.getBytes());
		
		String doc4 = "hohohohohohohohohohohohohoh";
		URI uri4 = new URI(doc4);
		InputStream inputStream4 = new ByteArrayInputStream(doc4.getBytes());
		
		docstore.putDocument(inputStream, uri);
		Thread.sleep(5);
		docstore.putDocument(inputStream1, uri1);
		Thread.sleep(5);
		docstore.putDocument(inputStream2, uri2);
		Thread.sleep(5);
		
		docstore.setMaxDocumentCount(3);
		
		docstore.putDocument(inputStream3, uri3);
		Thread.sleep(5);
		docstore.putDocument(inputStream4, uri4);
		
		
		System.out.println("CURRENT DOC SIZE: " + docstore.myMap.getSize());
		
		assertEquals("checking to see all puts where undon",doc3, docstore.getDocument(uri3));
		assertEquals("checking to see all puts where undon",doc4, docstore.getDocument(uri4));
		assertEquals("checking to see all puts where undon", doc2, docstore.getDocument(uri2));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri));
		
	}
	*/
}	
