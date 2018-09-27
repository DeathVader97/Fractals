package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.server.util.CategoryLogger;

public class NetworkManager {
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	static int ID_COUNTER = 0;
	
	List<ClientInfo> clients = new ArrayList<>();
	
	public ClientInfo createNewClient(ServerWriteThread writeThread) {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientInfo clientInfo = new ClientInfo(info, writeThread);
		clients.add(clientInfo);
		log.log("new client connected. ID="+info.clientId);
		return clientInfo;
	}
}
