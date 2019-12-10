package edu.yu.cs.com1320.project.Impl;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class DocumentImpl implements Document
{
	
	private byte[] docInByte;
	private int docHashCode;
	private URI key;
	private CompressionFormat compressionformat;
	HashMap<String, Integer> map;
	
	
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

}
