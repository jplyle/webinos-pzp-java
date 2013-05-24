package org.webinos.pzp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webinos.pzp.messaging.MessagePool;
import org.webinos.pzp.tls.client.TlsClientException;
import org.webinos.pzp.tls.client.TlsState;
import org.webinos.pzp.websocket.WebinosWebSocketServer;

/**
 * 
 */

/**
 * @author johl
 * 
 */
public class Main {

	private final static Logger LOGGER = Logger.getLogger(Main.class.getName()); 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties webinosProperties = getProperties(args);

		MessagePool pool = new MessagePool();
		
		WebinosWebSocketServer server = new WebinosWebSocketServer(
				Integer.parseInt(webinosProperties
						.getProperty("websocket.port")),
				webinosProperties.getProperty("pzp.name"), pool);

		
		
		
		server.start();
	}
	
	private static TlsState loadTlsServer(Properties webinosProperties, MessagePool pool) throws TlsClientException {
		TlsState tlsState = TlsState.getInstance();
		tlsState.configure(webinosProperties, pool);
		
		return tlsState;
	}

	private static Properties getProperties(String[] args) {
		Properties props = new Properties(getDefaultProperties());
		try {
			File propsFile;
			if (args != null && args.length >= 1) {
				propsFile = new File(args[0]);
			} else {
				propsFile = new File("webinos.properties");
			}
			if (propsFile.exists()) {
				LOGGER.log(Level.INFO, "Using properties file : " + propsFile.getAbsolutePath());
				props.load(new FileInputStream(propsFile));
			} else {
				LOGGER.log(Level.INFO, "Could not find a useful properties file");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	private static Properties getDefaultProperties() {
		Properties props = new Properties();
		//TODO: Define some defaults
		return props;
	}

}
