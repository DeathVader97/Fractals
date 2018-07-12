package de.felixperko.fractals.network;

import java.io.Serializable;

public abstract class Message implements Serializable{
	
	private static final long serialVersionUID = 3353653767834804430L;

	SenderInfo sender;
	
	long sentTime;
	long latency;
	
	long lastMessageTime;
	
	public Message() {
		
	}
	
	public Message(SenderInfo sender, Message lastMessage) {
		this.sender = sender;
		this.sentTime = System.nanoTime();
		if (lastMessage != null)
			setLastMessageTime(lastMessage.getLatency());
	}
	
	protected abstract void process();
	
	public void received() {
		this.latency = sentTime-System.nanoTime();
		process();
	}
	
	public long getLatency() {
		return latency;
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

	public long getLastMessageTime() {
		return lastMessageTime;
	}

	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
}
