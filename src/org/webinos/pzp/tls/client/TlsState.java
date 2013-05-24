package org.webinos.pzp.tls.client;

import java.security.cert.X509Certificate;
import java.util.Properties;

import org.webinos.pzp.messaging.MessagePool;


/**
 * @author johl
 * 
 * The expected use of this class is to create an instance, 
 * call "getConfigurationState" then do one of the following:
 * 
 * (1) Configure the connection with a PZH address + port
 * (2) Join the zone (getting a certificate)
 * (3) Connect to the PZH
 *
 */
public class TlsState {

	private WebinosTlsClient client = null;

	private MessagePool pool;

	private static TlsState instance = null;

	private TlsState() {
		/* singleton */
	}
	
	
	/**
	 * Configures a virgin client with a certificate
	 * @throws TlsClientException 
	 */
	public void joinZone(X509Certificate signedCertificate) throws TlsClientException {
		client = client.joinZone(signedCertificate);
	}
	
	public void joinZone(X509Certificate signedCertificate, String address, int port) throws TlsClientException {
		client = client.newHubAddress(address, port).joinZone(signedCertificate);
	}
	
	/**
	 * @param address
	 * @param port
	 * 
	 * Set the address of the PZH
	 * 
	 */
	public void setHubAddress(String address, int port) {
		WebinosTlsClient newClient = client.newHubAddress(address, port);
		client = newClient;
	}
	
	public ConnexionConfigState getConfigurationState() {
		return client.getConfigurationState();
	}
	
	public void connect() throws TlsClientException {
		if ( client.connect() ) {
			pool.addConsumer(client.getConsumer(pool));
		}
	}
	
	
	private void loadClient(Properties config, MessagePool pool) throws TlsClientException {
		if (!validateConfig(config)) {
			throw new TlsClientException(
					"Invalid configuration for TLS connection");
		}
		this.pool = pool;
		WebinosTlsClient client = new InZoneTlsClient(config);
		if (!client.isConfigured()) {
			client = new ConfiguredTlsClient(config);
			if (!client.isConfigured()) {
				client = new VirginTlsClient(config);
			}
		}
		client.setProperties(config);
		this.client = client;
	}

	public void configure(Properties properties, MessagePool pool)
			throws TlsClientException {
		instance.loadClient(properties, pool);
	}

	public static TlsState getInstance() {
		if (instance == null) {
			instance = new TlsState();
		}
		return instance;
	}

	private static boolean validateConfig(Properties config) {
		return config.containsKey("keystore.file")
				&& config.containsKey("keystore.password");
	}

}
