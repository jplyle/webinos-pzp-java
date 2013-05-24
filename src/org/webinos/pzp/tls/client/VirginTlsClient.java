package org.webinos.pzp.tls.client;

import java.security.cert.X509Certificate;
import java.util.Properties;

import org.webinos.pzp.messaging.MessagePool;


public class VirginTlsClient extends WebinosTlsClient {

	public VirginTlsClient(Properties config) {
		super(config);
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void disconnect() {
		/* Do nothing */
	}

	@Override
	public WebinosTlsClient newHubAddress(String address, int port) {
		WebinosTlsClient replacement = new ConfiguredTlsClient(getConfiguration());
		return replacement.newHubAddress(address, port);
	}

	@Override
	public WebinosTlsClient joinZone(X509Certificate signedCertificate) throws TlsClientException {
		throw new TlsClientException("No PZH details");
	}

	@Override
	public ConnexionConfigState getConfigurationState() {
		return ConnexionConfigState.VIRGIN;
	}
	
}
