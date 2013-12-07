package com.acertainbookstore.client.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.CertainBookStore;
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
	private static boolean localTest = false;
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
				storeManager = CertainBookStore.getInstance();
				client = CertainBookStore.getInstance();
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
		
		// Recount
		for (StockBook book : bookSet) {
			assertTrue(5 == book.getNumCopies()); // Initial stock was 5
		}
	}
	
	/**
	 * This test is "Test 2" from the assignment text.
	 */
	@Test
	public void testMultipleAddAndBuy () {
		int tries = 1;
		
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
		
		List<Book> before;
		try {
			before = client.getBooks(isbns);
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		} 
		List<Book> after;
		Set<Integer> ISBNList;
		List<Book> booksFromServer = null;
		
		/*
		
		// Create clients
		// TODO: `after` is not yet defined, debugging ^
		Thread getter = new Thread (new ConcurrentGetBooksTest(before, after, ISBNList, tries));
		Thread setter = new Thread (new ConcurrentBuyAndAdd(bookCopySet, tries));
		
		// Run them
		getter.start();
		setter.start();
	
		// Wait
		try {
			getter.join();
			setter.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} */
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
				fail();
			}
		}
	}
	
	public static class ConcurrentGetBooksTest implements Runnable {
		Set<Integer> isbns;
		int tries;
		List<Book> before;
		List<Book> after;
		public ConcurrentGetBooksTest (List<Book> before,
									   List<Book> after,
									   Set<Integer> ISBNList,
									   int tries) {
			this.isbns  = ISBNList;
			this.tries  = tries;
			this.before = before;
			this.after  = after;
		}
		public void run() {
			List<Book> booksFromServer = null;
			for (int i=0; i < tries; i++)
				try {
					booksFromServer = client.getBooks(isbns);
				} catch (BookStoreException e) {
					e.printStackTrace();
					fail();
				}
				if (! (booksFromServer.equals(before) 
				    || booksFromServer.equals(after))) {
					// Make error for JUnit
					error = new Error("Error in ConcurrentGetBooksTest");
				}
		}
	}
	
	public static class ConcurrentBuyAndAdd implements Runnable {
		Set<BookCopy> bookCopies;
		int tries;
		public ConcurrentBuyAndAdd (Set<BookCopy> bookCopies, int tries) {
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
					fail();
				}
		}
	}
}
