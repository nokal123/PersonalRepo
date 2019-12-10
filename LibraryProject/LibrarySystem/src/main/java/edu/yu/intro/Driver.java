package edu.yu.intro; 
import java.util.Objects;
import java.util.*;

public class Driver
{
	/** This method implements the following scenario in the specified order:
	1. Reset application state so that no Patrons exist, no Books exist, etc.
	2. Create three different books and add them to the Library.
	3. Create two different patrons and add them to Patrons. */
	
	public static Library run()
	{
		Library testLibrary = new Library("Bala Cynwyd","171 union ave", "2157765805");
		testLibrary.clearLibrary();
		testLibrary.clearPatrons();

		testLibrary.add(new Book("Harry Potter", "Noah kalandar", 1234567891022L, "paperback"));
		testLibrary.add(new Book("Lord of the Rings", "Mary Poppins", 3472859360127L, "hardcover"));
		testLibrary.add(new Book("Dr. Sues", "Professor Leff", 7837294628194L, "ebook"));

		testLibrary.add(new Patron("Noah", "Kalandar", "171 Union Ave"));
		testLibrary.add(new Patron("Professor", "Leff", "256 Amsterdam Ave"));

		return testLibrary;
	}	

}