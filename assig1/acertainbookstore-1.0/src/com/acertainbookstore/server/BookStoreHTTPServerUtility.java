/**
 * 
 */
package com.acertainbookstore.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

/**
 * Utility methods to create Jetty server instances
 * 
 */
public class BookStoreHTTPServerUtility {

	/**
	 * Creates a server on the port and blocks the calling thread
	 */
	public static boolean createServer(int port, AbstractHandler handler) {
		Server server = new Server(port);
		if (handler != null) {
			server.setHandler(handler);
		}
		
		SelectChannelConnector connector0 = new SelectChannelConnector();
        connector0.setPort(port);
        connector0.setReuseAddress(true);

		try {
			 server.setConnectors(new Connector[]
		                { connector0 });
			server.start();
			server.join();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Creates a server on the InetAddress and blocks the calling thread
	 */
	public static boolean createServer(String ipAddress, int port,
			AbstractHandler handler) {
		InetAddress inetIpAddress;
		InetSocketAddress address;
		Server server;

		if (ipAddress == null)
			return false;

		try {
			inetIpAddress = InetAddress.getByName(ipAddress);
			address = new InetSocketAddress(inetIpAddress, port);
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			return false;
		}

		server = new Server(address);
		if (handler != null) {
			server.setHandler(handler);
		}

		try {
			server.start();
			server.join();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}

	
	public static Thread startServerThread(final int port, final AbstractHandler handler){
		Thread thread = new Thread(new Runnable() {

		    public void run() {
		    	System.out.println(((Integer)port).toString());
		    	createServer(port, handler);
		    }
			
			
		});
		thread.start();
		return thread;
	}
	
}

