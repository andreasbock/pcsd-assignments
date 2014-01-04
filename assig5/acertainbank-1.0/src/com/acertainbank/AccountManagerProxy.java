package com.acertainbank;

import java.util.HashMap;

public class AccountManagerProxy implements AccountManager {
	public static HashMap<Integer, AccountManagerPartition> branches;
	
	@Override
	public void credit(int branchId, int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// Validate amount
		if (amount < 0) {
			throw new NegativeAmountException();
		}
		
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		}
		
		// Look up account
		AccountManagerPartition partition = branches.get(branchId);
		partition.performCredit(accountId, amount);
	}

	@Override
	public void debit(int branchId, int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		// Validate amount
		if (amount < 0) {
			throw new NegativeAmountException();
		}
		
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		} 
		
		// Look up account
		AccountManagerPartition partition = branches.get(branchId);
		partition.performDebit(accountId, amount);	
	}

	@Override
	public void transfer(int branchId, int accountIdOrig, int accountIdDest,
			double amount) throws InexistentBranchException,
			InexistentAccountException, NegativeAmountException {
		// Validate amount
		if (amount < 0) {
			throw new NegativeAmountException();
		}
		
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		} 
	
		AccountManagerPartition partition = branches.get(branchId);
		partition.performTransfer(accountIdOrig, accountIdDest, amount);	

	}

	@Override
	public double calculateExposure(int branchId)
			throws InexistentBranchException {
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		} 
		return branches.get(branchId).performCalculateExposure();
	}
}
