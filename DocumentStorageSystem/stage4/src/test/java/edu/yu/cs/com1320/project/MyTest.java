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
		Thread.sleep(5);
		docstore.putDocument(inputStream1, uri1);
		Thread.sleep(5);
		docstore.putDocument(inputStream2, uri2);
		Thread.sleep(5);
		docstore.putDocument(inputStream3, uri3);
		Thread.sleep(5);
		docstore.putDocument(inputStream4, uri4);
		
		
		docstore.deleteDocument(uri);
		docstore.deleteDocument(uri1);
		docstore.deleteDocument(uri2);
		
		docstore.setMaxDocumentCount(2);
		
		
		docstore.undo(uri);
		
		assertEquals("checking to see all puts where undone",doc, docstore.getDocument(uri));
		assertEquals("checking to see all puts where undone", null, docstore.getDocument(uri2));
		assertEquals("checking to see all puts where undone",null, docstore.getDocument(uri1));
		assertEquals("checking to see if doc3 was deleted",null, docstore.getDocument(uri3));

		
	}
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
		//assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri1));
		assertEquals("checking to see all puts where undon",null, docstore.getDocument(uri));
	}
	@Test
	public void undoLogicForDeleteWithUri() throws URISyntaxException, InterruptedException
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
		docstore.putDocument(inputStream1, uri1);
		docstore.putDocument(inputStream2, uri2);
		docstore.putDocument(inputStream3, uri3);
		docstore.putDocument(inputStream4, uri4);
		
		
		docstore.deleteDocument(uri);
		docstore.deleteDocument(uri1);
		docstore.deleteDocument(uri2);
		
		
		docstore.undo(uri);
		
		assertEquals("checking to see all puts where undone",doc, docstore.getDocument(uri));
		assertEquals("checking to see all puts where undone", null, docstore.getDocument(uri2));
		assertEquals("checking to see all puts where undone",null, docstore.getDocument(uri1));
		assertEquals("checking to see if doc3 was deleted",doc3, docstore.getDocument(uri3));
		assertEquals("checking to see if doc3 was deleted",doc4, docstore.getDocument(uri4));
		
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
	
	//--------------------------------------------------------------------------------
	//HEAP STUFFFFFF
	

	@Test
	public void minHeapTest() throws URISyntaxException, InterruptedException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		MinHeapImpl myHeap = new MinHeapImpl();
		
		String doc = "HELLO HELLO HELLO HELLO HELLO HELLO";
		String path = "aaaaaaaaaaa";
		HashMap<String, Integer> wordCountMap = new HashMap<>();
		URI uri = new URI(path);
		byte[] docInByte = docstore.compressDoc(CompressionFormat.ZIP, doc);
		DocumentImpl newDoc = new DocumentImpl(docInByte, doc.hashCode(), uri, CompressionFormat.ZIP, wordCountMap); 
		newDoc.setLastUseTime(System.currentTimeMillis());
		
		String doc1 = "Hello I am John, friends with noah.../ Hello, would you like to play?";
		String path1 = "bbbbbbbbbb";
		HashMap<String, Integer> wordCountMap1 = new HashMap<>();
		URI uri1 = new URI(path1);
		byte[] docInByte1 = docstore.compressDoc(CompressionFormat.ZIP, doc1);
		DocumentImpl newDoc1 = new DocumentImpl(docInByte1, doc1.hashCode(), uri1, CompressionFormat.ZIP, wordCountMap1);	
		newDoc1.setLastUseTime(System.currentTimeMillis());
		
		String doc2 = "Hello Hello Hello!!! i am NoAh there is no point to THIS";
		String path2 = "cccccccc";
		HashMap<String, Integer> wordCountMap2 = new HashMap<>();
		URI uri2 = new URI(path2);
		byte[] docInByte2 = docstore.compressDoc(CompressionFormat.ZIP, doc2);
		DocumentImpl newDoc2 = new DocumentImpl(docInByte2, doc2.hashCode(), uri2, CompressionFormat.ZIP, wordCountMap2);	
		newDoc2.setLastUseTime(System.currentTimeMillis());
		
		String doc3 = "yo. sup";
		String path3 = "ddddddddd";
		HashMap<String, Integer> wordCountMap3 = new HashMap<>();
		URI uri3 = new URI(path3);
		byte[] docInByte3 = docstore.compressDoc(CompressionFormat.ZIP, doc3);
		DocumentImpl newDoc3 = new DocumentImpl(docInByte3, doc3.hashCode(), uri3, CompressionFormat.ZIP, wordCountMap3);
		newDoc3.setLastUseTime(System.currentTimeMillis());
		
		
		myHeap.insert(newDoc);
		Thread.sleep(5);
		myHeap.insert(newDoc1);
		Thread.sleep(5);
		myHeap.insert(newDoc2);
		Thread.sleep(5);
		myHeap.insert(newDoc3);
		
		newDoc3.setLastUseTime(0);
		myHeap.reHeapify(newDoc3);
		
		assertEquals("reheapafied",newDoc3 ,myHeap.removeMin());
		
	}
	public void removeFromHeapTest() throws URISyntaxException, InterruptedException
	{
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		MinHeapImpl myHeap = new MinHeapImpl();
		
		String doc = "HELLO HELLO HELLO HELLO HELLO HELLO";
		String path = "aaaaaaaaaaa";
		HashMap<String, Integer> wordCountMap = new HashMap<>();
		URI uri = new URI(path);
		byte[] docInByte = docstore.compressDoc(CompressionFormat.ZIP, doc);
		DocumentImpl newDoc = new DocumentImpl(docInByte, doc.hashCode(), uri, CompressionFormat.ZIP, wordCountMap); 
		newDoc.setLastUseTime(System.currentTimeMillis());
		
		String doc1 = "Hello I am John, friends with noah.../ Hello, would you like to play?";
		String path1 = "bbbbbbbbbb";
		HashMap<String, Integer> wordCountMap1 = new HashMap<>();
		URI uri1 = new URI(path1);
		byte[] docInByte1 = docstore.compressDoc(CompressionFormat.ZIP, doc1);
		DocumentImpl newDoc1 = new DocumentImpl(docInByte1, doc1.hashCode(), uri1, CompressionFormat.ZIP, wordCountMap1);	
		newDoc1.setLastUseTime(System.currentTimeMillis());
		
		String doc2 = "Hello Hello Hello!!! i am NoAh there is no point to THIS";
		String path2 = "cccccccc";
		HashMap<String, Integer> wordCountMap2 = new HashMap<>();
		URI uri2 = new URI(path2);
		byte[] docInByte2 = docstore.compressDoc(CompressionFormat.ZIP, doc2);
		DocumentImpl newDoc2 = new DocumentImpl(docInByte2, doc2.hashCode(), uri2, CompressionFormat.ZIP, wordCountMap2);	
		newDoc2.setLastUseTime(System.currentTimeMillis());
		
		String doc3 = "yo. sup";
		String path3 = "ddddddddd";
		HashMap<String, Integer> wordCountMap3 = new HashMap<>();
		URI uri3 = new URI(path3);
		byte[] docInByte3 = docstore.compressDoc(CompressionFormat.ZIP, doc3);
		DocumentImpl newDoc3 = new DocumentImpl(docInByte3, doc3.hashCode(), uri3, CompressionFormat.ZIP, wordCountMap3);
		newDoc3.setLastUseTime(System.currentTimeMillis());
		
		
		myHeap.insert(newDoc);
		Thread.sleep(5);
		myHeap.insert(newDoc1);
		Thread.sleep(5);
		myHeap.insert(newDoc2);
		Thread.sleep(5);
		myHeap.insert(newDoc3);
		
		
		
		assertEquals("reheapafied",newDoc3 ,myHeap.removeMin());
	}
	
	
	
	
}	
