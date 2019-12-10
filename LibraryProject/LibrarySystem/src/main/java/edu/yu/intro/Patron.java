package edu.yu.intro; 
import java.util.Objects;
import java.util.*;

public class Patron
{
	String firstName; 
	String lastName;
	String pAddress;
	String uuid;

	public Patron(String fName, String lName, String address)
	{
		if(fName == null || fName.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Patron's first name is null or blank. Please enter valid input.");
		if(address == null || address.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Patron's last name is null or blank. Please enter valid input.");
		if(lName == null || lName.replace(" ", "").equals(""))
			throw new IllegalArgumentException("\nError: Patron's address is null or blank. Please enter valid input.");

		uuid = UUID.randomUUID().toString();
		firstName = fName;
		lastName = lName;
		pAddress = address;
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

		Patron otherPatron = (Patron) that;
		return uuid == otherPatron.getId();
	} 

	@Override
	public int hashCode() 
	{
		return Objects.hash(uuid);
	}

	@Override
	public String toString() 
	{
		return firstName + " " + lastName + " " + pAddress;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public String getAddress()
	{
		return pAddress;
	}
	
	public String getId()
	{
		return uuid;
	}
}