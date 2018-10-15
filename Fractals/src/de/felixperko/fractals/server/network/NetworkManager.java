package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.server.util.CategoryLogger;

public class NetworkManager {
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	static int ID_COUNTER = 0;
	
	Map<Integer, ClientConnection> clients = new HashMap<>();
	
	public ClientConnection createNewClient(ServerWriteThread writeThread) {
		SenderInfo info = new SenderInfo(ID_COUNTER++);
		ClientConnection clientInfo = new ClientConnection(info, writeThread);
		clients.put((Integer)clientInfo.getSenderInfo().clientId, clientInfo);
		log.log("new client connected. ID="+info.clientId);
		return clientInfo;
	}
	
	public ClientConnection getConnection(Integer clientId) {
		return clients.get(clientId);
	}
	
	public ClientConnection getConnection(SenderInfo senderInfo) {
		return getConnection(senderInfo.clientId);
	}
}
