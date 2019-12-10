package edu.yu.cs.com1320.project;
//Made by Shalom Gottesman, edited and changed by Noah Kalandar

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.JDCompressionUtility;

@SuppressWarnings("unused")
public class DocumentStoreTestStage5 {
	final static private boolean Trace = false; //use to trace testing
	private File testingSubDirectory;
	
	
	@Test public void initialize() {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 1 initialize");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
	}
	@Test public void addSingle() throws URISyntaxException {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 2 addSingle");}
		DocumentStoreImpl dsi = new DocumentStoreImpl();
		URI uri = new URI("one.doca");
		InputStream in = new ByteArrayInputStream("this is a document".getBytes());
		dsi.putDocument(in, uri);
	}
	
	@Test public void testUndoNullInput() throws URISyntaxException, IOException {//this tests undo of a null overwrite put doc
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 5 testUndoNullInput");}
		int x = setBaseDir();
		DocumentStoreImpl dsi = new DocumentStoreImpl(testingSubDirectory);
		//test a put sends two documents to disk, one is called via a search, then undo the origional
		//create and add doc1
        String str1 = "this is doc#1"; //size: 127
        URI uri1 = new URI("http://www.yu.edu/doc1"); 
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        sleep();
        //create and add doc2
        String str2 = "this is doc#2 this is doc#2"; //size, 131
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        sleep();
        //create and add doc3
        String str3 = "this is doc#3 this is doc#3 this is doc#3"; //size, 131
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        sleep();
        //create and add doc4
        String str4 = "this is doc#4 this is doc#4 this is doc#4 this is doc#4"; //size, 132
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        dsi.putDocument(null, uri4);
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertFalse(str4.equals(dsi.getDocument(uri4)));
        
        dsi.undo(uri4);
        
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertTrue(str4.equals(dsi.getDocument(uri4)));
        cleanUpFolder();
	}
	
	@Test public void testUndoDelete() throws URISyntaxException, IOException {//this tests undo of a null overwrite put doc
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 6 testUndoDelete");}
		int x = setBaseDir();
		DocumentStoreImpl dsi = new DocumentStoreImpl(testingSubDirectory);
		//test a put sends two documents to disk, one is called via a search, then undo the origional
		//create and add doc1
        String str1 = "this is doc#1"; //size: 127
        URI uri1 = new URI("http://www.yu.edu/doc1"); 
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        sleep();
        //create and add doc2
        String str2 = "this is doc#2 this is doc#2"; //size, 131
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        sleep();
        //create and add doc3
        String str3 = "this is doc#3 this is doc#3 this is doc#3"; //size, 131
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        sleep();
        //create and add doc4
        String str4 = "this is doc#4 this is doc#4 this is doc#4 this is doc#4"; //size, 132
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        sleep();
        
        dsi.deleteDocument(uri4);
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertFalse(str4.equals(dsi.getDocument(uri4)));
        
        dsi.undo(uri4);
        
        assertTrue(str1.equals(dsi.getDocument(uri1)));
        assertTrue(str2.equals(dsi.getDocument(uri2)));
        assertTrue(str3.equals(dsi.getDocument(uri3)));
        assertTrue(str4.equals(dsi.getDocument(uri4)));
        cleanUpFolder();
	}
	
	@Test public void undoCallsDeletedDoc() throws URISyntaxException {
		if (Trace == true) {System.out.println("\nDocumentStoreTest stage 5 Test: Test 7 undoCallsDeletedDoc");}
		int x = setBaseDir();
		DocumentStoreImpl dsi = new DocumentStoreImpl(testingSubDirectory);
		dsi.setMaxDocumentCount(3);
		String[] strs = createStrings(5);
		URI[] uris = createURIs(5);
		for (int z = 0; z < 5; z++) {
			ByteArrayInputStream bis = new ByteArrayInputStream(strs[z].getBytes());
			dsi.putDocument(bis, uris[z]);
			sleep();
		}
		assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc1.json").exists());
        assertTrue (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc3.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc4.json").exists());
        assertFalse(new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc5.json").exists());
        
        dsi.deleteDocument(uris[1]);//deletes doc 2
        assertFalse (new File(System.getProperty("user.dir") + "/testing/" +x + "/www.yu.edu/doc2.json").exists());
        assertNull(dsi.getDocument(uris[1]));
        
        
        dsi.undo(uris[4]);//undoes doc 5, which would reput doc 2
        assertNull(dsi.getDocument(uris[4]));
        assertNull(dsi.getDocument(uris[1]));
        cleanUpFolder();
	} 


	private String[] createStrings(int x) {
		String[] strs = new String[x]; 
		for (int y = 1; y <= x; y++) {
			strs[y - 1] = generateString(y);
		}
		return strs;
	}
	
	private String generateString(int x) {
		String str = "";
		for (int y = 0; y < x; y++) {
			str = str + "this is doc " + x + " ";
		}
		return str;
	}
	
	private URI[] createURIs(int x) throws URISyntaxException {
		URI[] uris = new URI[x];
		for (int y = 1; y <= x; y++) {
			URI uri = new URI("http://www.yu.edu/doc" + y);
			uris[y - 1] = uri;
		}
		return uris;
	}
	private void cleanUpFolder() {
		try {
			FileUtils.deleteDirectory(testingSubDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}//this will delete all the directory the test creates after the test is completed
	}
	
	private int setBaseDir() {
		Random rand = new Random();
		int x  = rand.nextInt();
		File dir = new File(System.getProperty("user.dir") + "/testing/" + x + "/");
		testingSubDirectory = dir;
		return x;
	}
	
	private void sleep() {
    	try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
