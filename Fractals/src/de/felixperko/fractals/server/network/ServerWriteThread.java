package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.net.Socket;

import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.network.messages.ConnectedMessage;
import de.felixperko.fractals.server.network.messages.ReachableRequestMessage;
import de.felixperko.fractals.server.util.CategoryLogger;

public class ServerWriteThread extends WriteThread {
	
	final static CategoryLogger superLog = new CategoryLogger("com/server", Color.MAGENTA);
	
	ClientInfo clientInfo;
	
	long reachableRequestInterval = (long) (1/NumberUtil.NS_TO_S);
	long lastReachableTime;
	
	public ServerWriteThread(Socket socket) {
		super(socket);
		lastReachableTime = System.nanoTime();
	}
	
	@Override
	public void writeMessage(Message msg) {
		if (clientInfo != null) {
			if (msg.getSender() == null)
				msg.setSender(clientInfo.getSenderInfo());
			else if (!msg.getSender().equals(clientInfo.getSenderInfo())){
				throw new IllegalStateException("Wrong thread to send message to client");
			}
		}
		super.writeMessage(msg);
	}
	
	@Override
	protected void tick() {
		if (System.nanoTime() - lastReachableTime > reachableRequestInterval) {
			lastReachableTime = System.nanoTime();
			writeMessage(new ReachableRequestMessage());
		}
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
		setListenLogger(superLog.createSubLogger(clientInfo.getSenderInfo().getClientId()+"/in"));
		this.log = superLog.createSubLogger(clientInfo.getSenderInfo().getClientId()+"/out");
		writeMessage(new ConnectedMessage(clientInfo));
	}
}
