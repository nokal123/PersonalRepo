package edu.yu.intro; 
import java.util.Objects;
import java.util.*;

public class Book 
{	
	public enum Btype { 
		ebook, paperback, hardcover }  //may change to reg cases
	
	String title; 
	String author;
	long isbn;
	String bookType;

	//constructor method 
	public Book(String bTitle, String bAuthor, long bISBN, String bBookType)
	{
		if(bTitle == null || bTitle.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Library name is null or blank. Please enter valid input.");
		if(bAuthor == null || bAuthor.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Address is null or blank. Please enter valid input.");
		if(bISBN == 0 || Long.toString(bISBN).length() != 13)
			throw new RuntimeException("ERROR: Invalid ISBN number. Must be 13 characters long");
		if(bBookType == null || bBookType.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Book Type is null or blank. Please enter valid input.");
		if(!(bBookType.equals(Btype.ebook.toString())) && !(bBookType.equals(Btype.paperback.toString())) && !(bBookType.equals(Btype.hardcover.toString())))
			throw new IllegalArgumentException("\nError: Invalid book type entry. Must enter eBook, paperback, or hardcover");

		//if(!(bBookType.equals("EBOOK")) && !(bBookType.equals("PAPERBACK")) && !(bBookType.equals("HARDCOVER")))

		title = bTitle;
		author = bAuthor;
		isbn = bISBN; 
		bookType = bBookType; 
	}

	@Override
	public boolean equals(Object that) 
	{
		if(this == that) 
		{
			return true;
		}

		if(that == null) 
		{
			return false;
		}
	
		if(getClass() != that.getClass()) 
		{
			return false;
		}

		Book otherBook = (Book) that;
		return isbn == otherBook.getISBN13();
	} 

	@Override
	public int hashCode() 
	{
		return Objects.hash(isbn);
	}

	@Override
	public String toString() 
	{
		return title + " " + author + " " + bookType + " " + isbn;  
	}

	public String getAuthor()
	{
		return author;
	}

	public String getTitle()
	{
		return title;
	}

	public long getISBN13()
	{
		return isbn;
	}

	public String getBookType()
	{
		return bookType;
	}
}