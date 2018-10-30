package de.felixperko.fractals.server.network.messages;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.network.ClientRemoteConnection;
import de.felixperko.fractals.server.network.Message;
import de.felixperko.fractals.server.network.SenderInfo;

public class ConnectedMessage extends Message {

	private static final long serialVersionUID = -1809347006971064792L;
	
	SenderInfo clientInfo;

	public ConnectedMessage(ClientRemoteConnection clientInfo) {
		this.clientInfo = clientInfo.getSenderInfo();
	}

	@Override
	protected void process() {
		FractalsMain.clientStateHolder.stateClientInfo.setValue(clientInfo);
		log.log("Got client info!");
		answer(new ConnectedAckMessage());
	}

}
