package com.acertainbank.util;

public class BankClientException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public BankClientException() {
		super();
	}

	public BankClientException(String message) {
		super(message);
	}

	public BankClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public BankClientException(Throwable ex) {
		super(ex);
	}
}
