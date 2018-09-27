package de.felixperko.fractals.server.steps.masks;

public interface IndexMask {
	
	public double getWeight();
	
	public int getIndex(int i);
}
