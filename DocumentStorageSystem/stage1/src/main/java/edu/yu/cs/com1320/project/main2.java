package edu.yu.cs.com1320.project;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import edu.yu.cs.com1320.project.DocumentStore.CompressionFormat;

public class main2 {

	public static void main(String[] args) throws IOException 
	{ 
		
		DocumentStoreImpl docstore = new DocumentStoreImpl();
		
		String j = "I like DonoutsndjnsefoiwejfiojqwkwejifwkijefnkiojfekliohfI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";
		//docstore.sevenZCompress(j);
		
		System.out.println("Compression and Decompression for sevenz");
		System.out.println("Compression:");
		byte[] compressResult = docstore.compressDoc(DocumentStore.CompressionFormat.SEVENZ, j);
		System.out.println("Decompression of same string:");
		byte[] decompResult = docstore.decompressDoc(compressResult, DocumentStore.CompressionFormat.SEVENZ);
		String newString = new String(decompResult);
		System.out.println("are the strings the same?: " + j.equals(newString));		
		
		
		
		
		
		/* 
		String i = "I like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like DonoutsI like Donouts ";
		byte[] input = docstore.sevenZCompress(i);
		byte[] in = docstore.sevenZDecompress(input);
		String newString1 = new String(in);
		System.out.println("are the strings the same?: " + j.equals(newString1));  
		
		 ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] temp = j.getBytes();
		ZipArchiveOutputStream out = new ZipArchiveOutputStream(output);
		//out.writeOut(temp);
		out.close();
		String s = new String(temp);
		System.out.println(s);
		*/
		
	}
		
}


