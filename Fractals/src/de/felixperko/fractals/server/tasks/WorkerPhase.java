package de.felixperko.fractals.server.tasks;

import java.awt.Color;

import org.eclipse.swt.widgets.Display;

public class WorkerPhase {
	
	String name;
	Color color;
	org.eclipse.swt.graphics.Color swtColor;
	Color oldColor;
	
	public WorkerPhase(String name, Color color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}
	
	protected void setColor(Color color) {
		this.color = color;
	}

	public org.eclipse.swt.graphics.Color getSwtColor() {
		if (swtColor != null && !color.equals(oldColor)) {
			swtColor.dispose();
			swtColor = null;
		}
		if (swtColor == null) {
			Display display = Display.getCurrent();
			swtColor = new org.eclipse.swt.graphics.Color(display, color.getRed(), color.getGreen(), color.getBlue());
			oldColor = color;
		}
		return swtColor;
	}
}
