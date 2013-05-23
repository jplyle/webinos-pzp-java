package org.webinos.pzp.tls.certificates;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.junit.Test;
import org.webinos.pzp.tls.certificates.CertificateManager;
import org.webinos.pzp.tls.certificates.CertificateManagerException;

public class CertificateManagerTest {

	@Test
	public void testPopulate() throws IOException, CertificateManagerException {
		CertificateManager certMan = getPopulatedKeyStore();
		assertTrue(certMan.isPopulated());
	}
	
	private CertificateManager getPopulatedKeyStore() throws IOException, CertificateManagerException {
		Properties props = new Properties();
		File f = File.createTempFile("keystore", ".keystore");
		props.setProperty("keystore.file", f.getAbsolutePath());
		props.setProperty("keystore.password", "secret");
		
		CertificateManager certMan = CertificateManager.getInstance(props);
		if (!certMan.isPopulated()) {
			certMan.populate();
		}
		return certMan;
	}

	@Test
	public void testLoad() throws CertificateManagerException, IOException {
		CertificateManager certMan = getPopulatedKeyStore();
		certMan.save();
		File keyStoreFile = certMan.getKeyStoreFile();
		certMan = CertificateManager.getInstance(keyStoreFile, "secret");
		certMan.load();
		assertTrue(certMan.isPopulated());
	}
	
	@Test
	public void testGetCSR() throws IOException, CertificateManagerException {
		CertificateManager certMan = getPopulatedKeyStore();
		String pemCsr = certMan.getPzpMasterCSR();
		assertNotNull(pemCsr);
		assertTrue(pemCsr.startsWith("-----BEGIN CERTIFICATE REQUEST"));
		System.out.println(pemCsr);
	}
	
	@Test
	public void testGetCerts() throws IOException, CertificateManagerException {
		String example = "-----BEGIN CERTIFICATE-----\n" + 
				"MIICdzCCAeCgAwIBAgJkr3Jn+lKsefs83mt3aGRpc8tx9MOnSDtD6JJNExYx1yZq\n" + 
				"fWQC4mdCvN2qpr4yw8NZaFxoBlAoHJdIbIb8TaMGdg/EeRaGQpTRcKeMAr2hya+A\n" + 
				"UOIFiCMkWXzLHnDAOeXKjgYh/TANBgkqhkiG9w0BAQUFADBQMRMwEQYDVQQDDApX\n" + 
				"ZWJpbm9zUFpQMQwwCgYDVQQLDANXUDQxEDAOBgNVBAoMB1dlYmlub3MxGTAXBgNV\n" + 
				"BAYTEExvbmRvbiBTPU1YIEM9VUswHhcNMTMwNTIxMDkwNDQ1WhcNMTQwNTIyMDkw\n" + 
				"NDQ1WjBQMRMwEQYDVQQDDApXZWJpbm9zUFpQMQwwCgYDVQQLDANXUDQxEDAOBgNV\n" + 
				"BAoMB1dlYmlub3MxGTAXBgNVBAYTEExvbmRvbiBTPU1YIEM9VUswgZ8wDQYJKoZI\n" + 
				"hvcNAQEBBQADgY0AMIGJAoGBAKy6bzoE4Q3cUVb+n5yOtaoJNFOcXscjQ3/HgLK0\n" + 
				"0KADWc2xu7UApFpaA14mXssPQfAmk2Ul3OjQPOPAjYFRxcaHNMFsYhzCMpZOLuBB\n" + 
				"wrRmDCcIOugFmsdDlW/l0daNfJ6x6EgeL8kq3iRcpKZrL3aPcvbag25WJ6EhUeJQ\n" + 
				"eMJFAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAUdwM767HTrOkYhDW9E3sNMnRLVZq\n" + 
				"JLnKIM5RCgJtJ8KKUh0BTEj+n8lh8+EcEFYUrjAqkN3Ui9CoPw33nKYxbuka4HUt\n" + 
				"yQWsic3aXII6VvOu9MMXbZZme+kqQVhP/IHrkhSjtxKi1lF0ofO4T3PNq6S0p9Po\n" + 
				"aWOYsTG149x0Tgo=\n" + 
				"-----END CERTIFICATE-----\n";
		
		CertificateManager certMan = getPopulatedKeyStore();
		X509Certificate cert = certMan.getCertificateFromString(example);
		System.out.println(cert.toString());
		assertNotNull(cert);
	}
	
}
