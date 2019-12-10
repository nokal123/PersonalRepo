package edu.yu.cs.com1320.project.Impl;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.Impl.DocumentStoreImpl.StorageElement;

public class BTreeImpl implements BTree<URI, DocumentImpl> 
{
	 //max children per B-tree node = MAX-1 (must be an even number and greater than 2)
    private static final int MAX = 4;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private File baseDir;
    DocumentIOImpl docIO;
  


    //B-tree node data type
    private static final class Node
    {
        private int entryCount; // number of entries
        private Entry[] entries = new Entry[BTreeImpl.MAX]; // the array of children
        private Node next;
        private Node previous;

        // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }

        private void setNext(Node next)
        {
            this.next = next;
        }
        private Node getNext()
        {
            return this.next;
        }
        private void setPrevious(Node previous)
        {
            this.previous = previous;
        }
        private Node getPrevious()
        {
            return this.previous;
        }

        private Entry[] getEntries()
        {
            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    public static class Entry
    {
        private Comparable key;
        private Object val;
        private Node child;

        public Entry(Comparable key, Object val, Node child)
        {
            this.key = key;
            this.val = val;
            this.child = child;
        }
        public Object getValue()
        {
            return this.val;
        }
        public Comparable getKey()
        {
            return this.key;
        }
    }
    
    /**
     * Initializes an empty B-tree.
     */
    public BTreeImpl(File baseDir)
    {
    	this.baseDir = baseDir;
    	docIO = new DocumentIOImpl(baseDir);
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
    }

	@Override
	public DocumentImpl get(URI uri) { //Change to accomidate when the file has been written to the disk and needs to be brought back into memory 
		
		if (uri == null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, uri, this.height);
        if(entry != null)
        {
        	//the file its on the end of that uri. deserialize . USE  instanceof
        	if(entry.val instanceof File) {
        		DocumentImpl doc = (DocumentImpl) docIO.deserialize(uri);
        		doc.setDeserialized(true);
        		entry.val = doc;
        		return doc;	
        	}
        	
        	DocumentImpl result = (DocumentImpl) entry.val;
        	
        	if(result != null)
        		result.setDeserialized(false);
        	
            return result; 
        }
        return null;
	}
	
	 private Entry get(Node currentNode, URI uri, int height)
	    {
	        Entry[] entries = currentNode.entries;

	        //current node is external (i.e. height == 0)
	        if (height == 0)
	        {
	            for (int j = 0; j < currentNode.entryCount; j++)
	            {
	                if(isEqual(uri, entries[j].key))
	                {
	                    //found desired key. Return its value
	                    return entries[j];
	                }
	            }
	            
	            //didn't find the key
	            return null;
	        }
	      //current node is internal (height > 0)
	        else
	        {
	            for (int j = 0; j < currentNode.entryCount; j++)
	            {
	                //if (we are at the last key in this node OR the key we
	                //are looking for is less than the next key, i.e. the
	                //desired key must be in the subtree below the current entry),
	                //then recurse into the current entry’s child
	                if (j + 1 == currentNode.entryCount || less(uri, entries[j + 1].key))
	                {
	                    return this.get(entries[j].child, uri, height - 1);
	                }
	            }
	            //take the key look up the filepath and see if its written off of memory
	            //didn't find the key
	            return null;
	        }
	    }
	private static boolean isEqual(Comparable k1, Comparable k2) 
    {
        return k1.compareTo(k2) == 0;
    }
	// comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2) 
    {
        return k1.compareTo(k2) < 0;
    }

    /**
     * Inserts the key-value pair into the symbol table, overwriting the old
     * value with the new value if the key is already in the symbol table. If
     * the value is {@code null}, this effectively deletes the key from the
     * symbol table.
     *
     * @param key the key
     * @param val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
	@Override
	public DocumentImpl put(URI uri, DocumentImpl doc) 
	{
        if (uri == null)
        {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        //if the key already exists in the b-tree, simply replace the value
        Entry alreadyThere = this.get(this.root, uri, this.height);
        if(alreadyThere != null)
        {
        	DocumentImpl previous;
        	
        	if(alreadyThere.val instanceof File)
        	{
        		previous = (DocumentImpl) docIO.deserialize(uri);
        		alreadyThere.val = doc;
                return previous;
        	}
        	previous = (DocumentImpl) alreadyThere.val;
        	alreadyThere.val = doc;
            return previous;
        }

        Node newNode = this.put(this.root, uri, doc, this.height);
        this.n++;
        if (newNode == null)
        {
            return null;
        }

        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return null; 
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node put(Node currentNode, URI uri, DocumentImpl doc, int height)
    {
        int j;
        Entry newEntry = new Entry(uri, doc, null);

        //external node
        if (height == 0)
        {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++)
            {
                if (less(uri, currentNode.entries[j].key))
                {
                    break;
                }
            }
        }

        // internal node
        else
        {
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(uri, currentNode.entries[j + 1].key))
                {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[j++].child, uri, doc, height - 1);
                    if (newNode == null)
                    {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--)
        {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX)
        {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else
        {
            //will have to create new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    /**
     * split node in half
     * @param currentNode
     * @return new node
     */
    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

	@Override
	public void moveToDisk(URI uri) throws Exception 
	{
		//all document's uri in btree now refrenece a file
		Entry entry = this.get(this.root, uri, this.height);
		DocumentImpl temp = (DocumentImpl) entry.val;
		File docFile = docIO.serialize(temp);
		entry.val = docFile;
		
	}


}
