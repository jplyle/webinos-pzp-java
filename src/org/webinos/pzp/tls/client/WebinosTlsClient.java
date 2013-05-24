/**
 * 
 */
package org.webinos.pzp.tls.client;

import java.security.cert.X509Certificate;
import java.util.Properties;

import org.webinos.pzp.messaging.MessageConsumer;
import org.webinos.pzp.messaging.MessagePool;


/**
 * @author johl
 *
 */
public abstract class WebinosTlsClient {

	private Properties config;

	public abstract ConnexionConfigState getConfigurationState();
	
	protected Properties getConfiguration() {
		return this.config;
	}
	
	public abstract boolean isConnected();
	
	public void setProperties(Properties props) {
		this.config = props;
	}

	public WebinosTlsClient(Properties config) {
		this.config = config;
	}
	
	public abstract boolean isConfigured();

	public boolean connect() throws TlsClientException {
		return false;
	}
	
	public abstract void disconnect();

	public abstract WebinosTlsClient newHubAddress(String address, int port);

	public WebinosTlsClient joinZone(X509Certificate signedCertificate) throws TlsClientException {
		throw new TlsClientException("Unable to join zone");
	}

	public String getCSR() throws TlsClientException {
		throw new TlsClientException("Unable to create CSR");
	}

	public MessageConsumer getConsumer(MessagePool pool) {
		return null;
	}
}
