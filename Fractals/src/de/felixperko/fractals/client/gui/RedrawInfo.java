package de.felixperko.fractals.client.gui;

import de.felixperko.fractals.server.util.Position;

public class RedrawInfo implements Comparable<RedrawInfo>{
	
	int x,y,w,h;
	double priority;

	public RedrawInfo(Position screenOffset, Position screenChunkDimensions, double priority) {
		x = (int) Math.round(screenOffset.getX());
		y = (int) Math.round(screenOffset.getY());
		w = (int) Math.round(screenChunkDimensions.getX());
		h = (int) Math.round(screenChunkDimensions.getY());
		this.priority = priority;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}

	@Override
	public int compareTo(RedrawInfo o) {
		return Double.compare(priority, o.priority);
	}
}