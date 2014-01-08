package com.acertainbank.business;

import com.acertainbank.util.NegativeAmountException;


public class Account {
	private double balance;

	public double getBalance() {
		return balance;
	}
	public void creditAccount(double amount) throws NegativeAmountException {
		// Validate amount
		if (amount < 0) {
			throw new NegativeAmountException();
		}
		this.balance += amount;
	}
	public void debitAccount(double amount) throws NegativeAmountException {
		// Validate amount
		if (amount < 0) {
			throw new NegativeAmountException();
		}
		this.balance -= amount;
	}
	public double overdraft() {
		return Math.abs(Math.min(0, balance));
	}
}
