package edu.yu.intro.test;
import org.junit.*;
import static org.junit.Assert.*;
import edu.yu.intro.Book;
import edu.yu.intro.Library;
import edu.yu.intro.Patron;
import edu.yu.intro.BookFilter;
import java.util.*;

public class library3TestCode
{
	//test code for Parton 
	@Test (expected = IllegalArgumentException.class)
	public void blankStringFName()
	{
		Patron temp = new Patron(" ", "looser", "171 union");
	}

	@Test (expected = IllegalArgumentException.class)
	public void blankStringlName()
	{
		
		Patron temp = new Patron("bob", " ", "171 union");
	}
	@Test (expected = IllegalArgumentException.class)
	public void blankStringaddress()
	{
		
		Patron temp = new Patron("bob", "poop", " ");
	}
	@Test (expected = IllegalArgumentException.class)
	public void nullStringFName()
	{
		Patron temp = new Patron(null, " poop", "171 union");
	}
	@Test (expected = IllegalArgumentException.class)
	public void nullStringlName()
	{
		Patron temp = new Patron("bob", null, "171 union");
	}
	@Test (expected = IllegalArgumentException.class)
	public void nullStringaddress()
	{
		Patron temp = new Patron("bob", "oppo", null);
	}

	//happy
	@Test
	public void getAddress()
	{
		Patron temp = new Patron("bob", "oppo", "171 union ave");
		assertEquals("test happy for getAddress", "171 union ave", temp.getAddress());
	}
	@Test
	public void getfirstName()
	{
		Patron temp = new Patron("bob", "oppo", "171 union ave");
		assertEquals("test happy for getFirstName", "bob", temp.getFirstName());
	}
	@Test
	public void getLastName()
	{
		Patron temp = new Patron("bob", "oppo", "171 union ave");
		assertEquals("test happy for getLastName", "oppo", temp.getLastName());
	}
	

// _________________________________________________________________________________________

	Library doof = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
	
	//test in Patrons
	@Test (expected = IllegalArgumentException.class)
	public void addingNullPatron()
	{
		doof.add((Patron)null);
	}
	@Test (expected = IllegalArgumentException.class)
	public void nullPrefix()
	{
		doof.byLastNamePrefix(null);
	}

	//Happy path
	@Test
	public void getPatronWUUID()
	{
		Patron temp = new Patron("bob", "oppo", "171 union ave");
		doof.add(temp);
		assertEquals("test for getting patron w uuid", temp , doof.get(temp.getId()));
	}

	@Test
	public void checkPrefixReturnSet()
	{

		doof.clearPatrons();
		doof.clearLibrary();
		Patron input = new Patron("Jonh", "Doe", "171 union ave");
		//Patron input1 = new Patron("bob", "oppo", "171 union ave");
		//Patron input2 = new Patron("bue", "Peet", "171 union ave");
		//Patron input3 = new Patron("poop", "Peat", "171 union ave");
		doof.add(input);
		//Patrons.Singleton.add(input1);
		//Patrons.Singleton.add(input2);
		//Patrons.Singleton.add(input3);	

		Set<Patron> valueSet = new HashSet<Patron>();
		valueSet.add(input);
		//valueSet.add(input1);
		//valueSet.add(input2);
		//valueSet.add(input3);

		assertEquals("test for black prefix", valueSet , doof.byLastNamePrefix(""));
	}

	@Test
	public void checkPrefixWTwoRight()
	{
		doof.clearPatrons();
		Patron input = new Patron("Jonh", "Doe", "171 union ave");
		Patron input1 = new Patron("bob", "oppo", "171 union ave");
		Patron input2 = new Patron("bue", "Peet", "171 union ave");
		Patron input3 = new Patron("poop", "Peat", "171 union ave");
		doof.add(input);
		doof.add(input1);
		doof.add(input2);
		doof.add(input3);	
		
		Set<Patron> valueSet = new HashSet<Patron>();
		//valueSet.add(input2);
		//valueSet.add(input3);


		assertEquals("test for set with 1 inside", valueSet , doof.byLastNamePrefix(" "));
	}



// _________________________________________________________________________________________


	//test for new code Borrow, onLoan and Search
	@Test (expected = IllegalArgumentException.class)
	public void testForNoPatroninBorrow()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Book testBook = new Book("Joan", "bob", 1234567891023L, "paperback");
		Patron input = new Patron("Jonh", "Doe", "171 union ave");
		Patron input1 = new Patron("bob", "oppo", "171 union ave");
		Patron input2 = new Patron("bue", "Peet", "171 union ave");

		//test.add(testBook);
		test.add(input);
		test.add(input1);

