package com.acertainbank.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbank.interfaces.AccountManager;
import com.acertainbank.util.InexistentAccountException;
import com.acertainbank.util.InexistentBranchException;
import com.acertainbank.util.NegativeAmountException;

public class AccountManagerProxy implements AccountManager {
	
	private HttpClient client;
	
	private final String PROPERTY_FILE = "proxy.properties";
	private HashMap<Integer, String> branchToPartition = new HashMap<Integer, String>();
	
	public AccountManagerProxy() throws Exception{		
		initPartitions();
		
		client = new HttpClient();
		client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		client.setMaxConnectionsPerAddress(BankClientConstants.CLIENT_MAX_CONNECTION_ADDRESS); // max
																									// concurrent
																									// connections
																									// to
																									// every
																									// address
		client.setThreadPool(new QueuedThreadPool(
				BankClientConstants.CLIENT_MAX_THREADSPOOL_THREADS)); // max
																			// threads
		client.setTimeout(BankClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS); // seconds
																					// timeout;
																					// if
																					// no
																					// server
																					// reply,
																					// the
																					// request
																					// expires
		client.start();
	}
	
	private void initPartitions() throws FileNotFoundException, IOException{
		
		Properties props = new Properties();
		props.load(new FileInputStream(PROPERTY_FILE));
		
		for (Object key : props.keySet()){
			if (((String)key).startsWith("branch_")){
				String[] value = props.getProperty((String)key).split(", ");
				branchToPartition.put(Integer.parseInt(value[0]), value[1]);
			}
		}
		
		System.out.println("Proxy loaded partitions:\n" + branchToPartition.toString());
	}

	@Override
	public void credit(int branchId, int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// TODO Auto-generated method stub	
	}

	@Override
	public void debit(int branchId, int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transfer(int branchId, int accountIdOrig, int accountIdDest,
			double amount) throws InexistentBranchException,
			InexistentAccountException, NegativeAmountException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double calculateExposure(int branchId)
			throws InexistentBranchException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int createAccount(int branchId) throws InexistentBranchException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
