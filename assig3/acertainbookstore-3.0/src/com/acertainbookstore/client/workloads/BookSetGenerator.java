package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {
	static Random rGen = new Random();
	
	public BookSetGenerator() {
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */
	public static List<Book> sampleFromSetOfISBNs(List<Book> isbns, int num) {
		List<Book> newList = new ArrayList<Book>();
		
		for (int i = 0; i < num; i++) {
			int randomIndex = randomInt(isbns.size());
			newList.add(isbns.get(randomIndex));
			// Remove to avoid duplicates
			isbns.remove(randomIndex);
		}
			
		return newList;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public static Set<StockBook> nextSetOfStockBooks(int num) {
		Set<StockBook> stockBooks = new HashSet<>();
		
		// Books are uniquely defined by their ISBN, so we don't care about the
		// other properties being equal.
		for (int i = 0; i < num; i++) {
			stockBooks.add(new ImmutableStockBook(random()+1, "Some Title", "Some Author", (float) 10.0,
												  20, (long) 0, (long) 0, (long) 0, randomBool()));
		}
		return stockBooks;
	}

	
	/**
	 * Generates a random int.
	 * @return int
	 */
	public static int random() {
        return rGen.nextInt(10000000);
	}
	
	/**
	 * Generates a random int in the interval [0,r].
	 * @param r
	 * @return int
	 */
	public static int randomInt (int r) {
        return rGen.nextInt(r);
	}
	
	/**
	 * Generates a random book.
	 * @return int
	 */
	public static boolean randomBool() {
        return rGen.nextBoolean();
	}
}
