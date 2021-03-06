package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> 
{
	private HashElement<Key, Value>[] buckets = new HashElement[200];
	private int currentElements = 0;
	
	//this must be able to deal with generics that are in the hashmap, not just objects
	public class HashElement<Key, Value>
    {
        private Key key;
        private Value value;
        HashElement<Key, Value> next; 

        public HashElement(Key key, Value value)
        {
            this.key = key;
            this.value = value;
            this.next = null;
        }

		private Value getValue() 
		{
			return value;
		}
		private void setValue(Value value)
		{
			this.value = value;
		}
		private Key getKey()
		{
			return key;
		}
        
    }
	
	private int HashIndex(Key key) 
    {
		return Math.abs(key.hashCode() % buckets.length);
		//this makes no sense 
    }
	
	/**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
	public Value get(Key key) 
	{
		if(key == null) {
			throw new IllegalArgumentException("ERROR! Invalid Key given. Key was null");
		}
		
		int index = HashIndex(key);
		HashElement<Key, Value> pointer = buckets[index];
		
		while (pointer != null) 
        { 
            if (pointer.getKey().equals(key)) 
            { 
                return pointer.getValue(); 
            } 
            pointer = pointer.next; 
        }
		
		return null;
	}
	
	 /**
     * @param k the key at which to store the value
     * @param v the value to store
     * @return if the key was already present in the HashTable, return the previous value stored for the key. 
     * If the key was not already present, return null.
     */
	public Value put(Key key, Value value)
	{
		if(key == null) {
			throw new IllegalArgumentException("ERROR! Invalid Key given. Key was null");
		}
		if(value == null) {
			return remove(key);
		}
		
		int index = HashIndex(key);
		HashElement<Key, Value> pointer = buckets[index];
		
		while (pointer != null) 
        { 
            if (pointer.getKey().equals(key)) 
            { 
            	Value temp = pointer.getValue();
            	pointer.setValue(value); 
                return  temp;
            } 
            pointer = pointer.next; 
        } 
		
		/* this is where I need to check to see if array doubling is nessasary */
		if(currentElements == 4 * buckets.length)
			doubleArray();
		
		HashElement<Key, Value> object = new HashElement<Key, Value>(key,value);
		object.next = buckets[index];
		buckets[index] = object;
		currentElements++;
		
		return null;
	}
	
	/**
	 * This is a helper method for the put(key, value) method that checks to see if the array needs to be doubled
	 * before being added too
	 */
	private void doubleArray()
	{
		HashElement<Key, Value>[] newBuckets = new HashElement[buckets.length * 2];
		for(HashElement <Key, Value> temp : buckets)
		{
			int newIndex = Math.abs(temp.getKey().hashCode() % newBuckets.length);
			//int newIndex = temp.getKey().hashCode() % Math.abs(newBuckets.length);
			newBuckets[newIndex] = temp;
		}
		buckets = newBuckets;
		
	}
	/** 
	 * Helper method to the put(key, value) method and for the deleteDocument(key) method of DocumentStoreImpl
	 * @param key
	 * @return null if element was not in the hash map with this key
	 * @return value for element that was deleted
	 */
	private Value remove(Key key)
	{
		int index = HashIndex(key);
		HashElement<Key, Value> pointer = buckets[index];
		HashElement<Key, Value> previous = null;
		
		while (pointer != null) 
        { 
            if (pointer.getKey().equals(key)) 
            { 
            	if(previous == null) {
            		buckets[index] = pointer.next;
            	}
            	else {
            		previous.next = pointer.next;
            	}
            	
            	currentElements--;
            	return pointer.getValue();
            } 
            previous = pointer;
            pointer = pointer.next; 
        }
		return null;
	}

	public int getSize() {
		return currentElements;
	}
	
}
