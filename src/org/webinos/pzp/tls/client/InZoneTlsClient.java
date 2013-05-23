package org.webinos.pzp.tls.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.bouncycastle.crypto.tls.TlsClient;
import org.bouncycastle.crypto.tls.TlsProtocolHandler;

public class InZoneTlsClient extends WebinosTlsClient {

	public InZoneTlsClient(Properties config) {
		super(config);
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WebinosTlsClient newHubAddress(String address, int port) {
		//TODO: Remove old settings.
		return new ConfiguredTlsClient(getConfiguration());
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
		TlsProtocolHandler handler = new TlsProtocolHandler(null,null);
		TlsClient client;
		
	}

	@Override
	public WebinosTlsClient joinZone(X509Certificate signedCertificate)
			throws TlsClientException {
		//TODO
		return this;
		
	}
	
	@Override
	public ConnexionConfigState getConfigurationState() {
		return ConnexionConfigState.IN_ZONE;
	}

	private SSLContext getSSLContext(KeyManager keyManager,
			TrustManager trustManager) throws NoSuchAlgorithmException,
			KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[] { keyManager },
				new TrustManager[] { trustManager }, null);
		return sslContext;
	}

	private SSLSocket getClientSocket(SSLContext sslContext)
			throws UnknownHostException, IOException {
		SSLSocketFactory socketFactory = sslContext.getSocketFactory();
		SSLSocket socket = (SSLSocket) socketFactory.createSocket(
				getConfiguration().getProperty("tls.server"), Integer.parseInt( getConfiguration().getProperty("tls.port") ));

		socket.setEnabledProtocols(new String[] { "TLSv1.2" });
		return socket;
	}

}
