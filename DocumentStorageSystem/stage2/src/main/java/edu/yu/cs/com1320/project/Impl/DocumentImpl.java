package edu.yu.cs.com1320.project.Impl;
import java.net.URI;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class DocumentImpl implements Document
{
	
	byte[] docInByte;
	int docHashCode;
	URI key;
	CompressionFormat compressionformat;
	
	public DocumentImpl(byte[] doc, int docHash, URI key, CompressionFormat compFor)
	{
		docInByte = doc;
		docHashCode = docHash; 
		this.key = key;
		compressionformat = compFor;
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

}
