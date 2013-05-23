/**
 * 
 */
package org.webinos.pzp.websocket;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

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

	MessagePool messagePool;
	private Map<String, WebSocket> replyMap = new HashMap<>();
	
	public WebinosWebSocketServer(int port, MessagePool pool) {
		super ( new InetSocketAddress(port) );
		this.messagePool = pool;
		pool.addConsumer(this);
	}

	/* (non-Javadoc)
	 * @see org.java_websocket.server.WebSocketServer#onClose(org.java_websocket.WebSocket, int, java.lang.String, boolean)
	 */
	@Override
	public void onClose(WebSocket ws, int code, String reason, boolean remote) {
		System.out.println("Websocket closed, code: " + code + ", reason: " + reason + ", remote? " + remote );
	}

	/* (non-Javadoc)
	 * @see org.java_websocket.server.WebSocketServer#onError(org.java_websocket.WebSocket, java.lang.Exception)
	 */
	@Override
	public void onError(WebSocket ws, Exception ex) {
		System.out.println("Error: ");
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
			System.out.println("Received message: " + msg.toString());
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
		System.out.println("Websocket opened.");
	}

	@Override
	public boolean acceptsMessage(WebinosMessage msg) {
		//TODO: Check that the message is 'to' a registered application
		//TODO: Maybe check the type field, or the actual Type of the object
		return msg.getTo().startsWith("Linux Device/");
	}

	@Override
	public void consume(WebinosMessage msg) {
		// get the app ID portion of the 'to' field
		System.out.println("Send to web app: " + msg.toString());
		String appId = msg.getTo().substring("Linux Device/".length());
		WebSocket ws = replyMap.get(appId);
		
		
	}

}
