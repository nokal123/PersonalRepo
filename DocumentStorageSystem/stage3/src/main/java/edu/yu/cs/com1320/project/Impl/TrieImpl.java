package edu.yu.cs.com1320.project.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import edu.yu.cs.com1320.project.Trie;

public class TrieImpl<Value> implements Trie<Value>
{
    private static final int alphabetSize = 256; // extended ASCII
    private Node<Value> root; // root of trie
    private Comparator<Value> sortComparator;


    public TrieImpl(Comparator<Value> comp)
    {
    	sortComparator = comp;
    }
   
    public static class Node<Value>
    {
        protected ArrayList<Value> val = new ArrayList<Value>();
        protected Node<Value>[] links = new Node[TrieImpl.alphabetSize];
    }

    /**
     * Returns the value associated with the given key.
     *
     * @param key the key
     * @return the value associated with the given key if the key is in the trie and {@code null} if not
     */
    @Override
    public List<Value> getAllSorted(String key)
    {
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null)
        {
            return null;
        }
        //sorts the arraylist according to the comparator object 
        //that was passed to TrieImpl when first made 
        //System.out.println("is comparator null: " + (sortComparator == null));
        x.val.sort(sortComparator); 
        return x.val;
    }

    /**
     * A char in java has an int value.
     * see http://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#getNumericValue-char-
     * see http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2
     */
    private Node<Value> get(Node<Value> x, String key, int d)
    {
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    /**
     * Main put method. Does most operations in private put method
     * @param String key 
     * @param Value Val
     */
    @Override
    public void put(String key, Value val)
    {
        //deleteAll the value from this key
        if (val == null)
        {
            this.deleteAll(key);
        }
        else
        {
            this.root = put(this.root, key, val, 0);
        }
    }
    /**
     * @param the root x
     * @param the string key
     * @param the value val
     * @param d is position in the trie corresponding to place in the string
     * @return
     */
    private Node<Value> put(Node<Value> x, String key, Value val, int d)
    {
        //create a new node
        if (x == null)
        {
            x = new Node<Value>();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
        	if(x.val.contains(val) == false) {
        		x.val.add(val);
        	}
        	
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    /**
     * Public DeleteAll Method. Does all operations in Private DeleteAll Method
     * @param String Key
     */
    @Override
    public void deleteAll(String key)
    {
        this.root = deleteAll(this.root, key, 0);
    }

    private Node<Value> deleteAll(Node<Value> x, String key, int d)
    {
        if (x == null)
        {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length())
        {
            x.val.clear();
        }
        //continue down the trie to the target node
        else
        {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (x.val != null)
        {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty	
        for (int c = 0; c <TrieImpl.alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

	 /**
     * delete ONLY the given value from the given key. Leave all other values.
     * @param key
     * @param val
     */
    @Override
    public void delete(String key, Value val)
	{
    	int d = 0;
    	Node<Value> x = this.get(this.root, key, 0);
    	
    	if(x != null) {
        	x.val.remove(val);
        }
        
	}
    
}



