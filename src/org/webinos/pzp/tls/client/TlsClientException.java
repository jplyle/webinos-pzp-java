/**
 * 
 */
package org.webinos.pzp.tls.client;

import org.webinos.pzp.tls.certificates.CertificateManagerException;

/**
 * @author johl
 *
 */
public class TlsClientException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8062840934278804334L;

	/**
	 * @param message
	 * @param cause
	 */
	public TlsClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public TlsClientException(String string) {
		super(string);
	}

	public TlsClientException(CertificateManagerException e) {
		super(e);
	}

}
