
package edu.yu.cs.com1320.project;

import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

public class Stage4Test
{

    @Test
    public void testMaxDocsPre() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        //this should result doc #1 and #2 being pushed out of memory when we add 3 and 4
        dsi.setMaxDocumentCount(2);

        //create and add doc1
        String str1 = "this is doc#1";
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);

        Assert.assertNull("doc1 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri1));
        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertEquals("string documents were not equal for doc3 which should not have been affected by setting maxDocs", str3, dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }

    @Test
    public void testMaxDocsPost() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();

        //create and add doc1
        String str1 = "this is doc#1";
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        Thread.sleep(50);

        //this should push doc #1 and #2 out of memory, but 3 and 4 should remain
        dsi.setMaxDocumentCount(2);

        Assert.assertNull("doc1 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri1));
        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertEquals("string documents were not equal for doc3 which should not have been affected by setting maxDocs", str3, dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }

    @Test
    public void testMaxBytesPre() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);

        //create and add doc1
        String str1 = "this is doc#1";
        int compressedLength = JDCompressionUtility.compressAsGzip(str1).length;
        //this should result doc #1 and #2 being pushed out of memory when we add 3 and 4
        dsi.setMaxDocumentBytes(compressedLength * 2);
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);
        
        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);
       
        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        Thread.sleep(50);
      

        Assert.assertNull("doc1 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri1));
        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertEquals("string documents were not equal for doc3 which should not have been affected by setting maxDocs", str3, dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }

    @Test
    public void testMaxBytesPost() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);

        //create and add doc1
        String str1 = "this is doc#1";
        int compressedLength = JDCompressionUtility.compressAsGzip(str1).length;
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        Thread.sleep(50);

        //this should push doc #1 and #2 out of memory, but 3 and 4 should remain
        dsi.setMaxDocumentBytes( compressedLength * 2);

        Assert.assertNull("doc1 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri1));
        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertEquals("string documents were not equal for doc3 which should not have been affected by setting maxDocs", str3, dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }

    @Test
    public void testBothMaxDocsAndBytesPre() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);

        //create and add doc1
        String str1 = "this is doc#1";
        int compressedLength = JDCompressionUtility.compressAsGzip(str1).length;
        dsi.setMaxDocumentBytes( compressedLength * 2);
        //this should result in all but doc #4 being out of memory
        dsi.setMaxDocumentCount(1);
        Thread.sleep(50);

        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);

        Assert.assertNull("doc1 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri1));
        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertNull("doc3 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }


    @Test
    public void testBothMaxDocsAndBytesPost() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);

        //create and add doc1
        String str1 = "this is doc#1";
        int compressedLength = JDCompressionUtility.compressAsGzip(str1).length;
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        Thread.sleep(50);

        //this should result in all but doc #4 being out of memory
        dsi.setMaxDocumentBytes( compressedLength * 2);
        dsi.setMaxDocumentCount(1);

        Assert.assertNull("doc1 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri1));
        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertNull("doc3 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }

    @Test
    public void testMaxDocsWithOneReheapify() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();

        //int compressedLength;
        //create and add doc1
        String str1 = "this is doc#1";
        //compressedLength = JDCompressionUtility.compressAsGzip(str1).length;
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        Thread.sleep(50);

        //reset doc1 to be most recent
        dsi.getDocument(uri1);
        //this should push doc #2 out of memory, others should remain
        dsi.setMaxDocumentCount(3);

        Assert.assertNull("doc2 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri2));
        Assert.assertEquals("string documents were not equal for doc1 which should not have been affected by setting maxDocs", str1, dsi.getDocument(uri1));
        Assert.assertEquals("string documents were not equal for doc3 which should not have been affected by setting maxDocs", str3, dsi.getDocument(uri3));
        Assert.assertEquals("string documents were not equal for doc4 which should not have been affected by setting maxDocs", str4, dsi.getDocument(uri4));
    }
    @Test
    public void testMaxDocsWithTwpReheapifies() throws Exception
    {
        DocumentStoreImpl dsi = new DocumentStoreImpl();

        //int compressedLength;
        //create and add doc1
        String str1 = "this is doc#1";
        //compressedLength = JDCompressionUtility.compressAsGzip(str1).length;
        URI uri1 = new URI("http://www.yu.edu/doc1");
        ByteArrayInputStream bis = new ByteArrayInputStream(str1.getBytes());
        dsi.putDocument(bis,uri1);
        Thread.sleep(50);

        //create and add doc2
        String str2 = "this is doc#2";
        URI uri2 = new URI("http://www.yu.edu/doc2");
        bis = new ByteArrayInputStream(str2.getBytes());
        dsi.putDocument(bis,uri2);
        Thread.sleep(50);

        //create and add doc3
        String str3 = "this is doc#3";
        URI uri3 = new URI("http://www.yu.edu/doc3");
        bis = new ByteArrayInputStream(str3.getBytes());
        dsi.putDocument(bis,uri3);
        Thread.sleep(50);

        //create and add doc4
        String str4 = "this is doc#4";
        URI uri4 = new URI("http://www.yu.edu/doc4");
        bis = new ByteArrayInputStream(str4.getBytes());
        dsi.putDocument(bis,uri4);
        Thread.sleep(50);

        //reset doc1 to be most recent
        dsi.getDocument(uri1);
        Thread.sleep(50);
        dsi.getDocument(uri2);
        //this should push doc #3 and doc #4 out of memory, 1 and 2 should remain
        dsi.setMaxDocumentCount(2);

        Assert.assertNull("doc3 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri3));
        Assert.assertNull("doc4 should have been pushed out as the LRU doc, but is still there",dsi.getDocument(uri4));
        Assert.assertEquals("string documents were not equal for doc1 which should not have been affected by setting maxDocs", str1, dsi.getDocument(uri1));
        Assert.assertEquals("string documents were not equal for doc2 which should not have been affected by setting maxDocs", str2, dsi.getDocument(uri2));
    }
}