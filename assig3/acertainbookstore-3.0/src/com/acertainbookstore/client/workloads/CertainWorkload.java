/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numConcurrentWorkloadThreads = 20;
		for (int threads = 1; threads <= numConcurrentWorkloadThreads; threads++) {
			String serverAddress = "http://localhost:8081";
			boolean localTest = false;
			List<WorkerRunResult> workerRunResults = new ArrayList<WorkerRunResult>();
			List<Future<WorkerRunResult>> runResults = new ArrayList<Future<WorkerRunResult>>();
	
			initializeBookStoreData(serverAddress, localTest);
	
			ExecutorService exec = Executors
					.newFixedThreadPool(threads);
	
			for (int i = 0; i < threads; i++) {
				// The server address is ignored if localTest is true
				WorkloadConfiguration config = new WorkloadConfiguration(
						serverAddress, localTest);
				Worker workerTask = new Worker(config);
				// Keep the futures to wait for the result from the thread
				runResults.add(exec.submit(workerTask));
			}
	
			// Get the results from the threads using the futures returned
			for (Future<WorkerRunResult> futureRunResult : runResults) {
				WorkerRunResult runResult = futureRunResult.get(); // blocking call
				workerRunResults.add(runResult);
			}
	
			exec.shutdownNow(); // shutdown the executor
			reportMetric(workerRunResults, threads);
		}
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 * @param numConcurrentWorkloadThreads 
	 */
	public static void reportMetric(List<WorkerRunResult> workerRunResults, int threads) {
		float successfulCust = 0;
		float totalCust = 0;
		float time = 0;
		for (WorkerRunResult result : workerRunResults) {
			successfulCust += result.getSuccessfulFrequentBookStoreInteractionRuns();
			totalCust += result.getTotalFrequentBookStoreInteractionRuns();
			time += result.getElapsedTimeInNanoSecs();
		}
		float averageseconds = (time / workerRunResults.size()) / 1000000000;
		float throughput  = successfulCust / averageseconds;
		float latency = averageseconds / (successfulCust / workerRunResults.size());
		System.out.println(threads + ", " + throughput + ", " + latency + ", " + successfulCust / totalCust);
	}

	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 * @param serverAddress
	 * @param localTest
	 * @throws Exception
	 */
	public static void initializeBookStoreData(String serverAddress,
			boolean localTest) throws Exception {
		BookStore bookStore = null;
		StockManager stockManager = null;
		// Initialize the RPC interfaces if its not a localTest
		if (localTest) {
			stockManager = CertainBookStore.getInstance();
			bookStore = CertainBookStore.getInstance();
		} else {
			stockManager = new StockManagerHTTPProxy(serverAddress + "/stock");
			bookStore = new BookStoreHTTPProxy(serverAddress);
		}

		stockManager.reset();
		int initialStock = 500; // Prone to change depending on results
		Set<StockBook> stockBooks = BookSetGenerator.nextSetOfStockBooks(initialStock);
		stockManager.addBooks(stockBooks);
				
		// Finished initialization, stop the clients if not localTest
		if (!localTest) {
			((BookStoreHTTPProxy) bookStore).stop();
			((StockManagerHTTPProxy) stockManager).stop();
		}

	}
}
