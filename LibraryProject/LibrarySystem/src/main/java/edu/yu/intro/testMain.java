package edu.yu.intro;
import edu.yu.intro.Book;
import edu.yu.intro.Library;
import edu.yu.intro.Patron;
import edu.yu.intro.BookFilter;
import java.util.*;

public class testMain
{
	public static void main(String[] args) 
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

		BookFilter testFilter = new BookFilter.Builder().setISBN13(1234537891024L).build();

		List<Book> testBooks = new ArrayList<Book>();
		testBooks.add(testBook3);

		test.search(testFilter);
	}
}