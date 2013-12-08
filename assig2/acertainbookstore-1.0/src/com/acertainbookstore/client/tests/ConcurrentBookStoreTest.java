package com.acertainbookstore.client.tests;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.server.BookStoreHTTPMessageHandler;
import com.acertainbookstore.server.BookStoreHTTPServerUtility;
import com.acertainbookstore.utils.BookStoreException;

import static org.junit.Assert.*;

public class ConcurrentBookStoreTest {
	private static boolean localTest = true;
	protected static StockManager storeManager;
	protected static BookStore client;
	private static BookStoreHTTPMessageHandler handler;
	private static Thread ServerThread = null;
	volatile static Exception exception = null;
	volatile static Error error = null;
	
	public static void startTestServer () {
		handler = new BookStoreHTTPMessageHandler();
		ServerThread = BookStoreHTTPServerUtility.startServerThread(8081, handler);
	}
	
	@SuppressWarnings("deprecation")
	public static void stopTestServer () {
		try {
		//	handler.stop();
		} catch (Exception e) {
			// Do nothing
			e.printStackTrace();
		}
		
		ServerThread.stop();
	}
		
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			if (localTest) {
				storeManager = ConcurrentCertainBookStore.getInstance();
				client = ConcurrentCertainBookStore.getInstance();
			} else {
				startTestServer();
				storeManager = new StockManagerHTTPProxy(
						"http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * This test is "Test 1" from the assignment text.
	 */
	@Test
	public void testSimpleAddAndBuy () {
		Set<StockBook> bookSet = new HashSet<StockBook>();
		Integer testISBN1 = 12;
		Integer testISBN2 = 13;
		Integer testISBN3 = 14;
		
		// Books to test on
		bookSet.add(new ImmutableStockBook(testISBN1,
				"Hansel and Gretel and the Mysterious Garbage Collector",
				"Something", (float) 99, 5, 0, 0, 0, false));
		bookSet.add(new ImmutableStockBook(testISBN2,
				"The SDK With the Dragon Tattoo",
				"Something here", (float) 98, 5, 0, 0, 0, false));	
		bookSet.add(new ImmutableStockBook(testISBN3,
				"Little Red JUnit Hood",
				"What is this", (float) 97, 5, 0, 0, 0, false));
		
		// First we need to add the books to the database,
		// and initial stock
		Set<Integer> isbns = new HashSet<Integer>();
		isbns.add(testISBN1);
		isbns.add(testISBN2);
		isbns.add(testISBN3);
		BookCopy bookCopy1 = new BookCopy(testISBN1, 2);
		BookCopy bookCopy2 = new BookCopy(testISBN2, 2);
		BookCopy bookCopy3 = new BookCopy(testISBN3, 2);
		Set<BookCopy> bookCopySet = new HashSet<BookCopy>();
		bookCopySet.add(bookCopy1);
		bookCopySet.add(bookCopy2);
		bookCopySet.add(bookCopy3);
		try {
			storeManager.addBooks(bookSet);
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		}
		// Create clients
		Thread client1 = new Thread (new ConcurrentAddCopiesTest(bookCopySet));
		Thread client2 = new Thread (new ConcurrentBuyBooksTest(bookCopySet));

		// Run them
		client1.start();
		client2.start();
	
		// Wait
		try {
			client1.join();
			client2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		List<StockBook> after = null;
		try {
			after = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		// Recount
		for (StockBook book : after) {
			// Initial stock was 5
			if (isbns.contains(book.getISBN())) {
				assertTrue(5 == book.getNumCopies()); 
			}
		}
	}
	/**
	 * This test is "Test 2" from the assignment text.
	 */
	@Test
	public void testMultipleAddAndBuy () {
		int tries = 1;
		
		Set<StockBook> bookSet = new HashSet<StockBook>();

		Integer testISBN1 = 15;
		Integer testISBN2 = 16;
		Integer testISBN3 = 17;
		
		// Books to test on
		bookSet.add(new ImmutableStockBook(testISBN1,
				"Hansel and Gretel and the Mysterious Garbage Collector",
				"Something", (float) 99, 5, 0, 0, 0, false));
		bookSet.add(new ImmutableStockBook(testISBN2,
				"The SDK With the Dragon Tattoo",
				"Something here", (float) 98, 5, 0, 0, 0, false));	
		bookSet.add(new ImmutableStockBook(testISBN3,
				"Little Red JUnit Hood",
				"What is this", (float) 97, 5, 0, 0, 0, false));
		// First we need to add the books to the database,
		// and initial stock
		Set<Integer> isbns = new HashSet<Integer>();
		isbns.add(testISBN1);
		isbns.add(testISBN2);
		isbns.add(testISBN3);
		BookCopy bookCopy1 = new BookCopy(testISBN1, 2);
		BookCopy bookCopy2 = new BookCopy(testISBN2, 2);
		BookCopy bookCopy3 = new BookCopy(testISBN3, 2);
		Set<BookCopy> bookCopySet = new HashSet<BookCopy>();
		bookCopySet.add(bookCopy1);
		bookCopySet.add(bookCopy2);
		bookCopySet.add(bookCopy3);
		
		List<StockBook> before = null;
		List<StockBook> after = null;
		try {
			before = storeManager.getBooks();
			after = storeManager.getBooks();
			storeManager.addBooks(bookSet);
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		}
		assertTrue(after.addAll(bookSet));
		
		// Create clients
		Thread getter = new Thread (new ConcurrentGetBooksTest(before, after, isbns, tries));
		Thread setter = new Thread (new ConcurrentBuyAndAddTest(bookCopySet, tries));
		
		// Run them
		getter.start();
		setter.start();
	
		// Wait
		try {
			getter.join();
			setter.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Our own concurrency test for ratings.
	 */
	@Test
	public void testConcurrentRating () {
		Set<StockBook> bookSet = new HashSet<StockBook>();

		Integer testISBN1 = 18;
		Integer testISBN2 = 19;
		
		// Books to test on
		bookSet.add(new ImmutableStockBook(testISBN1,
				"A Dissertation on the Use of Adult Diapers In Eastern Mongolia",
				"SomeAuthor", (float) 99, 5, 0, 0, 0, false));
		bookSet.add(new ImmutableStockBook(testISBN2,
				"Facts about the Tautology Club", 
				"Will Smith", (float) 98, 5, 0, 0, 0, false));	
		
		// First we need to add the books to the database
		try {
			storeManager.addBooks(bookSet);
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		}

		BookRating rating1 = new BookRating(testISBN1, 4);
		BookRating rating2 = new BookRating(testISBN2, 5);
		Set<BookRating> ratings = new HashSet<BookRating>();
		ratings.add(rating1);
		ratings.add(rating2);
		
		// Create clients
		Thread rater1 = new Thread (new ConcurrentRatingTest(ratings, 2));
		Thread rater2 = new Thread (new ConcurrentRatingTest(ratings, 2));
		
		// Run threads
		rater1.start();
		rater2.start();
	
		// Wait
		try {
			rater1.join();
			rater2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		List<StockBook> books = null;
		try {
			books = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
	
		// Total expected rating:
		int expectedRatingISBN1 = 4 * (2 + 2);
		int expectedRatingISBN2 = 5 * (2 + 2);
		int expectedAvgRatingISBN1 = expectedRatingISBN1 / 4;
		int expectedAvgRatingISBN2 = expectedRatingISBN2 / 4;
		
		for (StockBook book : books) {
			if (book.getISBN() == testISBN1) {
				assertTrue(book.getAverageRating() == expectedAvgRatingISBN1);
			}
			if (book.getISBN() == testISBN2) {
				assertTrue(book.getAverageRating() == expectedAvgRatingISBN2);
			}
		}
	}
	/**
	 * Our own concurrency test for getBooksInDemand.
	 */
	@Test
	public void testConcurrentInDemand () {
		Set<StockBook> bookSet = new HashSet<StockBook>();

		Integer testISBN1 = 20;
		Integer testISBN2 = 21;
		
		// Books to test on
		bookSet.add(new ImmutableStockBook(testISBN1,
				"A Tale of Things and Stuff",
				"Stephen Ming", (float) 99, 1, 0, 0, 0, false));
		bookSet.add(new ImmutableStockBook(testISBN2,
				"Facts the Truth of Factoids",
				"<3 Justin Bieber <3", (float) 98, 1, 0, 0, 0, false));	
		
		// First we need to add the books to the database
		try {
			storeManager.addBooks(bookSet);
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		}

		// Initial demand
		BookCopy bookCopy1 = new BookCopy(testISBN1, 2);
		BookCopy bookCopy2 = new BookCopy(testISBN2, 2);
		Set<BookCopy> bookCopySet1 = new HashSet<BookCopy>();
		Set<BookCopy> bookCopySet2 = new HashSet<BookCopy>();
		
		bookCopySet1.add(bookCopy1);
		bookCopySet2.add(bookCopy1);
		bookCopySet2.add(bookCopy2);

		// Create clients
		Thread buyer1 = new Thread (new ConcurrentBuyBooksTest(bookCopySet1));
		Thread buyer2 = new Thread (new ConcurrentBuyBooksTest(bookCopySet2));
		
		// Run threads
		buyer1.start();
		buyer2.start();
	
		// Wait
		try {
			buyer1.join();
			buyer2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		List<StockBook> books = null;
		try {
			books = storeManager.getBooksInDemand();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		for (StockBook book : books) {
			if (book.getISBN() == testISBN1) {
				assertTrue(book.getSaleMisses() == 2);
			}
			if (book.getISBN() == testISBN2) {
				assertTrue(book.getSaleMisses() == 1);
			}
		}

	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		if (error != null) {
			throw error;
		}
		if (error != null) {
			throw error;
		}
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
			stopTestServer ();
		}
	}
	
	public static class ConcurrentAddCopiesTest implements Runnable {
		Set<BookCopy> bookCopies;
		public ConcurrentAddCopiesTest (Set<BookCopy> bookCopies) {
			this.bookCopies = bookCopies;
		}
		public void run() {
			try {
				storeManager.addCopies(bookCopies);
			} catch (BookStoreException e) {
				e.printStackTrace();
				error = new Error();
				fail();
			}
		}
	}
	
	public static class ConcurrentBuyBooksTest implements Runnable {
		Set<BookCopy> books;
		public ConcurrentBuyBooksTest (Set<BookCopy> books) {
			this.books = books;
		}
		public void run() {
			try {
				client.buyBooks(books);
			} catch (BookStoreException e) {
				e.printStackTrace();
				error = new Error();
				fail();
			}
		}
	}
	
	public static class ConcurrentGetBooksTest implements Runnable {
		Set<Integer> isbns;
		int tries;
		List<StockBook> before;
		List<StockBook> after;
		public ConcurrentGetBooksTest (List<StockBook> before,
									   List<StockBook> after,
									   Set<Integer> ISBNList,
									   int tries) {
			this.before = before;
			this.after  = after;
			this.isbns  = ISBNList;
			this.tries  = tries;
		}

		public void run() {
			List<StockBook> booksFromServer = null;
			for (int i=0; i < tries; i++)
				try {
					booksFromServer = storeManager.getBooks();
				} catch (BookStoreException e) {
					e.printStackTrace();
					error = new Error("Error in getting books in ConcurrentGetBooksTest");
				}
				if (! (compareBooks(booksFromServer, before) 
				    || compareBooks(booksFromServer, after))) {
					// Make error for JUnit
					error = new Error("Error in ConcurrentGetBooksTest");
				}
		}
		public boolean compareBooks (List<StockBook> books1, List<StockBook> books2) {
			class StockBookCompareISBN implements Comparator<StockBook> {
				@Override
				public int compare(StockBook book0, StockBook book1) {
					return (int) (book1.getISBN() - book0.getISBN());
				}
			}
			Collections.sort(books1, new StockBookCompareISBN());
			Collections.sort(books2, new StockBookCompareISBN());
			if (books1.size() != books2.size()) {
				return false;
			}
			for (int i = 0; i < books1.size(); i++) {
				if (! books1.get(i).equals(books2.get(i))) {
					return false;
				}
			}
			return true;
		}
	}
	public static class ConcurrentBuyAndAddTest implements Runnable {
		Set<BookCopy> bookCopies;
		int tries;
		public ConcurrentBuyAndAddTest (Set<BookCopy> bookCopies, int tries) {
			this.bookCopies = bookCopies;
			this.tries = tries;
		}
		public void run() {
			for (int i=0; i < tries; i++)
				try {
					client.buyBooks(bookCopies);
					storeManager.addCopies(bookCopies);
				} catch (BookStoreException e) {
					e.printStackTrace();
					error = new Error("Error in ConcurrentBuyAndAddTest.");
				}
		}
	}
	public static class ConcurrentRatingTest implements Runnable {
		Set<BookRating> ratings;
		int numRatings;
		public ConcurrentRatingTest (Set<BookRating> ratings, int numRatings) {
			this.ratings = ratings;
			this.numRatings = numRatings;
		}
		public void run() {
			for (int i = 0; i < numRatings; i++) {
				try {
					client.rateBooks(ratings);
				} catch (BookStoreException e) {
					e.printStackTrace();
					error = new Error("Error in ConcurrentRatingTest1.");
				}
			}
		}
	}
}