		test.borrow(input2, testBook);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testForNoBookinBorrow()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Book testBook = new Book("Joan", "bob", 1234567891023L, "paperback");
		Book testBook1 = new Book("Bob", "Dillan", 1234567891033L, "paperback");
		Patron input = new Patron("Jonh", "Doe", "171 union ave");
		

		//test.add(testBook);
		test.add(input);
		test.add(testBook);

		test.borrow(input, testBook1);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testFornoPatronWhenGettingList()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Patron input = new Patron("Jonh", "Doe", "171 union ave");
		Patron input1 = new Patron("Jonh", "Doe", "171 union ave");
		test.add(input);

		test.onLoan(input1);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testFornullPatronWhenGettingList()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		test.onLoan(null);
	}

	@Test
	public void testHappyPathforBorrowandOnLoan()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Book testBook = new Book("Joan", "bob", 1234567891024L, "paperback");
		Book testBook1 = new Book("Bob", "Dillan", 1234567891032L, "paperback");
		Patron input = new Patron("Jonh", "Doe", "171 union ave");
		

		//test.add(testBook);
		test.add(input);
		test.add(testBook);
		test.add(testBook1);

		test.borrow(input, testBook);

		List<Book> testBooks = new ArrayList<Book>();
		testBooks.add(testBook);

		assertEquals("test for patrons loaned books with 1 inside", testBooks , test.onLoan(input));
	}



// _________________________________________________________________________________________

	//testing search and filter

	@Test (expected = IllegalArgumentException.class)
	public void testErrorInputOnBuilder()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Book testBook = new Book("Joan", "bob", 1234567851022L, "paperback");
		Book testBook3 = new Book("Dillan", "bob", 1234537891024L, "hardcover");
		Book testBook2 = new Book("Joan", "bob", 1232567894023L, "paperback");
		Book testBook1 = new Book("Dillan", "Dill", 1254562891039L, "ebook");
		

		//test.add(testBook);
		test.add(testBook2);
		test.add(testBook1);
		test.add(testBook);
		test.add(testBook3);

		BookFilter testFilter = new BookFilter.Builder().setTitle(" ").build();

		List<Book> testBooks = new ArrayList<Book>();
		testBooks.add(testBook1);
		testBooks.add(testBook3);

		assertEquals("test for happy path filter", testBooks , test.search(testFilter));

	}

	@Test
	public void testhappyPath()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Book testBook = new Book("Joan", "bob", 1234567851024L, "paperback");
		Book testBook3 = new Book("Dillan", "bob", 1234537891022L, "hardcover");
		Book testBook2 = new Book("Joan", "bob", 1232567894028L, "paperback");
		Book testBook1 = new Book("Dillan", "Dill", 1254562891031L, "ebook");
		

		//test.add(testBook);
		test.add(testBook2);
		test.add(testBook1);
		test.add(testBook);
		test.add(testBook3);

		BookFilter testFilter = new BookFilter.Builder().setTitle("Dillan").build();

		List<Book> testBooks = new ArrayList<Book>();
		testBooks.add(testBook1);
		testBooks.add(testBook3);

		assertEquals("test for happy path filter", testBooks , test.search(testFilter));

	}
	@Test
	public void testhappyPathwithISBN()
	{
		Library test = new Library("Bala Cynwyd" , "171 union ave", "2157765805");
		Book testBook = new Book("Joan", "bob", 1234567851022L, "paperback");
		Book testBook3 = new Book("Dillan", "bob", 1234537891024L, "hardcover");
		Book testBook2 = new Book("Joan", "bob", 1232567894023L, "paperback");
		Book testBook1 = new Book("Dillan", "Dill", 1234537891024L, "ebook");
		

		//test.add(testBook);
		test.add(testBook2);
		test.add(testBook1);
		test.add(testBook);
		test.add(testBook3);

		BookFilter testFilter = new BookFilter.Builder().setISBN13(1234537891024L).build();

		List<Book> testBooks = new ArrayList<Book>();
		testBooks.add(testBook1);
		testBooks.add(testBook3);


		assertEquals("test for happy path filter", testBooks , test.search(testFilter));
	}



// _________________________________________________________________________________________

	//test for book class
	@Test (expected = IllegalArgumentException.class)
	public void blankStringTitle()
	{
		//Library newLib = new Library("bobb", " 171 union ave", "12423512412");
		Book testBook = new Book(" ", "bob", 123456789102L, "paperback");
	}
	@Test (expected = RuntimeException.class)
	public void wrongISBN()
	{
		Book harryPotter = new Book("bob", "marly", 12345678910L, "paperback");
	}



}