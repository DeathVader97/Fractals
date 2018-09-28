package de.felixperko.fractals.server.network;

import java.io.Serializable;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.util.CategoryLogger;

public abstract class Message implements Serializable{
	
	private static final long serialVersionUID = 3353653767834804430L;

	SenderInfo sender;
	
	long sentTime;
	long latency;
	
	long creationTime;
	long lastMessageTime;
	
	protected transient CategoryLogger log;
	
	public Message() {
		
	}
	
	public Message(SenderInfo sender, Message lastMessage) {
		this.sender = sender;
		this.creationTime = System.nanoTime();
		if (lastMessage != null)
			setLastMessageTime(lastMessage.getLatency());
	}
	
	protected abstract void process();
	
	public void received(CategoryLogger comLogger) {
		this.latency = System.nanoTime()-sentTime;
		setComLogger(comLogger);
		log.log("received "+getClass().getSimpleName()+" ("+getLatencyInMs(1)+"ms)");
		process();
	}
	
	private void setComLogger(CategoryLogger comLogger) {
		log = comLogger;
	}

	public long getLatency() {
		return latency;
	}
	
	public double getLatencyInMs(int precision) {
		return NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS * getLatency(), precision);
	}
	
	public SenderInfo getSender() {
		return sender;
	}

	public void setSender(SenderInfo sender) {
		this.sender = sender;
	}

	public long getSentTime() {
		return sentTime;
	}

	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}

	public void setSentTime() {
		setSentTime(System.nanoTime());
	}

	public long getLastMessageTime() {
		return lastMessageTime;
	}

	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
	
	protected void answer(Message message) {
		//TODO how to answer if server?
		FractalsMain.messenger.writeMessageToServer(message);
	}
}
