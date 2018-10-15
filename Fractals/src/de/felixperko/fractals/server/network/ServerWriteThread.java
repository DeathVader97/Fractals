package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.net.Socket;

import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.network.messages.ConnectedMessage;
import de.felixperko.fractals.server.network.messages.ReachableRequestMessage;
import de.felixperko.fractals.server.util.CategoryLogger;

public class ServerWriteThread extends WriteThread {
	
	final static CategoryLogger superLog = new CategoryLogger("com/server", Color.MAGENTA);
	
	ClientConnection clientConnection;
	
	long reachableRequestInterval = (long) (1/NumberUtil.NS_TO_S);
	long lastReachableTime;
	
	public ServerWriteThread(Socket socket) {
		super(socket);
		lastReachableTime = System.nanoTime();
	}
	
	@Override
	public void writeMessage(Message msg) {
		//TODO seems to be wrong (messages from server don't have a SenderInfo, right?)
//		if (clientConnection != null) {
//			if (msg.getSender() == null)
//				msg.setSender(clientConnection.getSenderInfo());
//			else if (!msg.getSender().equals(clientConnection.getSenderInfo())){
//				throw new IllegalStateException("Wrong thread to send message to client");
//			}
//		}
		super.writeMessage(msg);
	}
	
	@Override
	protected void tick() {
		if (System.nanoTime() - lastReachableTime > reachableRequestInterval) {
			lastReachableTime = System.nanoTime();
			writeMessage(new ReachableRequestMessage());
		}
	}

	public void setClientConnection(ClientConnection clientConnection) {
		this.clientConnection = clientConnection;
		super.setConnection(clientConnection);
		setListenLogger(superLog.createSubLogger(clientConnection.getSenderInfo().getClientId()+"/in"));
		this.log = superLog.createSubLogger(clientConnection.getSenderInfo().getClientId()+"/out");
		writeMessage(new ConnectedMessage(clientConnection));
	}
}
