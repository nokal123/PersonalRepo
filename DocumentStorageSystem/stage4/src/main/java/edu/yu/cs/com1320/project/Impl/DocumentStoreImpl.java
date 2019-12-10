package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import edu.yu.cs.com1320.project.DocumentStore;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

public class DocumentStoreImpl implements DocumentStore 
{

	public class SortByWords implements Comparator<DocumentImpl>
	{
		String key;
		
		@Override
		public int compare(DocumentImpl a, DocumentImpl b) 
		{
			return b.wordCount(key) - a.wordCount(key);  
		}
		public void setKey(String k)
		{
			this.key = k;
		}
	} 
	
	private SortByWords sort = new SortByWords();
	public HashTableImpl<URI, DocumentImpl> myMap = new HashTableImpl<URI, DocumentImpl>();
	private StackImpl<Command> actionStack = new StackImpl<Command>();
	private TrieImpl<DocumentImpl> wordTrie = new TrieImpl<DocumentImpl>(sort);
	private MinHeapImpl heap = new MinHeapImpl();
	
	//0 means that the user has not speificed an amount yet
	private int MaxDocumentCount = -1;
	private int MaxDocumentBytes = -1;
	
	private int BytesInTable;
	private CompressionFormat defaultComp = CompressionFormat.ZIP;
	
	
	 /**
	 * specify which compression format should be used if none is specified on a putDocument,
	 * either because the two-argument putDocument method is used or the CompressionFormat is
	 * null in the three-argument version of putDocument
	 * @param format
	 * @see DocumentStore#putDocument(InputStream, URI)
	 * @see DocumentStore#putDocument(InputStream, URI, CompressionFormat)
	 */
	public void setDefaultCompressionFormat(CompressionFormat format) 
	{
		defaultComp = format; 
	}

	//not sure what this needs to do
	@Override
    public CompressionFormat getDefaultCompressionFormat()
    {
        return defaultComp; //CompressionFormat.ZIP;
    }

    /**
     * since the user does not specify a compression format, use the default compression format
     * @param input the document being put
     * @param uri unique identifier for the document
     * @return the hashcode of the document 
     */
	@Override
	public int putDocument(InputStream input, URI uri)
	{
		if(input == null)
		{
			deleteDocument(uri);  
		}
		
		int putresult = put(input, uri, defaultComp);
		
		if(putresult == -1)
			return -1;
		else
			return putresult;	
	}
	
	 /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format compression format to use for compressing this document
     * @return the hashcode of the document
     */
	public int putDocument(InputStream input, URI uri, CompressionFormat format) 
	{
		int putresult = put(input, uri, format);
		
		if(putresult == -1)
			return -1;
		else
			return putresult;
	}
	
	/**
	 * Helper method to both putDocument methods. Allows for no long duplicate code.
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format compression format to use for compressing this document
     * @return the hashcode of the document
     * @return -1 if error
     */
	@SuppressWarnings("deprecation")
	public int put(InputStream input, URI uri, CompressionFormat compression)
	{
		String stringDoc;
		
		try {	
			stringDoc = IOUtils.toString(input);
			IOUtils.closeQuietly(input);
			
		} catch (IOException | NullPointerException npe) { return -1; }
		
		int hashOfDoc = stringDoc.hashCode();
		if(myMap.get(uri) != null && myMap.get(uri).getKey().equals(uri) 
				&& (myMap.get(uri).getDocumentHashCode() == hashOfDoc)) {
			return hashOfDoc;
		}
		
		byte[] docInByte = compressDoc(compression, stringDoc);
		
		if(docInByte == null) {
			return -1;
		}
		
		HashMap<String, Integer> wordCountMap = new HashMap<>();
		putInMap(wordCountMap, stringDoc);
		
		DocumentImpl newDoc = new DocumentImpl(docInByte, hashOfDoc, uri, compression, wordCountMap);	
		newDoc.setLastUseTime(System.currentTimeMillis());
		DocumentImpl previousEntry = myMap.get(uri);
		DocumentImpl docResult = myMap.put(uri, newDoc); //takes doc result out of map
		isTableFull(newDoc, uri, previousEntry);
		heap.insert(newDoc);
		BytesInTable += docInByte.length;
		triePut(stringDoc, newDoc);
		
		if(docResult != null) {
			trieDelete(stringDoc, docResult); 
			removeFromHeap(docResult);
			BytesInTable -= docResult.getDocument().length;
		}
		
		//creates all the functions for undo and redo and puts them into the command stack
		createFunctions(docResult, uri, newDoc, stringDoc);
	
		return hashOfDoc;
		//how do you deal with case where you wanna put something in hashtable but at max of 
		//num of doc but your not putting something in a new slot just replaceing one thats there
		
	}
	
