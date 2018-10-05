package de.felixperko.fractals.server.util;

import de.felixperko.fractals.server.data.DataDescriptor;

public class Position {
	
	public static BiOperation addNew = (p1,p2) -> new Position(p1.getX()+p2.getX(), p1.getY()+p2.getY());
	public static BiOperation subNew = (p1,p2) -> new Position(p1.getX()-p2.getX(), p1.getY()-p2.getY());
	public static BiOperation add = (p1,p2) -> {
		p1.setX(p1.getX()+p2.getX());
		p1.setY(p1.getY()+p2.getY());
		return p1;
	};
	public static BiOperation sub = (p1,p2) -> {
		p1.setX(p1.getX()-p2.getX());
		p1.setY(p1.getY()-p2.getY());
		return p1;
	};
	public static SingleOperation complexSquared = (p) -> { 
		double x = p.getX();
		double y = p.getY();
		return new Position((x*x - (y*y)), (x*y + (y*x)));
	};
	
	double x,y;
	

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
	
	public Position scaleBy(double scale, boolean newInstance){
		if (newInstance)
			return new Position(x*scale, y*scale);
		else {
			x *= scale;
			y *= scale;
			return this;
		}
	}
	
	public Position screenToPlane(DataDescriptor dataDescriptor) {
		return new Position(dataDescriptor.getStart_x()+x*dataDescriptor.getSpacing(), dataDescriptor.getStart_y()+y*dataDescriptor.getSpacing());
	}
	
	public Position planeToScreen(DataDescriptor dataDescriptor) {
		return new Position((x-dataDescriptor.getStart_x())/dataDescriptor.getSpacing(), (y-dataDescriptor.getStart_y())/dataDescriptor.getSpacing());
	}
	
	public Position performOperation(BiOperation operation, Position other) {
		return operation.operation(this, other);
	}
	
	public Position performOperation(SingleOperation operation) {
		return operation.operation(this);
	}
	
	public double lengthSq() {
		return x*x + y*y;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		return (x == other.x || y == other.y);
	}
	
	interface BiOperation {
		Position operation(Position p1, Position p2);
	}
	
	public interface SingleOperation{
		Position operation(Position p);
	}

	public void addX(double add) {
		this.x += add;
	}

	public void addY(double add) {
		this.y += add;
	}
	
	@Override
	public String toString() {
		return x+", "+y;
	}

	public Position add(Position other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}

	public Position sub(Position other) {
		this.x -= other.x;
		this.y -= other.y;
		return this;
	}

	public Position mult(Position other) {
		this.x *= other.x;
		this.y *= other.y;
		return this;
	}

	public Position div(Position other) {
		this.x /= other.x;
		this.y /= other.y;
		return this;
	}
	
	public Position addNew(Position other) {
		Position newPos = copy();
		newPos.x += other.x;
		newPos.y += other.y;
		return newPos;
	}

	public Position subNew(Position other) {
		Position newPos = copy();
		newPos.x -= other.x;
		newPos.y -= other.y;
		return newPos;
	}
	
	public Position multNew(Position other) {
		Position newPos = copy();
		newPos.x *= other.x;
		newPos.y *= other.y;
		return newPos;
	}
	
	public Position divNew(Position other) {
		Position newPos = copy();
		newPos.x /= other.x;
		newPos.y /= other.y;
		return newPos;
	}
	
	public Position mult(double factor) {
		this.x *= factor;
		this.y *= factor;
		return this;
	}
	
	public Position div(double factor) {
		this.x /= factor;
		this.y /= factor;
		return this;
	}
	
	public Position multNew(double factor) {
		Position newPos = copy();
		newPos.x *= factor;
		newPos.y *= factor;
		return newPos;
	}
	
	public Position divNew(double factor) {
		Position newPos = copy();
		newPos.x /= factor;
		newPos.y /= factor;
		return newPos;
	}
	
	public Position copy() {
		return new Position(x,y);
	}
}