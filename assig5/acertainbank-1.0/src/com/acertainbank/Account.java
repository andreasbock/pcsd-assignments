package com.acertainbank;

public class Account {
	private double balance;

	public double getBalance() {
		return balance;
	}
	public void creditAccount(double amount) {
		this.balance += amount;
	}
	public void debitAccount(double amount) {
		this.balance -= amount;
	}
	public double overdraft() {
		return Math.abs(Math.min(0, balance));
	}
}
