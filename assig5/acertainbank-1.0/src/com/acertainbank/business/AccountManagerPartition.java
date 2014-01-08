package com.acertainbank.business;
import java.util.HashMap;

import com.acertainbank.interfaces.AccountManager;
import com.acertainbank.util.InexistentAccountException;
import com.acertainbank.util.InexistentBranchException;
import com.acertainbank.util.NegativeAmountException;

public class AccountManagerPartition implements AccountManager {
	private HashMap<Integer, Branch> branches;

	@Override
	public void credit(int branchId, int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		}
		
		// Look up account
		Branch branch = branches.get(branchId);
		branch.performCredit(accountId, amount);
	}

	@Override
	public void debit(int branchId, int accountId, double amount)
			throws InexistentBranchException, InexistentAccountException,
			NegativeAmountException {
		
		
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		} 
		
		// Look up account
		Branch branch = branches.get(branchId);
		branch.performDebit(accountId, amount);	
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
	
		Branch branch = branches.get(branchId);
		branch.performTransfer(accountIdOrig, accountIdDest, amount);	

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

	@Override
	public int createAccount(int branchId) throws InexistentBranchException {
		// Look up branch
		if (!branches.containsKey(branchId)) {
			throw new InexistentBranchException();
		} 
		return branches.get(branchId).performCreateAccount();
	}
}
