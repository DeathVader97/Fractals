package de.felixperko.fractals.server.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class CategoryLogger {
	
	public static CategoryLogger INFO = new CategoryLogger("info", new java.awt.Color(0, 127, 0));
	public static CategoryLogger WARNING = new CategoryLogger("warning", java.awt.Color.ORANGE);
	public static CategoryLogger WARNING_SERIOUS = new CategoryLogger("warning/serious", java.awt.Color.ORANGE);
	public static CategoryLogger ERROR = new CategoryLogger("error", java.awt.Color.RED);
	
	String category;
	Color color;
	
	public CategoryLogger(String category, java.awt.Color color) {
		this.category = category;
		this.color = new Color(Display.getCurrent(), new RGB(color.getRed(), color.getGreen(), color.getBlue()));
	}
	
	public CategoryLogger(String category, Color color) {
		this.category = category;
		this.color = color;
	}
	
	public void log(String msg) {
		Logger.log(new Message(this, msg));
	}
	
	public void log(String prefix, String msg) {
		Logger.log(new Message(this, msg).setPrefix(prefix));
	}

	public Color getColor() {
		return color;
	}

	public String getName() {
		return category;
	}
	
	public CategoryLogger createSubLogger(String subCategory) {
		return new CategoryLogger(category+"/"+subCategory, color);
	}
	
	public CategoryLogger createSubLogger(String subCategory, java.awt.Color color) {
		return new CategoryLogger(category+"/"+subCategory, color);
	}
	
	public CategoryLogger createSubLogger(String subCategory, Color color) {
		return new CategoryLogger(category+"/"+subCategory, color);
	}
}
