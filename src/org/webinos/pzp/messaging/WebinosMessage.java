package org.webinos.pzp.messaging;

import com.google.gson.Gson;

public class WebinosMessage {

	private String to = null;
	private String from = null; 
	private String type = null;
	private String resp_to = null;
	
	private MessageOrigin origin;
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public boolean isValid() {
		return this.to != null && !this.to.isEmpty() && this.from != null && !this.from.isEmpty();
	}

	public String getTo() {
		return to;
	}

	public void setOrigin(MessageOrigin msg) {
		this.origin = msg;
	}

	public String getRespondTo() {
		return resp_to;
	}

	public void setRespondTo(String resp_to) {
		this.resp_to = resp_to;
	}
	
	public String toJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this);	
	}

}
