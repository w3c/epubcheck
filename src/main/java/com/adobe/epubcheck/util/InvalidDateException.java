package com.adobe.epubcheck.util;

/**
 * Invalid date exception.
 * Exception to signal an invalid date.
 */
public class InvalidDateException extends Exception {
	
	/**
	 * Creates an exception to signal an invalid date.
	 * @param message
	 */
	public InvalidDateException(String message) {
		super(message);
	}
	
	private static final long serialVersionUID = -8476118619257777307L;
}
