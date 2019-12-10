package edu.yu.cs.com1320.project.Impl;

import java.util.Arrays;
import java.util.HashMap;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl extends MinHeap<DocumentImpl>
{
	
	public MinHeapImpl()
	{
		super.count = 0;
		super.elementsToArrayIndex = new HashMap<DocumentImpl, Integer>();
		super.elements = new DocumentImpl[10];
	}
	
	
	@Override
	protected void doubleArraySize() 
	{
		 elements = Arrays.copyOf(elements, elements.length * 2);
	}

	@Override
	public void reHeapify(DocumentImpl element) {
		
		int index = elementsToArrayIndex.get(element);
		upHeap(index);
		downHeap(index);

	}

	

	@Override
	protected int getArrayIndex(DocumentImpl element) {
		return elementsToArrayIndex.get(element);
	}
	
	@Override
	 protected  void upHeap(int k)
    {
        while (k > 1 && this.isGreater(k / 2, k))
        {
            this.swap(k, k / 2);
            k = k / 2;
        }
        elementsToArrayIndex.put(elements[k], k);
    }


	@Override
    protected  void downHeap(int k)
    {
        while (2 * k <= this.count)
        {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1))
            {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j))
            {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
        elementsToArrayIndex.put(elements[k], k);
    }
}
