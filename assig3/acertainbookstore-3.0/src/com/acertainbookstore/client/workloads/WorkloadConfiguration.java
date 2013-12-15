package com.acertainbookstore.client.workloads;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;

/**
 * 
 * WorkloadConfiguration represents the configuration parameters to be used by
 * Workers class for running the workloads
 * 
 */
public class WorkloadConfiguration {
	private int numBooksToBuy = 5;			// customer
	private int numEditorPicksToGet = 10;	// customer
	private int numAmountToBuy = 3;		// customer
	private int numReplenishBooks = 10;	// manager replenish
	private int numReplenishCopies = 10;	// manager replenish
	private int numBooksToAdd = 5;			// manager add new
	private int warmUpRuns = 100;
	private int numActualRuns = 500;
	private float percentRareStockManagerInteraction = 10f;
	private float percentFrequentStockManagerInteraction = 40f;
	private BookSetGenerator bookSetGenerator = null;
	private StockManager stockManager = null;
	private BookStore bookStore = null;

	public int getNumBooksToBuy() {
		return numBooksToBuy;
	}

	public void setNumBooksToBuy(int numBooksToBuy) {
		this.numBooksToBuy = numBooksToBuy;
	}

	public int getNumBooksToAdd() {
		return numBooksToAdd;
	}

	public void setNumBooksToAdd(int numBooksToAdd) {
		this.numBooksToAdd = numBooksToAdd;
	}

	public WorkloadConfiguration(String serverAddress, boolean localTest)
			throws Exception {
		// Create a new one so that it is not shared
		bookSetGenerator = new BookSetGenerator();
		// Initialize the RPC interfaces if its not a localTest
		if (localTest) {
			stockManager = CertainBookStore.getInstance();
			bookStore = CertainBookStore.getInstance();
		} else {
			stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
			bookStore = new BookStoreHTTPProxy(serverAddress);
		}
	}

	public StockManager getStockManager() {
		return stockManager;
	}

	public BookStore getBookStore() {
		return bookStore;
	}

	public float getPercentRareStockManagerInteraction() {
		return percentRareStockManagerInteraction;
	}

	public void setPercentRareStockManagerInteraction(
			float percentRareStockManagerInteraction) {
		this.percentRareStockManagerInteraction = percentRareStockManagerInteraction;
	}

	public float getPercentFrequentStockManagerInteraction() {
		return percentFrequentStockManagerInteraction;
	}

	public void setPercentFrequentStockManagerInteraction(
			float percentFrequentStockManagerInteraction) {
		this.percentFrequentStockManagerInteraction = percentFrequentStockManagerInteraction;
	}

	public int getWarmUpRuns() {
		return warmUpRuns;
	}

	public void setWarmUpRuns(int warmUpRuns) {
		this.warmUpRuns = warmUpRuns;
	}

	public int getNumActualRuns() {
		return numActualRuns;
	}

	public void setNumActualRuns(int numActualRuns) {
		this.numActualRuns = numActualRuns;
	}

	public int getNumEditorPicksToGet() {
		return numEditorPicksToGet;
	}

	public void setNumEditorPicksToGet(int numEditorPicksToGet) {
		this.numEditorPicksToGet = numEditorPicksToGet;
	}

	public BookSetGenerator getBookSetGenerator() {
		return bookSetGenerator;
	}

	public void setBookSetGenerator(BookSetGenerator bookSetGenerator) {
		this.bookSetGenerator = bookSetGenerator;
	}

	public int getNumReplenishBooks() {
		return numReplenishBooks;
	}

	public void setNumReplenishBooks(int numReplenishBooks) {
		this.numReplenishBooks = numReplenishBooks;
	}

	public int getNumReplenishCopies() {
		return numReplenishCopies;
	}

	public void setNumReplenishCopies(int numReplenishCopies) {
		this.numReplenishCopies = numReplenishCopies;
	}

	public int getNumAmountToBuy() {
		return numAmountToBuy;
	}

	public void setNumAmountToBuy(int numAmountToBuy) {
		this.numAmountToBuy = numAmountToBuy;
	}

}
