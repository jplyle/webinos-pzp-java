/**
 * 
 */
package org.webinos.pzp.websocket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.webinos.pzp.messaging.MessageConsumer;
import org.webinos.pzp.messaging.MessagePool;
import org.webinos.pzp.messaging.WebinosMessage;

/**
 * @author johl
 *
 */
public class WebinosWebSocketServer extends WebSocketServer implements MessageConsumer {

	private final static Logger LOGGER = Logger.getLogger(WebSocketServer.class.getName()); 
	private MessagePool messagePool;
	private String localName; 
	private Map<String, WebSocket> replyMap = new HashMap<>();
	
	public WebinosWebSocketServer(int port, String localName, MessagePool pool) {
		super ( new InetSocketAddress(port) );
		this.messagePool = pool;
		this.localName = localName;
		pool.addConsumer(this);
	}

	/* (non-Javadoc)
	 * @see org.java_websocket.server.WebSocketServer#onClose(org.java_websocket.WebSocket, int, java.lang.String, boolean)
	 */
	@Override
	public void onClose(WebSocket ws, int code, String reason, boolean remote) {
		LOGGER.log(Level.INFO, "Websocket closed, code: " + code + ", reason: " + reason + ", remote? " + remote );
	}

	/* (non-Javadoc)
	 * @see org.java_websocket.server.WebSocketServer#onError(org.java_websocket.WebSocket, java.lang.Exception)
	 */
	@Override
	public void onError(WebSocket ws, Exception ex) {
		LOGGER.log(Level.INFO, "Error: ");
		ex.printStackTrace();
	}

	/* (non-Javadoc)
	 * @see org.java_websocket.server.WebSocketServer#onMessage(org.java_websocket.WebSocket, java.lang.String)
	 */
	@Override
	public void onMessage(WebSocket ws, String msgString) {
		try {
			WebinosMessage msg = WebSocketMessageFactory.fromString(ws, msgString);
			replyMap.put( msg.getFrom(), ws );
			LOGGER.log(Level.INFO, "Received message: " + msg.toString());
			messagePool.addMessage(msg);
		} catch (InvalidMessageException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.java_websocket.server.WebSocketServer#onOpen(org.java_websocket.WebSocket, org.java_websocket.handshake.ClientHandshake)
	 */
	@Override
	public void onOpen(WebSocket ws, ClientHandshake arg1) {
		//TODO: Only allow connections from localhost.
		LOGGER.log(Level.INFO, "Websocket opened.");
	}

	@Override
	public boolean acceptsMessage(WebinosMessage msg) {
		//TODO: Check that the message is 'to' a registered application
		//TODO: Maybe check the type field, or the actual Type of the object
		return msg.getTo().startsWith(localName) && 
				msg.getTo().split("/").length == 2;
				
	}

	@Override
	public void consume(WebinosMessage msg) {
		// get the app ID portion of the 'to' field
		LOGGER.log(Level.INFO, "Send to web app: " + msg.toString());
		String appId = msg.getTo().split("/")[1];
		WebSocket ws = replyMap.get(appId);
		ws.send(msg.toJsonString());
	}

}
