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
		if (!partition.containsAccount(accountId)) {
			throw new InexistentAccountException();
		}
		
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
		if (!partition.containsAccount(accountId)) {
			throw new InexistentAccountException();
		}
		
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
		
		// Look up accounts
		AccountManagerPartition partition = branches.get(branchId);
		if (!partition.containsAccount(accountIdOrig)) {
			throw new InexistentAccountException(accountIdOrig);
		}
		if (!partition.containsAccount(accountIdDest)) {
			throw new InexistentAccountException(accountIdDest);
		}
		
		partition.performTransfer(accountIdOrig, accountIdDest, amount);	

	}

	@Override
	public double calculateExposure(int branchId)
			throws InexistentBranchException {
		// TODO Auto-generated method stub
		return 0;
	}

}
