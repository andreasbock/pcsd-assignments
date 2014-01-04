package com.acertainbank;

import java.util.HashMap;

public class AccountManagerPartition {
	private HashMap<Integer, Account> accounts;
	
	public synchronized void performCredit(int accountId, double amount)
		throws InexistentAccountException {
	// Look up account
	if (this.containsAccount(accountId)) {
		throw new InexistentAccountException(accountId);
	}
	
	accounts.get(accountId).creditAccount(amount);
	}

	public synchronized void performDebit(int accountId, double amount)
			throws InexistentAccountException {
		// Look up account
		if (this.containsAccount(accountId)) {
			throw new InexistentAccountException(accountId);
		}
		accounts.get(accountId).debitAccount(amount);
	}
	
	public synchronized void performTransfer(int accountIdOrig, int accountIdDest, double amount)
			throws InexistentAccountException {
		if (this.containsAccount(accountIdOrig)) {
			throw new InexistentAccountException(accountIdOrig);
		}
		if (this.containsAccount(accountIdDest)) {
			throw new InexistentAccountException(accountIdDest);
		}
		accounts.get(accountIdOrig).debitAccount(amount);
		accounts.get(accountIdDest).creditAccount(amount);
	}

	public synchronized double performCalculateExposure(int branchId)
			throws InexistentBranchException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean containsAccount (int accountId) {
		return accounts.containsKey(accountId);
	}
}
