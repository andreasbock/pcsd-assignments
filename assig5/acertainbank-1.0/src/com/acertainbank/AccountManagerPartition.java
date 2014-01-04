package com.acertainbank;

import java.util.HashMap;

public class AccountManagerPartition {
	private HashMap<Integer, Account> accounts;
	
	public void performCredit(int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// TODO Auto-generated method stub

	}

	public void performDebit(int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// TODO Auto-generated method stub

	}
	
	public void performTransfer(int accountIdOrig, int accountIdDest, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// TODO Auto-generated method stub

	}

	public double performCalculateExposure(int branchId)
			throws InexistentBranchException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean containsAccount (int accountId) {
		return accounts.containsKey(accountId);
	}
	
}
