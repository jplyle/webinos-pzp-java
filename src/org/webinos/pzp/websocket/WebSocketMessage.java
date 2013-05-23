/**
 * 
 */
package org.webinos.pzp.websocket;

import org.webinos.pzp.messaging.MessageOrigin;
import org.webinos.pzp.messaging.WebinosMessage;

/**
 * @author johl
 *
 */
public class WebSocketMessage implements MessageOrigin {

	private String from;
	private String originalJson;
	private String type;
	private WebinosMessage webinosMessage;
	
	public String getOriginalJson() {
		return originalJson;
	}
	public void setOriginalJson(String originalJson) {
		this.originalJson = originalJson;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public boolean isValid() {
		return 	(getType() != null) &&
				(!getType().isEmpty());
	}
	public void setWebinosMessage(WebinosMessage webinosMessage) {
		this.webinosMessage = webinosMessage;
	}
	
	public WebinosMessage getWebinosMessage() {
		return this.webinosMessage;
	}
	
	public String toString() {
		String res = "WebSocketMessage [" + getType() + "]";
		if (getFrom() != null) {
			res += "\nFrom: [" + getFrom() + "]";
		}
		if (getWebinosMessage() != null) {
			res += "\nBody: [" + getWebinosMessage().toString() + "]";
		}
		if (getOriginalJson() != null) { 
			res += "\nJSON: [" + getOriginalJson().toString() + "]";
		}
		
		return res;
	}
	public String getFrom() {
		return this.from;
	}
	void setFrom(String from) {
		this.from = "ws://" + from;
	}
}
