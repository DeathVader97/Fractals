package de.felixperko.fractals.util;

public class CategoryLogger {
	
	String category;
	
	public CategoryLogger(String category) {
		this.category = category;
	}
	
	public void log(String msg) {
		Logger.log(category, msg);
	}
}
