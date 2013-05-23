package org.webinos.pzp.tls.client;

import java.security.cert.X509Certificate;
import java.util.Properties;

import org.webinos.pzp.tls.certificates.CertificateManager;
import org.webinos.pzp.tls.certificates.CertificateManagerException;

public class ConfiguredTlsClient extends WebinosTlsClient {

	private CertificateManager certMan; 
	
	public ConfiguredTlsClient(Properties config) {
		super(config);
		
	}

	@Override
	public boolean isConfigured() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void disconnect() {
		
	}

	@Override
	public WebinosTlsClient newHubAddress(String address, int port) {
		getConfiguration().setProperty("tls.address", address);
		getConfiguration().setProperty("tls.port", Integer.toString(port));
		return this;
	}

	@Override
	public void connect() throws TlsClientException {
		throw new TlsClientException("Unable to connect: has not joined personal zone");
	}

	@Override
	public WebinosTlsClient joinZone(X509Certificate signedCertificate)
			throws TlsClientException {
		return new InZoneTlsClient(getConfiguration()).joinZone(signedCertificate);
	}
	
	@Override
	public ConnexionConfigState getConfigurationState() {
		return ConnexionConfigState.CONFIGURED;
	}


	/**
	 * Create the initial keys we need.
	 * 
	 * @throws TlsClientException
	 * @throws CertificateManagerException
	 */
	private void bootstrap()
			throws TlsClientException, CertificateManagerException {
		this.certMan = CertificateManager.getInstance(getConfiguration());
		if (!certMan.isPopulated()) {
			certMan.populate();
			certMan.save();
		}
	}
	
	@Override
	public String getCSR() throws TlsClientException {
		try {
			bootstrap();
			return this.certMan.getPzpMasterCSR();
		} catch (CertificateManagerException e) {
			e.printStackTrace();
			throw new TlsClientException(e);
		}
	}
	
}
