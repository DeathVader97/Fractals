package de.felixperko.fractals.Tasks;

import java.awt.Color;

public class WorkerPhase {
	
	String name;
	Color color;
	
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
}
