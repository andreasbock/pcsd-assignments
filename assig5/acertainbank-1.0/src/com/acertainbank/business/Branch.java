package com.acertainbank.business;

import java.util.HashMap;

import com.acertainbank.util.InexistentAccountException;
import com.acertainbank.util.NegativeAmountException;

public class Branch {
	
	private HashMap<Integer, Account> accounts;
	int account_incrementer;
	
	Branch(){
		accounts = new HashMap<Integer, Account>();
		account_incrementer = 0;
	}
	
	public synchronized void performDebit(int accountId, double amount)
			throws InexistentAccountException, NegativeAmountException {
		// Look up account
		if (this.containsAccount(accountId)) {
			throw new InexistentAccountException(accountId);
		}
		accounts.get(accountId).debitAccount(amount);
	}
	
	public synchronized void performCredit(int accountId, double amount)
			throws InexistentAccountException, NegativeAmountException {
		// Look up account
		if (this.containsAccount(accountId)) {
			throw new InexistentAccountException(accountId);
		}
		accounts.get(accountId).creditAccount(amount);
	}
	
	public synchronized void performTransfer(int accountIdOrig, int accountIdDest, double amount)
			throws InexistentAccountException, NegativeAmountException {
		if (this.containsAccount(accountIdOrig)) {
			throw new InexistentAccountException(accountIdOrig);
		}
		if (this.containsAccount(accountIdDest)) {
			throw new InexistentAccountException(accountIdDest);
		}
		accounts.get(accountIdOrig).debitAccount(amount);
		accounts.get(accountIdDest).creditAccount(amount);
	}

	public synchronized double performCalculateExposure() {
		double trouble = 0; // GET IT??? HUR HUR HUR
		
		for (Account acc : accounts.values()) {
			trouble += acc.overdraft();
		}
		
		return trouble;
	}
	
	public synchronized int performCreateAccount() {
		this.accounts.put(account_incrementer, new Account());
		return account_incrementer++;
	}
	
	public boolean containsAccount (int accountId) {
		return accounts.containsKey(accountId);
	}
}