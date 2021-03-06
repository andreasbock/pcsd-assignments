/**
 * 
 */
package com.acertainbank.util;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;

import com.acertainbank.client.BankClientConstants;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * BookStoreUtility implements utility methods used by bookstore servers and
 * clients
 * 
 */
public final class BankUtility {

	public static boolean isInvalidISBN(int isbn) {
		return (isbn < 1);
	}

	public static boolean isInvalidRating(int rating) {
		return (rating < 0 || rating > 5);
	}

	public static boolean isInvalidNoCopies(int copies) {
		return (copies < 1);
	}

	/**
	 * Checks if a string is empty or null
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return ((str == null) || str.isEmpty());
	}

	/**
	 * Converts a string to a float if possible else it returns the signal value
	 * for failure passed as parameter
	 * 
	 * @param str
	 * @param failureSignal
	 * @return
	 */
	public static float convertStringToFloat(String str, float failureSignal) {
		float returnValue = failureSignal;
		try {
			returnValue = Float.parseFloat(str);

		} catch (NumberFormatException ex) {
			;
		} catch (NullPointerException ex) {
			;
		}
		return returnValue;
	}


	/**
	 * Convert a request URI to the message tags supported in CertainBookStore
	 * 
	 * @param requestURI
	 * @return
	 */
	public static BankMessageTag convertURItoMessageTag(String requestURI) {

		try {
			BankMessageTag messageTag = BankMessageTag
					.valueOf(requestURI.substring(1).toUpperCase());
			return messageTag;
		} catch (IllegalArgumentException ex) {
			; // Enum type matching failed so non supported message
		} catch (NullPointerException ex) {
			; // RequestURI was empty
		}
		return null;
	}

	/**
	 * Serializes an object to an xml string
	 * 
	 * @param object
	 * @return
	 */
	public static String serializeObjectToXMLString(Object object) {
		String xmlString;
		XStream xmlStream = new XStream(new StaxDriver());
		xmlString = xmlStream.toXML(object);
		return xmlString;
	}

	/**
	 * De-serializes an xml string to object
	 * 
	 * @param xmlObject
	 * @return
	 */
	public static Object deserializeXMLStringToObject(String xmlObject) {
		Object dataObject = null;
		XStream xmlStream = new XStream(new StaxDriver());
		dataObject = xmlStream.fromXML(xmlObject);
		return dataObject;
	}


	/**
	 * Manages the sending of an exchange through the client, waits for the
	 * response and unpacks the response
	 * 
	 * @param client
	 * @param exchange
	 * @return 
	 * @throws Exception 
	 * @throws BookStoreException
	 */
	public static List<?> SendAndRecv(HttpClient client,
			ContentExchange exchange) throws BankClientException,
			InexistentAccountException, InexistentBranchException,  NegativeAmountException {
		int exchangeState;
		try {
			client.send(exchange);
		} catch(java.net.ConnectException ex) {
			throw new BankClientException(
					BankClientConstants.strERR_CLIENT_REQUEST_SENDING, ex);
		}
		catch (IOException ex) {
			throw new BankClientException(
					BankClientConstants.strERR_CLIENT_REQUEST_SENDING, ex);
		}
		catch(Throwable e){
			  System.out.println(e.getClass().getName());
		}
		
		try {
			exchangeState = exchange.waitForDone(); // block until the response
													// is available
		} catch (InterruptedException ex) {
			throw new BankClientException(
					BankClientConstants.strERR_CLIENT_REQUEST_SENDING, ex);
		}

		if (exchangeState == HttpExchange.STATUS_COMPLETED) {
			try {
				BankResponse bookStoreResponse = (BankResponse) BankUtility
						.deserializeXMLStringToObject(exchange
								.getResponseContent().trim());
				Exception ex = bookStoreResponse.getException();
				if (ex != null) {
					if (ex instanceof InexistentAccountException)
						throw (InexistentAccountException)ex;
					if (ex instanceof InexistentBranchException)
						throw (InexistentBranchException)ex;
					if (ex instanceof NegativeAmountException)
						throw (NegativeAmountException)ex;
					
					System.out.println("Uncatched exception in sendAndRecv(): " + ex.getClass().getName());
					return null;
				}else{
					return bookStoreResponse.getResult();
				}
				
			} catch (UnsupportedEncodingException ex) {
				throw new BankClientException(
						BankClientConstants.strERR_CLIENT_RESPONSE_DECODING,
						ex);
			}
		} else if (exchangeState == HttpExchange.STATUS_EXCEPTED) {
			throw new BankClientException(
					BankClientConstants.strERR_CLIENT_REQUEST_EXCEPTION);
		} else if (exchangeState == HttpExchange.STATUS_EXPIRED) {
			throw new BankClientException(
					BankClientConstants.strERR_CLIENT_REQUEST_TIMEOUT);
		} else {
			throw new BankClientException(
					BankClientConstants.strERR_CLIENT_UNKNOWN);
		}
	}

	/**
	 * Returns the message of the request as a string
	 * 
	 * @param request
	 * @return xml string
	 * @throws IOException
	 */
	public static String extractPOSTDataFromRequest(HttpServletRequest request)
			throws IOException {
		Reader reader = request.getReader();
		int len = request.getContentLength();

		// Request must be read into a char[] first
		char res[] = new char[len];
		reader.read(res);
		reader.close();
		return new String(res);
	}
}
