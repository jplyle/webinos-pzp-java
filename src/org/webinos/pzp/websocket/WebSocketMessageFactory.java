/**
 * 
 */
package org.webinos.pzp.websocket;

import org.java_websocket.WebSocket;
import org.webinos.pzp.messaging.WebinosMessage;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author johl
 *
 */
public class WebSocketMessageFactory {
	@SuppressWarnings("rawtypes")
	private static Class[] messageClasses = {
		WebinosMessage.class
	};
	

	@SuppressWarnings("unchecked")
	public static WebinosMessage fromString(WebSocket ws, String message) throws InvalidMessageException {
		Gson gson = new Gson();
		try {
			WebSocketMessage msg  = gson.fromJson(message, WebSocketMessage.class);
			msg.setFrom(ws.getRemoteSocketAddress().toString());
			msg.setOriginalJson(message);
			if (!msg.isValid()) {
				throw new InvalidMessageException("Invalid JSON message");
			} else {
				System.out.println("Found message: " + msg);
			}
			for (@SuppressWarnings("rawtypes") Class c : WebSocketMessageFactory.messageClasses) {
				WebinosMessage webinosMessage = (WebinosMessage) gson.fromJson(message, c);
				webinosMessage.setOrigin(msg);
				if(webinosMessage.isValid()) {
					return webinosMessage;
				} else {
					throw new InvalidMessageException("Invalid JSON message body");
				}
			}
		} catch (JsonSyntaxException e) {
			throw new InvalidMessageException("Error parsing JSON from WebSocket", e);
		}
		throw new InvalidMessageException("Could not find matching class");
	}

}