	//checks if the hashtable is full before adding any new elements
	public boolean isTableFull(DocumentImpl doc, URI uri, DocumentImpl previousEntry)
	{
			int docSize = doc.getDocument().length;
			
			if(MaxDocumentBytes != -1 && doc.getDocument().length > MaxDocumentBytes)
			{
				throw new IllegalArgumentException("ERROR: Size of document was greater then the total amount of bytes allowed in the store!");
			}
			
			if(previousEntry != null)
			{
				if(MaxDocumentBytes == -1) { 
					return true; 
				}
				byteCheck(docSize);
				return true;	
			}
			else
			{
				 if(MaxDocumentBytes != -1) {
					byteCheck(docSize);	
				 }
				 
				 if(MaxDocumentCount != -1) {
					docCheck();
				}
				 
			}	
		return true;
		
	}
	
	//helper for isTableFull
	public void byteCheck(int docSize)
	{
 		int bytesLeft = MaxDocumentBytes - BytesInTable;
		
		if(docSize == 0)
		{
			while(BytesInTable > MaxDocumentBytes)
			{
				DocumentImpl removed = heap.removeMin();
				String docInString = getDocument(removed.getKey());
				trieDelete(docInString, removed);
				myMap.put(removed.getKey(), null);
				BytesInTable -= removed.getDocument().length;
				removeFromStack(removed);
				bytesLeft = MaxDocumentBytes - BytesInTable;
			}
		}
		//delete last used document 
		while(bytesLeft < docSize) 
		{
			//have to remove the commands from the stack
			DocumentImpl removed = heap.removeMin();
			String docInString = getDocument(removed.getKey());
			trieDelete(docInString, removed);
			myMap.put(removed.getKey(), null);
			BytesInTable -= removed.getDocument().length;
			removeFromStack(removed);
			bytesLeft = MaxDocumentBytes - BytesInTable;
		}
	}
	
	//helper for isTableFull
	public void docCheck()
	{
		int docNumLeft = MaxDocumentCount - myMap.getSize();
		//System.out.println("SIZE: "+ myMap.getSize() + " DOCNUM: " + docNumLeft);
		//delete last used document 
		while(docNumLeft < 0)  //just < makes it work for the undo logic and <= makes it work for setting reg limits on things
		{
			DocumentImpl removed = heap.removeMin();
			String docInString = getDocument(removed.getKey());
			//System.out.println("\nDOC IN STRING: " + docInString);
			trieDelete(docInString, removed);
			myMap.put(removed.getKey(), null);
			removeFromStack(removed);
			docNumLeft = MaxDocumentCount - myMap.getSize();
			//System.out.println("INSIDE WHILE: SIZE: "+ myMap.getSize() + " DOCNUM: " + docNumLeft);
		}
	}
	
	//helper for docCheck and byteCheck
	public void removeFromStack(DocumentImpl removed)
	{
		StackImpl<Command> helperStack = new StackImpl<Command>();
		
		while(actionStack.size() != 0)
		{
			Command lastCom = actionStack.pop();
			
			if(lastCom.getUri().equals(removed.getKey())) {
				break; 
			}
			helperStack.push(lastCom);
		}
		while(helperStack.size() != 0) {
			actionStack.push(helperStack.pop());	
		}
	}
	private void removeFromHeap(DocumentImpl removed)
	{
		removed.setLastUseTime(0);
		heap.reHeapify(removed);
		heap.removeMin();
	}
	
