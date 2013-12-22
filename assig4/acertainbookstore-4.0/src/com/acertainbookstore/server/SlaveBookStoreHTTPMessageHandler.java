/**
 * 
 */
package com.acertainbookstore.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.MasterCertainBookStore;
import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.SlaveCertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResponse;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * 
 * SlaveBookStoreHTTPMessageHandler implements the message handler class which
 * is invoked to handle messages received by the slave book store HTTP server It
 * decodes the HTTP message and invokes the SlaveCertainBookStore API
 * 
 */
public class SlaveBookStoreHTTPMessageHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		BookStoreMessageTag messageTag;
		String numBooksString = null;
		int numBooks = -1;
		String requestURI;
		String xml;

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		requestURI = request.getRequestURI();

		// Need to do request multi-plexing
		if (!BookStoreUtility.isEmpty(requestURI)
				&& requestURI.toLowerCase().startsWith("/stock")) {
			messageTag = BookStoreUtility.convertURItoMessageTag(requestURI
					.substring(6)); // the request is from store
			// manager, more
			// sophisticated security
			// features could be added
			// here
		} else {
			messageTag = BookStoreUtility.convertURItoMessageTag(requestURI);
		}
		// the RequestURI before the switch
		if (messageTag == null) {
			System.out.println("Unknown message tag");
		} else {

			// Write requests should not be handled
			switch (messageTag) {
			
			case REPLICATE:
				xml = BookStoreUtility
					.extractPOSTDataFromRequest(request);

				ReplicationRequest repRequest = (ReplicationRequest) BookStoreUtility
						.deserializeXMLStringToObject(xml);
				
				switch(repRequest.getMessageType()){
				case ADDBOOKS:
					
					BookStoreResponse bookStoreresponse = new BookStoreResponse();
					try {
						bookStoreresponse.setResult(MasterCertainBookStore
								.getInstance().addBooks((Set<StockBook>) repRequest.getDataSet()));
					} catch (BookStoreException ex) {
						bookStoreresponse.setException(ex);
					}

					response.getWriter().println(
							BookStoreUtility
									.serializeObjectToXMLString(bookStoreresponse));
					break;
				case ADDCOPIES:
					bookStoreresponse = new BookStoreResponse();
					try {
						bookStoreresponse.setResult(MasterCertainBookStore
								.getInstance().addCopies((Set<BookCopy>) repRequest.getDataSet()));
					} catch (BookStoreException ex) {
						bookStoreresponse.setException(ex);
					}
					response.getWriter().println(
							BookStoreUtility
									.serializeObjectToXMLString(bookStoreresponse));
					break;
				case BUYBOOKS:
					// Make the purchase
					bookStoreresponse = new BookStoreResponse();
					try {
						bookStoreresponse.setResult(MasterCertainBookStore
								.getInstance().buyBooks((Set<BookCopy>) repRequest.getDataSet()));
					} catch (BookStoreException ex) {
						bookStoreresponse.setException(ex);
					}
					response.getWriter().println(
							BookStoreUtility
									.serializeObjectToXMLString(bookStoreresponse));
					break;
				case UPDATEEDITORPICKS:
					bookStoreresponse = new BookStoreResponse();

					try {
						bookStoreresponse.setResult(MasterCertainBookStore
								.getInstance().updateEditorPicks(
										(Set<BookEditorPick>) repRequest.getDataSet()));
					} catch (BookStoreException ex) {
						bookStoreresponse.setException(ex);
					}
					response.getWriter().println(
							BookStoreUtility
									.serializeObjectToXMLString(bookStoreresponse));
					break;
				default:
					break;
				}
				break;

			case LISTBOOKS:
				BookStoreResponse bookStoreresponse = new BookStoreResponse();
				try {
					bookStoreresponse.setResult(SlaveCertainBookStore
							.getInstance().getBooks());
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				response.getWriter().println(
						BookStoreUtility
								.serializeObjectToXMLString(bookStoreresponse));
				break;

			case GETBOOKS:
				xml = BookStoreUtility
						.extractPOSTDataFromRequest(request);
				Set<Integer> isbnSet = (Set<Integer>) BookStoreUtility
						.deserializeXMLStringToObject(xml);

				bookStoreresponse = new BookStoreResponse();
				try {
					bookStoreresponse.setResult(SlaveCertainBookStore
							.getInstance().getBooks(isbnSet));
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				response.getWriter().println(
						BookStoreUtility
								.serializeObjectToXMLString(bookStoreresponse));
				break;

			case EDITORPICKS:
				numBooksString = URLDecoder
						.decode(request
								.getParameter(BookStoreConstants.BOOK_NUM_PARAM),
								"UTF-8");
				bookStoreresponse = new BookStoreResponse();
				try {
					numBooks = BookStoreUtility
							.convertStringToInt(numBooksString);
					bookStoreresponse.setResult(SlaveCertainBookStore
							.getInstance().getEditorPicks(numBooks));
				} catch (BookStoreException ex) {
					bookStoreresponse.setException(ex);
				}
				response.getWriter().println(
						BookStoreUtility
								.serializeObjectToXMLString(bookStoreresponse));
				break;

			default:
				System.out.println("Unhandled message tag");
				break;
			}
		}
		// Mark the request as handled so that the HTTP response can be sent
		baseRequest.setHandled(true);

	}
}
