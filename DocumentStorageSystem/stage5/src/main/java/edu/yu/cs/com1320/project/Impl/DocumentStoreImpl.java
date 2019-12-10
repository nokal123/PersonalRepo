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
import java.util.Objects;

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

	public class SortByWords implements Comparator<StorageElement>
	{
		String key;
		
		@Override
		public int compare(StorageElement a, StorageElement b) 
		{
			return b.tree.get(b.uri).wordCount(key) - a.tree.get(a.uri).wordCount(key);  
		}
		public void setKey(String k)
		{
			this.key = k;
		}
	} 
	public class StorageElement implements Comparable<StorageElement>
	{
		BTreeImpl tree;
		URI uri;
		
		public StorageElement (BTreeImpl tree, URI uri)
		{
			this.tree = tree;
			this.uri = uri;
		}
		@Override
		public int compareTo(StorageElement o) {
			DocumentImpl thisDoc = this.tree.get(this.uri);
			DocumentImpl thatDoc = o.tree.get(o.uri);
			
			if(thisDoc == null)
				return -1;
			
			return thisDoc.compareTo(thatDoc);
		} 
		@Override
		public boolean equals(Object o) {
	 
			// null check
			if (o == null) {
				return false;
			}
	 
			// this instance check
			if (this == o) {
				return true;
			} 
			if(o instanceof StorageElement)
			{
				if(((StorageElement) o).uri == this.uri)
				{
					return true;
				}
				return false;
			}
			else 
				return false;
		}
		@Override 
		public int hashCode()
		{
			return Objects.hashCode(uri);
		}
	}
	
	private SortByWords sort;
	private StackImpl<Command> actionStack;
	private TrieImpl<StorageElement> wordTrie;
	private MinHeapImpl heap;
	//private HashMap<URI, File> undoRedoMap = new HashMap<URI, File>();
	
	//0 means that the user has not speificed an amount yet
	private int MaxDocumentCount;
	private int MaxDocumentBytes;
	public File baseDir;
	
	private int BytesInTable;
	private CompressionFormat defaultComp;
	int currentDocuments;
	private BTreeImpl Btree;
	private DocumentIOImpl docIO;
	private File tempSerializeDir;
	
	//constructor 
	public DocumentStoreImpl(File baseDir)
	{
		this.baseDir = baseDir;
		sort = new SortByWords();
		heap = new MinHeapImpl();
		Btree = new BTreeImpl(baseDir);
		tempSerializeDir = new File(System.getProperty("user.dir") + File.separator + "tempSerializeDir");
		docIO = new DocumentIOImpl(tempSerializeDir);
		actionStack = new StackImpl<Command>();
		wordTrie = new TrieImpl<StorageElement>(sort);
		defaultComp = CompressionFormat.ZIP;
		MaxDocumentCount = -1;
		MaxDocumentBytes = -1;
		currentDocuments = 0;
	}
	public DocumentStoreImpl() {
		sort = new SortByWords();
		heap = new MinHeapImpl();
		Btree = new BTreeImpl(baseDir);
		tempSerializeDir = new File(System.getProperty("user.dir") + File.separator + "tempSerializeDir");
		docIO = new DocumentIOImpl(tempSerializeDir);
		actionStack = new StackImpl<Command>();
		wordTrie = new TrieImpl<StorageElement>(sort);
		defaultComp = CompressionFormat.ZIP;
		MaxDocumentCount = -1;
		MaxDocumentBytes = -1;
		currentDocuments = 0;
	}
	
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
	public int put(InputStream input, URI uri, CompressionFormat compression)
	{
		String stringDoc;
		
		try {	
			stringDoc = IOUtils.toString(input);
			IOUtils.closeQuietly(input);
			
		} catch (IOException | NullPointerException npe) { return -1; }
		
		int hashOfDoc = stringDoc.hashCode();
		if(Btree.get(uri) != null && Btree.get(uri).getKey().equals(uri) 
				&& (Btree.get(uri).getDocumentHashCode() == hashOfDoc)) {
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
		DocumentImpl previousEntry = Btree.get(uri);     //myMap.get(uri);
		DocumentImpl docResult = Btree.put(uri, newDoc);  //myMap.put(uri, newDoc); //takes doc result out of map
		currentDocuments++;
		isTableFull(newDoc, uri, previousEntry);
		BytesInTable += docInByte.length;
		newDoc.setStorageElement(new StorageElement(Btree, uri));
		heap.insert(newDoc.getStorageElement());
		triePut(stringDoc, newDoc);
		
		if(docResult != null) {
			trieDelete(stringDoc, docResult); 
			removeFromHeap(docResult);
			BytesInTable -= docResult.getDocument().length;
			currentDocuments--;
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
		
		if(docSize == 0) //what was i trying to do here???? I think this was for setmaxbytecount post
		{
			
			while(BytesInTable > MaxDocumentBytes)
			{
				try 
				{
					StorageElement se = heap.removeMin();
					DocumentImpl removed = se.tree.get(se.uri);
					Btree.moveToDisk(se.uri);
					BytesInTable -= removed.getDocument().length;
					currentDocuments--;
					removeFromStack(removed);
					bytesLeft = MaxDocumentBytes - BytesInTable;
					createMMFunctions(removed.getKey());
					
				} catch (Exception e) {
				e.printStackTrace();
				}
			}
		}
		//delete last used document 
		while(bytesLeft < docSize) 
		{
			
			try 
			{
				//have to remove the commands from the stack
				StorageElement se = heap.removeMin();
				DocumentImpl removed = se.tree.get(se.uri);
				Btree.moveToDisk(se.uri); //myMap.put(removed.getKey(), null);
				BytesInTable -= removed.getDocument().length;
				currentDocuments--;
				removeFromStack(removed);
				bytesLeft = MaxDocumentBytes - BytesInTable;
				createMMFunctions(removed.getKey());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//helper for isTableFull
	public void docCheck() 
	{
		int docNumLeft = MaxDocumentCount - currentDocuments;

		
		while(docNumLeft < 0)  //just < makes it work for the undo logic and <= makes it work for setting reg limits on things
		{
			try 
			{
				StorageElement se = heap.removeMin();
				DocumentImpl removed = se.tree.get(se.uri);
				Btree.moveToDisk(se.uri);  //myMap.put(removed.getKey(), null);
				currentDocuments--;
				removeFromStack(removed);
				docNumLeft = MaxDocumentCount - currentDocuments;
				createMMFunctions(removed.getKey());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}  
	
	private void createMMFunctions(URI uri) //true for num false for bytes
	{
		//creates a function for the command stack for an undo
		Function<URI,Boolean> undo = (inputUri) ->  
		{
			DocumentImpl temp = Btree.get(inputUri); //this gives it a time and brings in back into memory
			temp.setStorageElement(new StorageElement(Btree, inputUri));
			heap.insert(temp.getStorageElement());
			return false; 
		};
		//creates a function for the command stack for a redo
		Function<URI,Boolean> redo = (inputUri) -> 
		{
			try {
				
				DocumentImpl temp = Btree.get(inputUri);
				Btree.moveToDisk(inputUri);
				removeFromHeap(temp);
				
			} catch (Exception e) { e.printStackTrace(); }
			
			return false; 
		};
		
		Command newCommand = new Command(uri, undo, redo);
		actionStack.push(newCommand);
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
		
		if(removed.getStorageElement() == null) {
			removed.setStorageElement(new StorageElement(Btree, removed.getKey()));
		}
		
		heap.reHeapify(removed.getStorageElement()); 
		heap.removeMin();
	}
	
	
	//creates lamda functions for the command stack
	private void createFunctions(DocumentImpl docResult, URI uri, DocumentImpl newDoc, String doc)
	{
		
		if(docResult == null)  //ONLY DELETE IF USER CALLS DELETE
		{
			long oldTime = newDoc.getLastUseTime();
			
			//creates a function for the command stack for an undo
			Function<URI,Boolean> undo = (inputUri) ->  
			{
				DocumentImpl thisDoc =  Btree.get(inputUri);
				docIO.serialize(thisDoc);
				removeFromHeap(thisDoc);
				trieDelete(doc, thisDoc); 
				Btree.put(inputUri, null);
				return true; 
			};
			//creates a function for the command stack for a redo
			Function<URI,Boolean> redo = (inputUri) -> 
			{
				DocumentImpl thisDoc = (DocumentImpl) docIO.deserialize(inputUri);
				thisDoc.setStorageElement(new StorageElement(Btree, inputUri));
				Btree.put(inputUri, thisDoc);
				thisDoc.setLastUseTime(oldTime);
				heap.insert(thisDoc.getStorageElement()); 
				triePut(doc, thisDoc); 
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
				 
				//removeFromHeap(newDoc); dont need this either cause same uri as other doc
				DocumentImpl currentDoc =  Btree.get(inputUri);
				DocumentImpl pastDoc = (DocumentImpl) docIO.deserialize(inputUri);
				trieDelete(doc,currentDoc); 
				docIO.serialize(currentDoc);
				Btree.put(inputUri, pastDoc); //deletes the old one put putting in the new 
				pastDoc.setLastUseTime(oldTime);
				//heap.insert(new StorageElement(Btree, inputUri)); unnessasary cause the element still refrences the same uri. everything in Btree
				triePut(doc, pastDoc);
				
				
				return true; 
			};
			
			//creates a function for the command stack for a redo
			Function<URI,Boolean> redo = (inputUri) -> 
			{ 
				DocumentImpl currentDoc =  Btree.get(inputUri);
				DocumentImpl pastDoc = (DocumentImpl) docIO.deserialize(inputUri);
				
				trieDelete(doc,pastDoc); 
				Btree.put(inputUri, currentDoc); 
				currentDoc.setLastUseTime(oldTime2);
				triePut(doc,currentDoc); 
				//heap.insert(new StorageElement(Btree, inputUri)); unnessasary cause the element still refrences the same uri. everything in Btree
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
		   wordTrie.put(wordL, doc.getStorageElement());
		}
	}
	private void trieDelete(String d, DocumentImpl doc)
	{
		String[] splitUp = d.split("[^a-zA-Z]+"); 
		
		for(String word : splitUp) 
		{ //changes the word to lower case so that it is case insensitive
		   String wordL = word.toLowerCase(); 
		   wordTrie.delete(wordL, doc.getStorageElement());
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
     * @return the <B>uncompressed</B> document as a String, 
     */
	@Override
	public String getDocument(URI uri) 
	{
		DocumentImpl doc = Btree.get(uri);
		
		if(doc == null) {
			return null;
		}
		if(doc.getDeserialized() == true) { 
			isTableFull(doc, uri, null);
			createMMFunctions(uri);
			doc.setStorageElement(new StorageElement(Btree, uri));
		}
		
		byte[] compressedDoc =  doc.getDocument();
		doc.setLastUseTime(System.currentTimeMillis());
		heap.reHeapify(doc.getStorageElement());
		CompressionFormat format = doc.getCompressionFormat();
		
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
	public byte[] decompressDoc(byte[] compressedDoc , CompressionFormat format)
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
		DocumentImpl doc = Btree.get(uri);
		if(doc == null)
			return null;
		
		if(doc.getDeserialized() == true) { 
			isTableFull(doc, uri, null);
		} 
		doc.setLastUseTime(System.currentTimeMillis());
		heap.reHeapify(doc.getStorageElement());
		return doc.getDocument();
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
		DocumentImpl temp = Btree.get(uri);
		
		if(temp == null)
			return false;
		
		if(temp.getDeserialized() == true) { 
			temp.setStorageElement(new StorageElement(Btree, uri));
			heap.insert(temp.getStorageElement());
		}
		
		byte[]decompressed = decompressDoc(temp.getDocument(), temp.getCompressionFormat());
		String docInString = new String(decompressed);
		BytesInTable -= temp.getDocument().length;
		currentDocuments--;
		removeFromHeap(temp);
		trieDelete(docInString, temp);
		docIO.serialize(temp);
		Btree.put(uri, null);
		long originalTime = temp.getLastUseTime();
		
		
		Function<URI,Boolean> undo = (inputUri) ->  
		{ 
			DocumentImpl thisDoc = (DocumentImpl) docIO.deserialize(inputUri);
			thisDoc.setStorageElement(new StorageElement(Btree, inputUri));
			Btree.put(inputUri, thisDoc);
			thisDoc.setLastUseTime(originalTime);
			heap.insert(thisDoc.getStorageElement()); 
			triePut(docInString, thisDoc); 
			BytesInTable += temp.getDocument().length;
			currentDocuments++;
			return true; 
		};
		
		Function<URI,Boolean> redo = (inputUri) -> 
		{
			DocumentImpl thisDoc =  Btree.get(inputUri);
			docIO.serialize(thisDoc);
			Btree.put(inputUri, null);
			removeFromHeap(thisDoc);
			trieDelete(docInString, thisDoc); 
			return true; 
		};
		
		Command newCommand = new Command(uri, undo, redo);
		actionStack.push(newCommand);
		return true;
	
	}


	/**
    * undo the last put or delete command
    * @return true if successfully undid command, false if not successful
    * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
    */
	@Override
	public boolean undo() throws IllegalStateException 
	{
		Boolean wasAFile = false;
		Boolean isAFile = false;
		DocumentImpl previousEntry = null;
		DocumentImpl currentEntry = null;
		
		if(actionStack.size() == 0) {
			throw new IllegalStateException();
		}
		
		Command last = actionStack.pop();
		URI uri = last.getUri();
		
		try 
		{
			previousEntry = Btree.get(last.getUri());
			if(previousEntry.getDeserialized() == true) { //if the get that was made above pulls a doc back into memory, must push it back to disk
				Btree.moveToDisk(uri);
				wasAFile = true; //now we know if it WAS a file 
			}
			last.undo();
			
			currentEntry = Btree.get(last.getUri());
			if(currentEntry != null && currentEntry.getDeserialized() == true) { //if the get that was made above pulls a doc back into memory, must push it back to disk
				Btree.moveToDisk(uri);
				isAFile = true; //now we know if it IS a file 
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
		if(wasAFile == true || (currentEntry != null && isAFile == false)) {
			isTableFull(currentEntry, uri, previousEntry);
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
		
		Boolean wasAFile = false;
		Boolean isAFile = false;
		DocumentImpl previousEntry = null;
		DocumentImpl currentEntry = null;
		StackImpl<Command> helperStack = new StackImpl<Command>();
		
		while(actionStack.size() != 0)
		{
			Command lastCom = actionStack.pop();
			
			if(lastCom.getUri().equals(uri)) 
				{
					try 
					{
						previousEntry = Btree.get(lastCom.getUri());
						if(previousEntry != null && previousEntry.getDeserialized() == true) { //if the get that was made above pulls a doc back into memory, must push it back to disk
							Btree.moveToDisk(uri);
							wasAFile = true; //now we know if it WAS a file 
						}
						lastCom.undo();
						
						currentEntry = Btree.get(lastCom.getUri());
						if(currentEntry != null && currentEntry.getDeserialized() == true) { //if the get that was made above pulls a doc back into memory, must push it back to disk
							Btree.moveToDisk(uri);
							isAFile = true; //now we know if it IS a file 
						}
					} catch (Exception e) { e.printStackTrace(); }
					
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
		
		//tells me if this is an undo of a put or delete. also tells me if it was a undo of a movetodisk or movetomemory
		if(wasAFile == true || (currentEntry != null && isAFile == false)) {
			isTableFull(currentEntry, uri, previousEntry);
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
		
		List<StorageElement> elementList = wordTrie.getAllSorted(wordL);
		List<String> sortedList = new ArrayList<String>();
		
		for(StorageElement se: elementList)
		{
			DocumentImpl temp = se.tree.get(se.uri);
			temp.setLastUseTime(System.currentTimeMillis());
			heap.reHeapify(se);
			String docInString = getDocument(temp.getKey());
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
		
		List<StorageElement> elementList = wordTrie.getAllSorted(wordL);
		List<byte[]> sortedList = new ArrayList<byte[]>();
		
		for(StorageElement se: elementList)
		{
			//gets the byte[] of the doc 
			DocumentImpl docTemp = se.tree.get(se.uri);
			docTemp.setLastUseTime(System.currentTimeMillis());
			heap.reHeapify(se);
			byte[] temp = docTemp.getDocument();
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
