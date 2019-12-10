package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T>
{
	private int count = 0;
	private LinearNode<T> top = null;
	
	private class LinearNode<T>
	{
		private T element;
		private LinearNode<T> next;
		
		public LinearNode(T input) 
		{
			element = input;
			next = null;
			
		}
		private void setNext(LinearNode<T> next)
		{
			this.next = next;
		}
		private T getElement()
		{
			return element;
		}
	}
		
	/**
     * @param element object to add to the Stack
     */
	@Override
	public void push(T element) {
		
		LinearNode<T> newElement = new LinearNode<T>(element);
		newElement.setNext(top);
		top = newElement;
		count++;
	}
	
	 /**
    * @return how many elements are currently in the stack
    */
	@Override
	public int size() {
		return count;
	}
	
	 /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
	@Override
	public T pop() 
	{
		if(size() == 0)
			return null;
	
		T element = top.getElement();
		top = top.next;
		count--;
		return element;
	}
	
	 /**
    *
    * @return the element at the top of the stack without removing it
    */
	@Override
	public T peek() {
		return top.getElement();
	}
	

}
