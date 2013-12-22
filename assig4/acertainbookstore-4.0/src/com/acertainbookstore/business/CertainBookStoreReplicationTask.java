package com.acertainbookstore.business;

import java.util.concurrent.Callable;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * CertainBookStoreReplicationTask performs replication to a slave server. It
 * returns the result of the replication on completion using ReplicationResult
 */
public class CertainBookStoreReplicationTask implements
		Callable<ReplicationResult> {
	
	private ReplicationRequest request;
	private String addr;
	private HttpClient client;
	
	
	public CertainBookStoreReplicationTask(ReplicationRequest request, String addr, HttpClient client) {
		this.request = request;
		this.addr = addr;
		this.client = client;
	}

	@Override
	public ReplicationResult call() {
		String listISBNsxmlString = BookStoreUtility
				.serializeObjectToXMLString(request);
		Buffer requestContent = new ByteArrayBuffer(listISBNsxmlString);

		ContentExchange exchange = new ContentExchange();
		String urlString = addr + "/"
				+ BookStoreMessageTag.REPLICATE;
		exchange.setMethod("POST");
		exchange.setURL(urlString);
		exchange.setRequestContent(requestContent);
		
		try {
			BookStoreUtility.SendAndRecv(this.client, exchange);
		} catch (BookStoreException e) {
			return new ReplicationResult(addr, false);
		}
		return new ReplicationResult(addr, true);
	}
}
