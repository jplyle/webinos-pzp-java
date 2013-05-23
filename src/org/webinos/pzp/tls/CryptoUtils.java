/**
 * 
 */
package org.webinos.pzp.tls;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author johl
 * 
 */
public class CryptoUtils {




	public static X509TrustManager getX509TrustManager(KeyStore keyStore)
			throws NoSuchAlgorithmException, NoSuchProviderException,
			KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance("PKIX", "SunJSSE");
		trustManagerFactory.init(keyStore);

		X509TrustManager x509TrustManager = null;
		for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager) {
				x509TrustManager = (X509TrustManager) trustManager;
				break;
			}
		}
		if (x509TrustManager == null) {
			throw new NullPointerException();
		}
		
		
		return x509TrustManager;
	}

	public static X509KeyManager getKeyManagerFactory(KeyStore keyStore,
			String password) throws NoSuchAlgorithmException,
			NoSuchProviderException, UnrecoverableKeyException,
			KeyStoreException {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
				"SunX509", "SunJSSE");
		keyManagerFactory.init(keyStore, password.toCharArray());

		X509KeyManager x509KeyManager = null;
		for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
			if (keyManager instanceof X509KeyManager) {
				x509KeyManager = (X509KeyManager) keyManager;
				break;
			}
		}
		if (x509KeyManager == null) {
			throw new NullPointerException();
		}
		return x509KeyManager;
	}
}
