package de.felixperko.fractals.util;

public class Message {
	
	String message;
	CategoryLogger category;
	String prefix;
	
	public Message(CategoryLogger category, String message){
		this.category = category;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Message setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public CategoryLogger getCategory() {
		return category;
	}
	
	public Message setCategory(CategoryLogger category) {
		this.category = category;
		return this;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public Message setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}
	
	public String getLogString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[").append(category.category);
		
		if (prefix != null){
			sb.append("/").append(prefix);
		}
		
		sb.append("] ").append(message);
		
		return sb.toString();
	}
	
	
}