	//creates lamda functions for the command stack
	private void createFunctions(DocumentImpl docResult, URI uri, DocumentImpl newDoc, String doc)
	{
		if(docResult == null) 
		{
			long oldTime = newDoc.getLastUseTime();
			
			//creates a function for the command stack for an undo
			Function<URI,Boolean> undo = (inputUri) ->  
			{
				myMap.put(inputUri, null); 
				removeFromHeap(newDoc);
				trieDelete(doc, newDoc); 
				return true; 
			};
			//creates a function for the command stack for a redo
			Function<URI,Boolean> redo = (inputUri) -> 
			{
				myMap.put(inputUri, newDoc); 
				newDoc.setLastUseTime(oldTime);
				heap.insert(newDoc); 
				triePut(doc, newDoc); 
				return true; 
			};
			
			Command newCommand = new Command(uri, undo, redo);
			actionStack.push(newCommand);
		}
		//this is creating a command for a case of replacing a document with a new one
		else 
		{ 
			long oldTime = docResult.getLastUseTime();
			long oldTime2 = newDoc.getLastUseTime();
			
			//creates a function for the command stack for an undo
			Function<URI,Boolean> undo = (inputUri) -> 
			{ 
				 
				removeFromHeap(newDoc);
				trieDelete(doc,newDoc); 
				myMap.put(inputUri, docResult); //deletes the old one put putting in the new 
				docResult.setLastUseTime(oldTime);
				heap.insert(docResult);
				triePut(doc, docResult);
				
				
				return true; 
			};
			
			//creates a function for the command stack for a redo
			Function<URI,Boolean> redo = (inputUri) -> 
			{ 
				removeFromHeap(docResult);
				trieDelete(doc,docResult); 
				myMap.put(inputUri, newDoc); 
				newDoc.setLastUseTime(oldTime2);
				triePut(doc,newDoc); 
				heap.insert(newDoc);
				return true; 
			};
			
			Command newCommand = new Command(uri, undo, redo);
			actionStack.push(newCommand);
		}
	}
	
	public void putInMap(HashMap<String, Integer> map, String doc)
	{
		//Splits up doc into just a-z characters
		
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
	}
	
