package de.felixperko.fractals.util;

import de.felixperko.fractals.DataDescriptor;

public class Position {
	
	public static BiOperation addNew = (p1,p2) -> new Position(p1.getX()+p2.getX(), p1.getY()+p2.getY());
	public static BiOperation add = (p1,p2) -> {
		p1.setX(p1.getX()+p2.getX());
		p1.setY(p1.getX()+p2.getY());
		return p1;
	};
	public static SingleOperation complexSquared = (p) -> { 
		double x = p.getX();
		double y = p.getY();
		return new Position((x*x - (y*y)), (x*y + (y*x)));
	};
	
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
	
	public Position screenToPlane(DataDescriptor dataDescriptor) {
		return new Position(dataDescriptor.getStart_x()+x*dataDescriptor.getSpacing(), dataDescriptor.getStart_y()+y*dataDescriptor.getSpacing());
	}
	
	public Position planeToScreen(DataDescriptor dataDescriptor) {
		return new Position((x-dataDescriptor.getStart_x())/dataDescriptor.getSpacing(), (y-dataDescriptor.getStart_y())/dataDescriptor.getSpacing());
	}
	
//	public Position complexSquared() {
//		double new_real = (x*x - (y*y));
//		double new_imag = (x*y + (y*x));
//		return new Position(new_real,new_imag);
//	}
	
	public Position performOperation(BiOperation operation, Position other) {
		return operation.operation(this, other);
	}
	
	public Position performOperation(SingleOperation operation) {
		return operation.operation(this);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Position && ((Position)other).getX() == x && ((Position)other).getY() == y;
	}
	
	interface BiOperation {
		Position operation(Position p1, Position p2);
	}
	
	interface SingleOperation{
		Position operation(Position p);
	}
}
