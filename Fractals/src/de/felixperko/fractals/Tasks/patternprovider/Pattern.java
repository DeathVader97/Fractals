package de.felixperko.fractals.Tasks.patternprovider;

import de.felixperko.fractals.util.Position;

public class Pattern {
	
	Position[] positions;
	boolean generic;
	int summedCount;
	
	public Pattern(boolean generic, Position... positions) {
		this.generic = generic;
		this.positions = positions;
	}

	public Position[] getPositions() {
		return positions;
	}

	public void setPositions(Position[] positions) {
		this.positions = positions;
	}

	public boolean isGeneric() {
		return generic;
	}

	public Pattern setGeneric(boolean generic) {
		this.generic = generic;
		return this;
	}

	public int getSummedCount() {
		return summedCount;
	}

	public void setSummedCount(int summedCount) {
		this.summedCount = summedCount;
	}
	
	@Override
	public String toString() {
		return positions.length+" - "+generic+" - "+positions.toString();
	}
}
