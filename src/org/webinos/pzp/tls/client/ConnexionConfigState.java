package org.webinos.pzp.tls.client;

public enum ConnexionConfigState {
	VIRGIN,		// This PZP has no settings for a PZH - no address nor port 
	CONFIGURED,	// This PZP has a PZH address and port, but has not been authenticated by the PZH 
				// or given any certificates
	IN_ZONE		// This PZP has certificates and is fully configured, but is not connected 
}
