package org.webinos.pzp;
import org.webinos.pzp.messaging.MessagePool;
import org.webinos.pzp.websocket.WebinosWebSocketServer;

/**
 * 
 */

/**
 * @author johl
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessagePool pool = new MessagePool();
		
		WebinosWebSocketServer server = new WebinosWebSocketServer(8080, pool);
		
		server.start();
	}

}
