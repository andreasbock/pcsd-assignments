package com.acertainbookstore.client.workloads;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;



/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
	static Random rGen = new Random();
	private WorkloadConfiguration configuration = null;
	private int numSuccessfulFrequentBookStoreInteraction = 0;
	private int numTotalFrequentBookStoreInteraction = 0;

	public Worker(WorkloadConfiguration config) {
		configuration = config;
	}

	/**
	 * Run the appropriate interaction while trying to maintain the configured
	 * distributions
	 * 
	 * Updates the counts of total runs and successful runs for customer
	 * interaction
	 * 
	 * @param chooseInteraction
	 * @return
	 */
	private boolean runInteraction(float chooseInteraction) {
		try {
			runFrequentBookStoreInteraction();
			
			if (chooseInteraction < configuration.getPercentRareStockManagerInteraction()) {
				runRareStockManagerInteraction();
			} else if (chooseInteraction < configuration.getPercentFrequentStockManagerInteraction()) {
				runFrequentStockManagerInteraction();
			} else {
				numTotalFrequentBookStoreInteraction++;
				runFrequentBookStoreInteraction();
				numSuccessfulFrequentBookStoreInteraction++;
			}
			
			
		} catch (BookStoreException ex) {
//			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Run the workloads trying to respect the distributions of the interactions
	 * and return result in the end
	 */
	public WorkerRunResult call() throws Exception {
		int count = 1;
		long startTimeInNanoSecs = 0;
		long endTimeInNanoSecs = 0;
		int successfulInteractions = 0;
		long timeForRunsInNanoSecs = 0;

		Random rand = new Random();
		float chooseInteraction;

		// Perform the warmup runs
		while (count++ <= configuration.getWarmUpRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			runInteraction(chooseInteraction);
		}

		count = 1;
		numTotalFrequentBookStoreInteraction = 0;
		numSuccessfulFrequentBookStoreInteraction = 0;

		// Perform the actual runs
		startTimeInNanoSecs = System.nanoTime();
		while (count++ <= configuration.getNumActualRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			if (runInteraction(chooseInteraction)) {
				successfulInteractions++;
			}
		}
		endTimeInNanoSecs = System.nanoTime();
		timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
		return new WorkerRunResult(successfulInteractions,
				timeForRunsInNanoSecs, configuration.getNumActualRuns(),
				numSuccessfulFrequentBookStoreInteraction,
				numTotalFrequentBookStoreInteraction);
	}

	/**
	 * Runs the new stock acquisition interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runRareStockManagerInteraction() throws BookStoreException {
		StockManager stockManager = configuration.getStockManager();
		int booksToAdd = configuration.getNumBooksToAdd();
		
		List<StockBook> books = stockManager.getBooks();
		
		Set<StockBook> genStockBooks = BookSetGenerator.nextSetOfStockBooks(booksToAdd);
		genStockBooks.removeAll(books);
		
		stockManager.addBooks(genStockBooks);
	}

	/**
	 * Runs the stock replenishment interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentStockManagerInteraction() throws BookStoreException {
		StockManager stockManager = configuration.getStockManager();
		int addBooks = configuration.getNumReplenishBooks();
		int addCopies = configuration.getNumReplenishCopies();
		
		List<StockBook> books = stockManager.getBooks();
		Collections.sort(books, NumCopiesComparator);
		books = books.subList(0, addBooks);
		
		Set<BookCopy> bookCopiesSet = new HashSet<BookCopy>();
		
		for (Book book : books) {
			bookCopiesSet.add(new BookCopy(book.getISBN(),addCopies));
		}
		
		stockManager.addCopies(bookCopiesSet);
	}

	/**
	 * Runs the customer interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentBookStoreInteraction() throws BookStoreException {
		BookStore store = configuration.getBookStore();
		int editorPicks = configuration.getNumEditorPicksToGet();
		int booksToBuy = configuration.getNumBooksToBuy();
		int amountToBuy = configuration.getNumAmountToBuy();
		
		List<Book> books = store.getEditorPicks(editorPicks);
		
		books = BookSetGenerator.sampleFromSetOfISBNs(books, booksToBuy);
		
		Set<BookCopy> buyBooks = new HashSet<BookCopy>();
		for (Book book : books) {
			buyBooks.add(new BookCopy(book.getISBN(),amountToBuy));
		}

		store.buyBooks(buyBooks);
	}

	public static Comparator<StockBook> NumCopiesComparator = new Comparator<StockBook>() {
		public int compare(StockBook book1, StockBook book2) {
			//ascending order
			return book1.getNumCopies() - book2.getNumCopies();
		}
	};
	
}
