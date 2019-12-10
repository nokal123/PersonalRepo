package edu.yu.cs.com1320.project.Impl;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl.StorageElement;

public class DocumentImpl implements Document, Comparable<Document>
{
	
	private byte[] docInByte;
	private int docHashCode;
	private URI key;
	private CompressionFormat compressionformat;
	private HashMap<String, Integer> map;
	private long timeStamp;
	private boolean deserialized;
	private StorageElement storageElement;
	
	
	public DocumentImpl(byte[] doc, int docHash, URI key, CompressionFormat compFor, HashMap<String, Integer> map)
	{
		docInByte = doc;
		docHashCode = docHash; 
		this.key = key;
		compressionformat = compFor;
		this.map = map;
		
	}
	
	public byte[] getDocument() {

		return docInByte;
	}

	public int getDocumentHashCode() {
		
		return docHashCode;
	}

	public URI getKey() {
		
		return key;
	}

	public CompressionFormat getCompressionFormat() {
		
		return compressionformat;
	}
	public HashMap<String, Integer> getMap() {
		
		return map;
	}

	 /**
     * how many times does the given word appear in the document?
     * @param word
     * @return
     */
	@Override
	public int wordCount(String word) 
	{
		String wordL = word.toLowerCase();
		Integer result = map.get(wordL);
		if(result == null)
			return 0;

		return result;
	}

	@Override
	public long getLastUseTime() {
		return timeStamp;
	}

	@Override
	public void setLastUseTime(long timeInMilliseconds) {
		timeStamp = timeInMilliseconds;
	}

	@Override
	public int compareTo(Document o) {
		
		if(this.getLastUseTime() > o.getLastUseTime())
			return 1;
		
		else if(this.getLastUseTime() < o.getLastUseTime())
			return -1;
		
		else 
			return 0;
	
	}

	@Override
	public HashMap<String, Integer> getWordMap() {
		return map;
	}

	@Override
	public void setWordMap(HashMap<String, Integer> wordMap) {
		map = wordMap;
		
	}
	protected void setDeserialized(Boolean status)
	{
		this.deserialized = status;
	}
	protected Boolean getDeserialized()
	{
		return deserialized;
	}
	protected void setStorageElement(StorageElement se)
	{
		this.storageElement = se;
	}
	protected StorageElement getStorageElement()
	{
		return storageElement;
	}
}
