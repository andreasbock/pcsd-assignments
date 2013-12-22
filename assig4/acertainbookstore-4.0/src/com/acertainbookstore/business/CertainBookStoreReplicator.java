package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.client.BookStoreClientConstants;
import com.acertainbookstore.interfaces.Replicator;

/**
 * CertainBookStoreReplicator is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {

	private ExecutorService executor;
	
	private HttpClient client;
	
	public CertainBookStoreReplicator(int maxReplicatorThreads)   throws Exception {
		executor = Executors.newFixedThreadPool(maxReplicatorThreads);
		client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(200); 
		client.setThreadPool(new QueuedThreadPool(200));
		client.setTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS);
		client.start();
	}

	public List<Future<ReplicationResult>> replicate(Set<String> slaveServers,
			ReplicationRequest request) 
	{
		CertainBookStoreReplicationTask rep;
		FutureTask<ReplicationResult> futureTask;
		List<Future<ReplicationResult>> results = new ArrayList<Future<ReplicationResult>>();
		
		for( String addr : slaveServers){
			rep = new CertainBookStoreReplicationTask(request, addr, client);
			futureTask = new FutureTask<ReplicationResult>(rep);
			executor.execute(futureTask);
			results.add(futureTask);
		}
		
		return results;
	}
}
