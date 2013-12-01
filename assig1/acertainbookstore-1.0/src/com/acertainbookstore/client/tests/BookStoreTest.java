package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.server.BookStoreHTTPMessageHandler;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Test class to test the BookStore interface
 *
 */
public class BookStoreTest {

	private static boolean localTest = true; 
	private static StockManager storeManager;
	private static BookStore client;
	private static BookStoreHTTPMessageHandler handler;

	public static void startTestServer () {
		handler = new BookStoreHTTPMessageHandler();
	}
	
	public static void stopTestServer () {
		try {
			handler.stop();
		} catch (Exception e) {
			// Do nothing
			e.printStackTrace();
		}
	}
	@BeforeClass
	public static void setUpBeforeClass() {
		
		startTestServer();
		
		try {
			if (localTest) {
				storeManager = CertainBookStore.getInstance();
				client = CertainBookStore.getInstance();
			} else {
				storeManager = new StockManagerHTTPProxy(
						"http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Here we want to test buyBooks functionality
	 * 
	 * 1. First we add a book with 5 copies.
	 * 
	 * 2. We buy two copies of this book.
	 * 
	 * 3. We check that after buying a copy number of copies is reduced to 3.
	 * 
	 * 4. We also try to buy non-existing books/ invalid ISBN and check that
	 * appropriate exceptions are thrown.
	 * 
	 * 5. We also try to buy 5 copies of the book and check that it is not
	 * possible to buy and the appropriate exception is thrown
	 */
	@Test
	public void testBuyBooks() {
		Integer testISBN = 300;
		Integer numCpies = 5;
		int buyCopies = 2;

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
				"Author Name", (float) 100, numCpies, 0, 0, 0, false));
		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		List<StockBook> listBooks = null;
		booksToBuy.add(new BookCopy(testISBN, buyCopies));
		try {
			client.buyBooks(booksToBuy);
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		for (StockBook b : listBooks) {
			if (b.getISBN() == testISBN) {
				assertTrue("Num copies  after buying one copy",
						b.getNumCopies() == numCpies - buyCopies);
				break;
			}
		}

		Boolean invalidISBNExceptionThrown = false;
		booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(-1, 1));
		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException e) {
			invalidISBNExceptionThrown = true;
		}
		assertTrue(invalidISBNExceptionThrown);
		List<StockBook> currentList = null;
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Boolean nonExistingISBNExceptionThrown = false;
		booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(100000, 10));
		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException e) {
			nonExistingISBNExceptionThrown = true;
		}
		assertTrue(nonExistingISBNExceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Boolean cannotBuyExceptionThrown = false;
		booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(testISBN, numCpies));
		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException e) {
			cannotBuyExceptionThrown = true;
		}
		assertTrue(cannotBuyExceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * 
	 * Here we want to test getBooks(List<Integer> ISBbList)
	 * 
	 * 1. We add a book with ISBN = testISBN.
	 * 
	 * 2. We try to retrieve this book by executing getBooks with a list
	 * containing testISBN.
	 * 
	 * 3. We also test that getBooks executed with incorrect arguments throws
	 * exception.
	 * 
	 */
	@Test
	public void testGetBooks() {
		Integer testISBN = 400;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
				"Book Author", (float) 100, 5, 0, 0, 0, false));
		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			e.printStackTrace();
		}

		Set<Integer> ISBNList = new HashSet<Integer>();
		ISBNList.add(testISBN);
		List<Book> books = null;
		try {
			books = client.getBooks(ISBNList);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		for (Book b : books) {
			if (b.getISBN() == testISBN) {
				assertTrue("Book ISBN", b.getISBN() == testISBN);
				assertTrue("Book Price", b.getPrice() == 100);
			}
		}
		List<StockBook> listBooks = null;
		try {
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		Boolean exceptionThrown = false;
		ISBNList = new HashSet<Integer>();
		ISBNList.add(-1);
		try {
			books = client.getBooks(ISBNList);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		List<StockBook> currentList = null;
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		exceptionThrown = false;
		ISBNList = new HashSet<Integer>();
		ISBNList.add(10000000);
		try {
			books = client.getBooks(ISBNList);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * 
	 * Here we want to test rateBook functionality
	 * 
	 * 1. We add a book.
	 * 
	 * 2. We rate it using rateBook to certain rating.
	 * 
	 * 3. We check if the rating is updated by executing getBooks.
	 *
	 * 4. We repeat steps 2 and 3 with another rating to make sure
	 * 		average ratings are handled correctly
	 * 
	 * 4. We also check that the appropriate exception is thrown when rateBook
	 * 		is executed with wrong arguments
	 */
	@Test
	public void testRateBook() {
		Integer testISBN = 700;
		Integer rating = 1;
		Integer rating2 = 2;
		Float avgRating = (rating.floatValue() + rating2.floatValue())/2;
		

		// Add book
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
				"Book Author", (float) 100, 5, 0, 0, 0, false));
		try {
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		// Add single rating to the book and test it
		Set<BookRating> bookRatingList = new HashSet<BookRating>();
		bookRatingList.add(new BookRating(testISBN, rating));
		List<StockBook> listBooks = null;
		try {
			client.rateBooks(bookRatingList);
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		}
		for (StockBook book : listBooks) {
			if (book.getISBN() == testISBN) {
				assertTrue(book.getTotalRating() == rating);
				break;
			}
		}
		
		// Add another rating to the book and test it
		bookRatingList = new HashSet<BookRating>();
		bookRatingList.add(new BookRating(testISBN, rating2));
		listBooks = null;
		try {
			client.rateBooks(bookRatingList);
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e1) {
			e1.printStackTrace();
			fail();
		}
		for (StockBook book : listBooks) {
			if (book.getISBN() == testISBN) {
				assertTrue(Math.abs(book.getAverageRating() - avgRating) < 0.5);
				break;
			}
		}
		
		// Test exceptions is thrown when rating is negative
		Boolean exceptionThrown = false;
		bookRatingList.add(new BookRating(testISBN, -1));
		try {
			client.rateBooks(bookRatingList);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		List<StockBook> currentList = null;
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		
		// Test exceptions is thrown when rating is out of range
		exceptionThrown = false;
		bookRatingList.add(new BookRating(testISBN, 11));
		try {
			client.rateBooks(bookRatingList);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		
		// Test exceptions is thrown when ISBN does not exist
		exceptionThrown = false;
		bookRatingList.add(new BookRating(testISBN, 11));
		try {
			client.rateBooks(bookRatingList);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		try {
			currentList = storeManager.getBooks();
			assertTrue(currentList.equals(listBooks));
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		
		
	}

	/**
	 * 
	 * Here we want to test getTopRatedBooks functionality
	 * Thus function assumes no other books in the database with
	 * rating > 3
	 * It also assumes <1000 books in the database
	 * 
	 * 1. We add two books one with ISBN - testISBN and another with ISBN -
	 * testISBN + 1.
	 * 
	 * 2. We rate book with testISBN with rating 5.
	 * 
	 * 3. Then we execute getTopRatedBooks(1) and check that testISBN is
	 * returned.
	 * 
	 * 4. Now we rate testISBN with rating 0 this makes its average rating = 2.5
	 * and testISBN+1 book with rating 4(average rating = 4)
	 * 
	 * 5. We again execute getTopRatedBooks(1) and check that testISBN is not
	 * returned but testISBN+1 is.
	 * 
	 */
	@Test
	public void testGetTopRatedBooks() {
		Integer testISBN = 600;
		List<Book> books = null;
		Set<StockBook> booksToAdd;
		ImmutableStockBook book0, book1, book2;
		Set<BookRating> bookRatingList;
		book0 = new ImmutableStockBook(testISBN, "Book High",
			   "Book Author", (float) 100, 5, 0, 0, 0, false);
		book1 = new ImmutableStockBook(testISBN+1, "Book Low",
			   "Book Author", (float) 100, 5, 0, 0, 0, false);
		book2 = new ImmutableStockBook(testISBN+2, "Book High2",
			   "Book Author", (float) 100, 5, 0, 0, 0, false);
	
		
		
		// Add a book and a rating
		bookRatingList = new HashSet<BookRating>();
		bookRatingList.add(new BookRating(testISBN, 5));
		booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(book0);
		try {
			storeManager.addBooks(booksToAdd);
			client.rateBooks(bookRatingList);
			books = client.getTopRatedBooks(1);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(book0, books.get(0));
	
		// Add new book with lower rating
		bookRatingList = new HashSet<BookRating>();
		booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(book1);
		bookRatingList.add(new BookRating(testISBN+1, 4));
		try {
			storeManager.addBooks(booksToAdd);
			client.rateBooks(bookRatingList);
			books = client.getTopRatedBooks(1);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(book0, books.get(0));
		
		// Add new book with same rating
		bookRatingList = new HashSet<BookRating>();
		booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(book2);
		bookRatingList.add(new BookRating(testISBN+2, 5));
		try {
			storeManager.addBooks(booksToAdd);
			client.rateBooks(bookRatingList);
			books = client.getTopRatedBooks(1);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(book0.equals(books.get(0)) || book1.equals(books.get(0)));

		// Test get multiple highest rated books
		try {
			books = client.getTopRatedBooks(3);
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(books.contains(book0) && books.contains(book1) && books.contains(book2));
		
		// Test argument negative
		boolean exceptionThrown = false;
		try {
			books = client.getTopRatedBooks(-1);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
		
		// Test get more top rated books than books in store
		exceptionThrown = false;
		try {
			books = client.getTopRatedBooks(1010);
		} catch (BookStoreException e) {
			exceptionThrown = true;
		}
		assertFalse(exceptionThrown);
		assertTrue(books.size() < 1010);
		
	}

	
	
	
	@AfterClass
	public static void tearDownAfterClass() {
		
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
		
		stopTestServer();
	}

}
