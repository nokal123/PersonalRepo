package edu.yu.cs.com1320.project;

import java.net.URI;
import java.util.HashMap;

public interface Document extends Comparable<Document>
{
    byte[] getDocument();
    int getDocumentHashCode();
    URI getKey();
    DocumentStore.CompressionFormat getCompressionFormat();
    long getLastUseTime();
    void setLastUseTime(long timeInMilliseconds);
    HashMap<String,Integer> getWordMap();
    void setWordMap(HashMap<String,Integer> wordMap);

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return
     */
    int wordCount(String word);
}
