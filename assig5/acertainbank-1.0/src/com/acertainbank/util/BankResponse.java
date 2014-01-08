package com.acertainbank.util;

import java.util.List;

/**
 * 
 * Data Structure that we use to communicate objects and error messages from the
 * server to the client.
 * 
 */
public class BankResponse {
	private Exception exception = null;
	
	private List<?> result = null;

	public BankResponse() {

	}

	public BankResponse(Exception exception, List<?> result) {
		this.setException(exception);
		this.setResult(result);
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public List<?> getResult() {
		return result;
	}

	public void setResult(List<?> result) {
		this.result = result;
	}

}
