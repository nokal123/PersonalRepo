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

	private class SortByWords implements Comparator<DocumentImpl>
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
	private HashTableImpl<URI, DocumentImpl> myMap = new HashTableImpl<URI, DocumentImpl>();
	private StackImpl<Command> actionStack = new StackImpl<Command>();
	private TrieImpl<DocumentImpl> wordTrie = new TrieImpl<DocumentImpl>(sort);
	
	
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
	private int put(InputStream input, URI uri, CompressionFormat compression)
	{
		String doc;
		
		try {	
			doc = IOUtils.toString(input);
			IOUtils.closeQuietly(input);
			
		} catch (IOException | NullPointerException npe) { return -1; }
		
		int hashOfDoc = doc.hashCode();
		if(myMap.get(uri) != null && myMap.get(uri).getKey().equals(uri) 
				&& (myMap.get(uri).getDocumentHashCode() == hashOfDoc)) {
			return hashOfDoc;
		}
		
		byte[] docInByte = compressDoc(compression, doc);
		
		if(docInByte == null) {
			return -1;
		}
		HashMap<String, Integer> wordCountMap = new HashMap<>();
		putInMap(wordCountMap, doc);
		DocumentImpl newDoc = new DocumentImpl(docInByte, hashOfDoc, uri, compression, wordCountMap);	
		DocumentImpl docResult = myMap.put(uri, newDoc);
		
		
		if(docResult == null) {
			triePut(doc, newDoc);
			Function<URI,Boolean> undo = (inputUri) ->  { myMap.put(inputUri, null); trieDelete(doc, newDoc); return true; };
			Function<URI,Boolean> redo = (inputUri) -> { myMap.put(inputUri, newDoc); triePut(doc, newDoc); return true; };
			Command newCommand = new Command(uri, undo, redo);
			actionStack.push(newCommand);
		}
		else { //this is creating a command for a case of replacing a document with a new one
			triePut(doc, newDoc); //puts newdoc into trie
			trieDelete(doc, docResult); //deletes the document it replaced 
			Function<URI,Boolean> undo = (inputUri) -> { myMap.put(inputUri, docResult); triePut(doc, docResult); trieDelete(doc,newDoc); return true; };
			Function<URI,Boolean> redo = (inputUri) -> { myMap.put(uri, newDoc); triePut(doc,newDoc); trieDelete(doc,docResult); return true; };
			Command newCommand = new Command(uri, undo, redo);
			actionStack.push(newCommand);
		}
		
		
		
		return hashOfDoc;
		
	}
	private void putInMap(HashMap<String, Integer> map, String doc)
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
	private byte[] compressDoc(CompressionFormat format, String doc)
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
			byte[] r  = Files.readAllBytes(tempFile.toPath());
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
     * @return the <B>compressed</B> version of the document
     */
	@Override
	public byte[] getCompressedDocument(URI uri) 
	{
		
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
			
			Function<URI,Boolean> undo = (inputUri) ->  { myMap.put(inputUri,temp); triePut(docInString , temp); return true; };
			Function<URI,Boolean> redo = (inputUri) -> { myMap.put(inputUri, null); trieDelete(docInString, temp); return true; };
			Command newCommand = new Command(uri, undo, redo);
			actionStack.push(newCommand);
			
			myMap.put(uri, null);
			trieDelete(docInString, temp);
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
		boolean status = last.undo();
		if(status == true) {
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
		
		StackImpl<Command> helperStack = new StackImpl<Command>();
		
		while(actionStack.size() != 0)
		{
			Command lastCom = actionStack.pop();
			
			if(lastCom.getUri().equals(uri)) {
				lastCom.undo();
				break;
			}
			
			helperStack.push(lastCom);	
		}
		while(helperStack.size() != 0)
		{
			Command lastCom = helperStack.pop();
			lastCom.redo();
			actionStack.push(lastCom);	
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
			byte[] temp = docList.get(i).getDocument();
			sortedList.add(temp);	
		}
		
		return sortedList;
	}





}
