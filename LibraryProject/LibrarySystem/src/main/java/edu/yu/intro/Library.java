package edu.yu.intro; 
import java.util.*;
import java.util.Objects;

/**
* This class creates a Library instance which has a holding of 
* @author Noah Kalandar the legend 
* @version 2.0
*
*/
public class Library
{
	String lName; 
	String lAddress;
	String lPhoneNumber;
	
	List<Book> libraryHolding = new ArrayList<Book>();
	Map<String, Patron> patronSet = new HashMap<>();
	Map<String, List> patronsLoanedBooks = new HashMap<>(); //potentailly change to patron? 



	//constructor 
	public Library(String name, String address, String phoneNumber)
	{
		if(name == null || name.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Library name is null or blank. Please enter valid input.");
		if(address == null || address.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Address is null or blank. Please enter valid input.");
		if(phoneNumber == null || phoneNumber.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Your phonenumber is null or blank. Please enter valid input."); 

		lName = name;
		lAddress = address;
		lPhoneNumber = phoneNumber;
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

		Library otherLibrary = (Library) that;
		return lName == otherLibrary.getName();
	} 

	@Override
	public int hashCode() 
	{
		return Objects.hash(lName);
	}

	@Override
	public String toString() 
	{
		return lName + " " + lAddress + " " + lPhoneNumber;  
	}

	public String getName()
	{
		return lName;
	}

	public String getAddress()
	{
		return lAddress;
	}

	public String getPhoneNumber()
	{
		return lPhoneNumber;
	}


	/** This method adds the specified book to the library’s holdings. It is
	not an error if the book has already been added.*/

	public void add(Book b)
	{
		if(b == null)
		{
			throw new IllegalArgumentException("ERROR: Entered a null paramiter for add method");
		}

		libraryHolding.add(b);
	}

	/** This method returns true iff the specified book in the library’s holdings.
	If the title is empty should throw an IllegalArgumentException.*/

	public boolean isTitleInHoldings(String title)
	{
		if(title == null || title.replace(" ", "").equals(""))
			throw new IllegalArgumentException("Error: Library name is null or blank. Please enter valid input.");

		for(int i = 0; i < libraryHolding.size() - 1; i++)
		{
			if(libraryHolding.get(i).getTitle().equals(title))
				return true;
		}

		return false;
	}

	/** This method returns true iff the specified book in the library’s holdings.
	If the isbn is invalid, should throw an IllegalArgumentException.*/

	public boolean isISBNInHoldings(long isbn13)
	{
		if(String.valueOf(isbn13).length() != 13)
			throw new IllegalArgumentException("ERROR: Must enter a 13 digit number. Please re-enter.");

		for(int i = 0; i < libraryHolding.size() - 1; i++)
		{
			if(libraryHolding.get(i).getISBN13() == isbn13)
				return true;
		}
 
		return false;
	}

	/** This method returns null if the specified Book is not in the library’s
	holdings; returns the Book instance if it is. If the isbn is invalid, should
	throw an IllegalArgumentException.*/
	
	public Book getBook(long isbn13)
	{
		if(String.valueOf(isbn13).length() != 13)
			throw new IllegalArgumentException("ERROR: Must enter a 13 digit number. Please re-enter.");

		for(int i = 0; i < libraryHolding.size() - 1; i++)
		{
			if(libraryHolding.get(i).getISBN13() == isbn13)
				return libraryHolding.get(i);
		}

		return null;

	}

	/** This method returns the number of Books in the library’s holdings. */
	public int nBooks()
	{
		return libraryHolding.size();
	}

	public void clearLibrary()
	{
		libraryHolding.clear();
	}


	/** This method enables the specified patron to borrow the specified book. 
	This implications of this action are that onLoan() (see below) will list
	that book as being “on loan” to this user. Throws an IllegalArgumentException 
	if Patron is not a member of this Library. Throws an 
	IllegalArgumentException if Book is not in Library’s holdings. */

	public void borrow(Patron patron , Book book)
	{
		if(patron == null || book == null)
			throw new IllegalArgumentException("ERROR: One of the paramiters where null.");

		if(get(patron.getId()) == null)
			throw new IllegalArgumentException("ERROR: Patron not in the Library system!");

		if(getBook(book.getISBN13()) == null)
			throw new IllegalArgumentException("ERROR: Book is not in the Library system!");
		
		String uuid = patron.getId();

		patronsLoanedBooks.get(uuid).add(book);

	}

	/** This method returns a (possibly empty) Collection of Book instances
	that are currently on loan to the specified patron. Throws an IllegalArgumentException
	if Patron is not a member of this Library. */
    public Collection <Book> onLoan(Patron patron)
	{
		if(patron == null)
			throw new IllegalArgumentException("ERROR: Entered a null paramiter for add method");

		if(get(patron.getId()) == null)
			throw new IllegalArgumentException("ERROR: Patron not in the Library system!");
			

		String uuid = patron.getId();

		return patronsLoanedBooks.get(uuid);

	}

	/** This method returns a (possibly empty) Collection of Books that
	match this search criteria specified by the BookFilter (see section below
	on BookFilter function). */
	public Collection <Book> search(BookFilter filter)
	{
		List<Book> searchResults = new ArrayList<Book>();

		for(int i = 0; i < libraryHolding.size(); i++)
		{
			if(filter.filter(libraryHolding.get(i)) == true)
			{
				searchResults.add(libraryHolding.get(i));		
			}
		}

		return searchResults;
	}

	/** This method adds the specified patron to the container. */
	public void add(Patron patron)
	{
		if(patron == null)
			{
				throw new IllegalArgumentException("ERROR: Entered a null paramiter for add method");
			}

		patronSet.put(patron.getId(), patron);

		List<Book> patronsBooks = new ArrayList<Book>();
		patronsLoanedBooks.put(patron.getId(), patronsBooks);
	}

	/** This method removes all patrons from the container. */
	public void clearPatrons()
	{
		patronSet.clear();
	}

 	/** This method returns the specified Patron (or null, if no such Patron exists). */
 	public Patron get(String uuid)
 	{
 		return patronSet.get(uuid);
 	}

	/** This method returns the number of stored Patrons. */
 	public int nPatrons()
 	{
 		return patronSet.size();
 	}

	//helper method for bylastnameprefix method
 	private boolean prefix(String word, String prefix)
 	{
 		for(int i = 0; i < prefix.length(); i++)
		{
	   		if(word.charAt(i) != prefix.charAt(i)) 
   			{
   				return false;
   			}
	   	}

	   	return true;	
 	}


 	/** This method returns a (possibly empty) java.util.Set of “all Patrons
	whose last name starts with the specified prefix”. Specifying an empty
	prefix is equivalent to matching all last names: i.e., return all patrons
	in the system. Specifying a null prefix should trigger an IllegalArgumentException.
	Note that this method returns a parameterized
	Set: one that contains Patron instances. */
	public Set<Patron> byLastNamePrefix(final String prefix)
	{
		if(prefix == null)
			throw new IllegalArgumentException("\nERROR: Invalid prefix entered. Prefix was null.");

		if(prefix.equals(""))
		{
			Set<Patron> valueSet = new HashSet<Patron>(patronSet.values());
			return valueSet; 
		}

		Set <Patron> byLastNamePrefix = new HashSet<>();
		String lastName = null;
		

		for (Map.Entry<String, Patron> entry : patronSet.entrySet())
			{
				lastName = entry.getValue().getLastName();

			   	if(prefix(lastName, prefix))
			   	{
			   		byLastNamePrefix.add(entry.getValue());
			   	}
			}

		return byLastNamePrefix;

		//Set<String> setofValues = new HashSet<>(patronSet.values());
	}

}