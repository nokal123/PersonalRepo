package edu.yu.cs.com1320.project;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> 
{
	private HashElement<Key, Value>[] buckets = new HashElement[997];
	
	//this must be able to deal with generics that are in the hashmap, not just objects
	private class HashElement<Key, Value>
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
	
	public int HashIndex(Key key) 
    {
		return key.hashCode() % Math.abs(buckets.length);
       
		/* char[] keyChar = new char[((String) key).length()];
        int length = ((String) key).length();
        int hash = 0;
        for(int i = 0; i < length; i++)
        {
            hash += keyChar[i] * 31^(length - (i + 1));
        }
        return hash % buckets.length; */
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
                return (Value) pointer.getValue(); 
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
		
		HashElement<Key, Value> object = new HashElement<Key, Value>(key,value);
		object.next = buckets[index];
		buckets[index] = object;
		return null;
	}
	
	
}