	private void triePut(String d, DocumentImpl doc)
	{
		String[] splitUp = d.split("[^a-zA-Z]+"); 
		
		for(String word : splitUp) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   wordTrie.put(wordL, doc);
		}
	}
	private void trieDelete(String d, DocumentImpl doc)
	{
		String[] splitUp = d.split("[^a-zA-Z]+"); 
		
		for(String word : splitUp) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   wordTrie.delete(wordL, doc);
		}
	}
	
	   
	 /**
	 * Compresses the document into a byte[] with specific format 
	 * @param format and String of InputStream
	 * @see DocumentStore#putDocument(InputStream, URI)
	 * @see DocumentStore#putDocument(InputStream, URI, CompressionFormat)
	 * @return null if IllegalArgumentException thrown
	 * @return compressed byte[] with specificed format 
	 * @throws  
	 */
	public byte[] compressDoc(CompressionFormat format, String doc)
	{
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

		
			if(format == CompressionFormat.JAR){
				return compressZip(output, doc, new JarArchiveOutputStream(output));
			} else if(format == CompressionFormat.ZIP) {
				return compressZip(output, doc ,new ZipArchiveOutputStream(output));
			} else if(format == CompressionFormat.GZIP) {
				return compress(output, doc, new GzipCompressorOutputStream(output));
			} else if(format == CompressionFormat.BZIP2) {
				return compress(output, doc, new BZip2CompressorOutputStream(output));		
			} else if(format == CompressionFormat.SEVENZ) {
				return sevenZCompress(doc);
				
			}
		} catch (IllegalArgumentException | IOException  e) {
			return null;
		} 
		
		return null;		
	}
	
	/**
	 * Helper method to compressDoc
	 * @return compressed byte[]
	 */
	private byte[] compress(ByteArrayOutputStream byteOutput, String doc, OutputStream compressType)
	{
		try {
		compressType.write(doc.getBytes());
	    compressType.close();
	    byte[] compressed = byteOutput.toByteArray();
		return compressed;	
		
		} catch (IllegalArgumentException | IOException  e) {
			return null;
		} 
	}
	private byte[] compressZip(ByteArrayOutputStream byteOutput, String doc, ArchiveOutputStream compressType)
	{
		
		try {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(doc.getBytes());
		ZipArchiveEntry entry = new ZipArchiveEntry("name");
		compressType.putArchiveEntry(entry);
		
		
		//does compression
		IOUtils.copy(inputStream, compressType);
		compressType.closeArchiveEntry();
	    compressType.close();
	
	   
		return byteOutput.toByteArray();	
		
		} catch (IllegalArgumentException | IOException  e) {
			return null;
		} 
	}
	private byte[] sevenZCompress(String doc)
	{
		try {
			File tempFile = new File("temp.7z");//new file
			tempFile.deleteOnExit();
			
			//makes input stream of doc and output stream of tempFile and copies inputstream into outputstream
			//ie puts doc into tempfile
			IOUtils.copy(new ByteArrayInputStream(doc.getBytes()), (new FileOutputStream(tempFile)));
			
			//makes a 7z compressor object file from your file
			SevenZOutputFile sevenZOutput = new SevenZOutputFile(tempFile);
			
			//makes a 7z archive within the file
			SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(tempFile, "document");
			
			//puts an archive in the szoutput which your file points to
			sevenZOutput.putArchiveEntry(entry);
			
			//(not writing)compresses the data which is already in your archive which is in a file
			sevenZOutput.write(doc.getBytes());
			
			sevenZOutput.closeArchiveEntry();
			//closes and finishes the 7z object
			sevenZOutput.close();
			return Files.readAllBytes(tempFile.toPath());
		
		
		} catch(IOException e) {
			return null;
		}
	}
	/**
     * @param uri the unique identifier of the document to get
     * @return the <B>uncompressed</B> document as a String, or null if no such document exists
     */
	@Override
	public String getDocument(URI uri) 
	{
		if(myMap.get(uri) == null)
			return null;
		
		byte[] compressedDoc =  myMap.get(uri).getDocument();
		myMap.get(uri).setLastUseTime(System.currentTimeMillis());
		heap.reHeapify(myMap.get(uri));
		CompressionFormat format = myMap.get(uri).getCompressionFormat();
		
		byte[] decompressedDoc = decompressDoc(compressedDoc, format);
		String output = new String(decompressedDoc);
		return output;
	}
	/**
     * @param compressed document in a byte[]
     * @param current format of byte[]
     * @return decompressed document as a byte[]
     * @return null if IOException
     */
	private byte[] decompressDoc(byte[] compressedDoc , CompressionFormat format)
	{	
		try {
			if(format == CompressionFormat.JAR){
				return decompressZip(compressedDoc);
				
			} else if(format == CompressionFormat.ZIP) {
				return decompressZip(compressedDoc);
				
			} else if(format == CompressionFormat.GZIP) {
				return decompress(new GzipCompressorInputStream(new ByteArrayInputStream(compressedDoc)));
				
			} else if(format == CompressionFormat.BZIP2) {
				return decompress(new BZip2CompressorInputStream(new ByteArrayInputStream(compressedDoc)));
				
			} else if(format == CompressionFormat.SEVENZ) {
				return sevenZDecompress(compressedDoc);
			}
		} catch (IOException e) {
			return null;
		}
		
		return null;
	}
	private byte[] decompressZip(byte [] input) 
	{
		try {
	
			File tempFile = new File("archive.zip");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, input);
			ZipFile zipFile = new ZipFile(tempFile);
			
			ZipArchiveEntry archiveEntry = zipFile.getEntry("name");
			InputStream inputStream = zipFile.getInputStream(archiveEntry);
			
			OutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream);
			zipFile.close();
			inputStream.close();
			return ((ByteArrayOutputStream) outputStream).toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	private byte[] decompress(InputStream input) 
	{
		try {
	
			byte[] deComp = IOUtils.toByteArray(input);
			input.close();
			return deComp;
			
		} catch (IOException e) {
			return null;
		}
		
	}
	private byte[] sevenZDecompress(byte[] compressedDoc)
	{
		try {
			
			File tempFile = new File("archive.7z");
			FileUtils.writeByteArrayToFile(tempFile, compressedDoc);
			
			SevenZFile sevenZFile = new SevenZFile(tempFile);
			SevenZArchiveEntry entry = sevenZFile.getNextEntry();
			
			byte[] decompressed =  new byte[(int) entry.getSize()];
			sevenZFile.read(decompressed);
			sevenZFile.close();
			
			return decompressed;
		} catch(IOException e) {
			return null;
		}
		
		
	}

	/**
     * @param uri the unique identifier of the document to get
     * @return the <B>compressed</B> version of the document uri 
     */
	@Override
	public byte[] getCompressedDocument(URI uri) 
	{
		myMap.get(uri).setLastUseTime(System.currentTimeMillis());
		heap.reHeapify(myMap.get(uri));
		return myMap.get(uri).getDocument();
	}

	/**
	 * a null value will result in the element with the key uri being deleting.
	 * this is done by passing the put document a null value
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
	@Override
	public boolean deleteDocument(URI uri) 
	{
		if(myMap.get(uri) == null)
			return false;
		
		else
		{
			DocumentImpl temp = myMap.get(uri);
			String docInString = getDocument(temp.getKey());
			BytesInTable -= temp.getDocument().length;
			trieDelete(docInString, temp);
			myMap.put(uri, null);
			long originalTime = temp.getLastUseTime();
			removeFromHeap(temp);
			
			Function<URI,Boolean> undo = (inputUri) ->  
			{ 
				
				myMap.put(inputUri,temp); 
				triePut(docInString , temp); 
				temp.setLastUseTime(originalTime);
				heap.insert(temp);
				BytesInTable += temp.getDocument().length;
				return true; 
			};
			
			Function<URI,Boolean> redo = (inputUri) -> 
			{
				myMap.put(inputUri, null); 
				BytesInTable -= temp.getDocument().length;
				trieDelete(docInString, temp); 
				return true; 
			};
			
			Command newCommand = new Command(uri, undo, redo);
			actionStack.push(newCommand);
			return true;
		}
	}


	/**
    * undo the last put or delete command
    * @return true if successfully undid command, false if not successful
    * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
    */
	@Override
	public boolean undo() throws IllegalStateException 
	{
		if(actionStack.size() == 0) {
			throw new IllegalStateException();
		}
		
		Command last = actionStack.pop();
		DocumentImpl previousEntry = myMap.get(last.getUri());
		URI uri = last.getUri();
		boolean status = last.undo();
		
		if(status == true) {
			if(myMap.get(uri) != null) {
				isTableFull(myMap.get(uri), uri, previousEntry);
			}
			
			return true;
		}
		return false;
	}

	 /**
    * undo the last put or delete that was done with the given URI as its key
    * @param uri
    * @return
    * @throws IllegalStateException if there are no actions on the command stack for the given URI
    */
	public boolean undo(URI uri) throws IllegalStateException 
	{
		if(actionStack.size() == 0) {
			throw new IllegalStateException();
		}
		
		DocumentImpl previousEntry = null;
		StackImpl<Command> helperStack = new StackImpl<Command>();
		
		while(actionStack.size() != 0)
		{
			Command lastCom = actionStack.pop();
			
			if(lastCom.getUri().equals(uri)) {
				previousEntry = myMap.get(lastCom.getUri());
				lastCom.undo();
				break;
			}
			
			lastCom.undo();
			helperStack.push(lastCom);
		}
		while(helperStack.size() != 0)
		{
			Command lastCom = helperStack.pop();
			lastCom.redo();
			actionStack.push(lastCom);	
		}	
		
		//tells me if this is an undo of a put or delete
		if(myMap.get(uri) != null) {
			isTableFull(myMap.get(uri), uri, previousEntry);
		}
		
		return false;
	}
	
	 /**
     * Retrieve all documents that contain the given key word.
     * Documents are returned in sorted, in descending order, by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return
     */
	@Override
	public List<String> search(String keyword) 
	{
		String wordL = keyword.toLowerCase(); 
		sort.setKey(wordL);
		
		List<DocumentImpl> docList = wordTrie.getAllSorted(wordL);
		List<String> sortedList = new ArrayList<String>();
		
		for(int i = 0; i < docList.size(); i++) { 
			docList.get(i).setLastUseTime(System.currentTimeMillis());
			heap.reHeapify(docList.get(i));
			String docInString = getDocument(docList.get(i).getKey());
			sortedList.add(docInString);	
		}
		
		return sortedList;
		
	}

	 /**
     * Retrieve the compressed form of all documents that contain the given key word.
     * Documents are returned in sorted, in descending order, by the number of times the keyword appears in the document
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return
     */
	@Override
	public List<byte[]> searchCompressed(String keyword) {
		
		String wordL = keyword.toLowerCase(); 
		sort.setKey(wordL);
		
		List<DocumentImpl> docList = wordTrie.getAllSorted(wordL);
		List<byte[]> sortedList = new ArrayList<byte[]>();
		
		for(int i = 0; i < docList.size(); i++)
		{
			//gets the byte[] of the doc 
			docList.get(i).setLastUseTime(System.currentTimeMillis());
			heap.reHeapify(docList.get(i));
			byte[] temp = docList.get(i).getDocument();
			sortedList.add(temp);	
		}
		
		return sortedList;
	}

	/**
    * set maximum number of documents that may be stored
    * @param limit
    */
	public void setMaxDocumentCount(int limit) {
		
		if(limit < 0) {
			throw new IllegalArgumentException();
		}
		else if(limit == 0) {
			MaxDocumentBytes = -1;
		}
		
		MaxDocumentCount = limit;
		docCheck();
	}

	/**
    * set maximum number of bytes of memory that may be used by all the compressed
    documents in memory combined
    * @param limit
    */
	public void setMaxDocumentBytes(int limit) {
		
		if(limit < 0) {
			throw new IllegalArgumentException();
		}
		else if(limit == 0) {
			MaxDocumentBytes = -1;
		}
		
		MaxDocumentBytes = limit;
		byteCheck(0);
	}





}
