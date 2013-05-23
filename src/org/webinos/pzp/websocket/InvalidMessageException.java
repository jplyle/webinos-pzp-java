package org.webinos.pzp.websocket;

import com.google.gson.JsonSyntaxException;

public class InvalidMessageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4000825246001956431L;

	public InvalidMessageException(String string) {
		super(string);
	}

	public InvalidMessageException(String string, JsonSyntaxException e) {
		super(string, e);
	}

}
