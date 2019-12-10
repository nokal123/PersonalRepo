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
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl.SortByWords;
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
	
	//---------------------------------------------------------------------------------------------------
	
	//all trie tests specificlly getAllSorted
	
	
	@Test
	public void trieHappyPath() throws URISyntaxException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		SortByWords sort = docstore.new SortByWords(); //tried with my own sortby words still had prob
		TrieImpl<DocumentImpl> myTrie = new TrieImpl<DocumentImpl>(sort);
		sort.setKey("hello");
		
		String doc = "HELLO HELLO HELLO HELLO HELLO HELLO";
		String path = "aaaaaaaaaaa";
		HashMap<String, Integer> wordCountMap = new HashMap<>();
		docstore.putInMap(wordCountMap, doc);
		URI uri = new URI(path);
		byte[] docInByte = docstore.compressDoc(CompressionFormat.ZIP, doc);
		DocumentImpl newDoc = new DocumentImpl(docInByte, doc.hashCode(), uri, CompressionFormat.ZIP, wordCountMap); 
		
		String[] splitUp = doc.split("[^a-zA-Z]+");
		for(String word : splitUp) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   myTrie.put(wordL, newDoc);
		}
		
		String doc1 = "Hello I am John, friends with noah.../ Hello, would you like to play?";
		String path1 = "bbbbbbbbbb";
		HashMap<String, Integer> wordCountMap1 = new HashMap<>();
		docstore.putInMap(wordCountMap1, doc1);
		URI uri1 = new URI(path1);
		byte[] docInByte1 = docstore.compressDoc(CompressionFormat.ZIP, doc1);
		DocumentImpl newDoc1 = new DocumentImpl(docInByte1, doc1.hashCode(), uri1, CompressionFormat.ZIP, wordCountMap1);	
		
		String[] splitUp1 = doc1.split("[^a-zA-Z]+");
		for(String word : splitUp1) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   myTrie.put(wordL, newDoc1);
		}
		
		String doc2 = "Hello Hello Hello!!! i am NoAh there is no point to THIS";
		String path2 = "cccccccc";
		HashMap<String, Integer> wordCountMap2 = new HashMap<>();
		docstore.putInMap(wordCountMap2, doc2);
		URI uri2 = new URI(path2);
		byte[] docInByte2 = docstore.compressDoc(CompressionFormat.ZIP, doc2);
		DocumentImpl newDoc2 = new DocumentImpl(docInByte2, doc2.hashCode(), uri2, CompressionFormat.ZIP, wordCountMap2);	
		
		String[] splitUp2 = doc2.split("[^a-zA-Z]+");
		for(String word : splitUp2) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   myTrie.put(wordL, newDoc2);
		}
		
		String doc3 = "yo. sup";
		String path3 = "ddddddddd";
		HashMap<String, Integer> wordCountMap3 = new HashMap<>();
		docstore.putInMap(wordCountMap3, doc3);
		URI uri3 = new URI(path3);
		byte[] docInByte3 = docstore.compressDoc(CompressionFormat.ZIP, doc3);
		DocumentImpl newDoc3 = new DocumentImpl(docInByte3, doc3.hashCode(), uri3, CompressionFormat.ZIP, wordCountMap3);	
		
		String[] splitUp3 = doc3.split("[^a-zA-Z]+");
		for(String word : splitUp3) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   myTrie.put(wordL, newDoc3);
		}
		
		String doc4 = "Hello there i Am Noah Kalandar. I like fires? ";
		String path4 = "eeeeeeeeee";
		HashMap<String, Integer> wordCountMap4 = new HashMap<>();
		docstore.putInMap(wordCountMap4, doc4);
		URI uri4 = new URI(path4);
		byte[] docInByte4 = docstore.compressDoc(CompressionFormat.ZIP, doc4);
		DocumentImpl newDoc4 = new DocumentImpl(docInByte4, doc4.hashCode(), uri4, CompressionFormat.ZIP, wordCountMap4);	
		
		String[] splitUp4 = doc4.split("[^a-zA-Z]+");
		for(String word : splitUp4) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   myTrie.put(wordL, newDoc4);
		}
		
		List<URI> expected = new ArrayList<URI>();
		expected.add(newDoc.getKey());
		expected.add(newDoc2.getKey());
		expected.add(newDoc1.getKey());
		expected.add(newDoc4.getKey());
		
		List<DocumentImpl> docTemp = myTrie.getAllSorted("hello");
		List<URI> result = new ArrayList<URI>();
		for(DocumentImpl temp: docTemp) {
			result.add(temp.getKey());
		}
		
		
		assertEquals("checking to see all puts where sucusessfull and all gets are",expected,result);
		
		
		
	}
	@Test
	public void wordCountHappyPath() throws URISyntaxException 
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String doc = "Hello Hello Hello Hello";
		String path = "aaaaaaaaaaa";
		HashMap<String, Integer> wordCountMap = new HashMap<>();
		docstore.putInMap(wordCountMap, doc);
		URI uri = new URI(path);
		byte[] docInByte = docstore.compressDoc(CompressionFormat.ZIP, doc);
		DocumentImpl newDoc = new DocumentImpl(docInByte, doc.hashCode(), uri, CompressionFormat.ZIP, wordCountMap); 
		
		assertEquals("wordcount", 4 ,newDoc.wordCount("Hello"));
	}
	@Test
	public void comparatorTest() 
	{
		Comparator<Integer> intCompare = new Comparator<Integer>() 
		{
			@Override
			public int compare(Integer one, Integer two) {
				// TODO Auto-generated method stub
				return two - one;
			}
		} ;
		
		TrieImpl<Integer> myTrie = new TrieImpl<Integer>(intCompare);
		
		String s = "hello";
		int i = 4;
		myTrie.put(s, i);
		
		String s1 = "hello";
		int i1 = 3;
		myTrie.put(s1, i1);

		String s2 = "hello";
		int i2 = 5;
		myTrie.put(s2, i2);

		String s3 = "hello";
		int i3 = 10;
		myTrie.put(s3, i3);
		
		List<Integer> expected = new ArrayList<Integer>();
		expected.add(i3);
		expected.add(i2);
		expected.add(i);
		expected.add(i1);
		
		assertEquals("generic", expected ,myTrie.getAllSorted("hello"));
		
			
	}
	@Test
	public void everythingElse() throws URISyntaxException 
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		String doc = "hello there hello ";
		String d = "aaaa";
		URI uri = new URI(d);
		InputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		docstore.putDocument(inputStream, uri);
		
		String doc1 = "there hello ";
		String d1 = "bbbb";
		URI uri1 = new URI(d1);
		InputStream inputStream1 = new ByteArrayInputStream(doc1.getBytes());
		docstore.putDocument(inputStream1, uri1);
		
		String doc2 = "NAAAAAAAA";
		String d2 = "cccc";
		URI uri2 = new URI(d2);
		InputStream inputStream2 = new ByteArrayInputStream(doc2.getBytes());
		docstore.putDocument(inputStream2, uri2);
		
		List<String> temp = new ArrayList<String>();
		temp.add(doc);
		temp.add(doc1);
		
		assertEquals("everythingElse ", temp ,docstore.search("hello"));
	
	}
	
	
	//document HashMap
	//new command stacks with trieDelete and triePut for put and delete 
	// docImpl search
	//docimpl searchCommpressed 
	
}	
