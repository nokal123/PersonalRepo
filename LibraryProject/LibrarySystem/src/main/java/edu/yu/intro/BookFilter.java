package edu.yu.intro; 

public class BookFilter
{
	private String author; 
	private String title; 
	private long isbn13;
	private String bookType;
	/** and use the Builder class (below) to create a fully configured BookFilter
	instance. */
	private BookFilter(Builder builder) 
	{
		author = builder.author; 
		title = builder.title;
		isbn13 = builder.isbn13;
		bookType = builder.bookType;
	}

	/** This method returns true if the book’s properties match all of the
	properties that have been configured on the BookFilter instance. */
    public boolean filter(Book book)
    {
    	if( author != null && !(book.getAuthor().equals(author)))
    	{
    		return false;
    	}

    	if(title != null && !(book.getTitle().equals(title)))
    	{
    		return false;
    	}

    	if(isbn13 != 0 && isbn13 != book.getISBN13()) //check to see if same issue with equals on null
    	{
    		return false; 	
    	}

    	if(bookType != null && !(book.getBookType().equals(bookType)))
    	{
    		return false; 
    	}


    	return true;
    }

    	public static class Builder
    	{
    		private String author; 
    		private String title; 
    		private long isbn13;
    		private String bookType;
    		private enum Btype { ebook, paperback, hardcover }
    		
    		public Builder() {}

    		public Builder setAuthor(String bAuthor)
    		{
    			if(bAuthor == null || bAuthor.replace(" ", "").equals(""))
						throw new IllegalArgumentException("\nError: Address is null or blank. Please enter valid input.");
    			
    			this.author = bAuthor;
    			return this;
    		}

			public Builder setTitle(String bTitle)
			{
				if(bTitle == null || bTitle.replace(" ", "").equals(""))
						throw new IllegalArgumentException("\nError: Library name is null or blank. Please enter valid input.");

				this.title = bTitle ;
				return this;
			}

			public Builder setISBN13(long bISBN)
			{
				if(bISBN == 0 || Long.toString(bISBN).length() != 13)
						throw new RuntimeException("ERROR: Invalid ISBN number. Must be 13 characters long");

				this.isbn13 = bISBN;
				return this;
			}

			public Builder setBookType(String bBookType)
			{
				if(bBookType == null || bBookType.replace(" ", "").equals(""))
						throw new IllegalArgumentException("\nError: Book Type is null or blank. Please enter valid input.");

				if(!(bBookType.equals(Btype.ebook.toString())) && !(bBookType.equals(Btype.paperback.toString())) && !(bBookType.equals(Btype.hardcover.toString())))
						throw new IllegalArgumentException("\nError: Invalid book type entry. Must enter eBook, paperback, or hardcover");

				this.bookType = bBookType;
				return this;

			}
			public String getAuthor()
			{
				return this.author;
			}
			public String getTitle()
			{
				return this.title;
			}
			public long getISBN()
			{
				return this.isbn13;
			}
			public String getBookType()
			{
				return this.bookType;
			}

			public BookFilter build()
			{
				return new BookFilter(this); //incorrect 
			}
    	}
	
}