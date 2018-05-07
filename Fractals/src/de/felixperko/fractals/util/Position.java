package de.felixperko.fractals.util;

public class Position {
	double x,y;
	int displayPrecision;
	String separation = "x";

	/**
	 * creates a Position with the values (0,0)
	 */
	public Position() {
	}
	
	public Position(double x, double y){
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getDisplayPrecision() {
		return displayPrecision;
	}

	public void setDisplayPrecision(int displayPrecision) {
		this.displayPrecision = displayPrecision;
	}

	public String getSeparation() {
		return separation;
	}

	public void setSeparation(String separation) {
		this.separation = separation;
	}
}
