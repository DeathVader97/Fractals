package de.felixperko.fractals.server.network;

public class SenderInfo {
	
	String name;
	int clientId;
	
	public SenderInfo(int clientId) {
		this.name = "Client_"+clientId;
		this.clientId = clientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
}
