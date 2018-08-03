package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.util.Position;

public class Chunk {
	
	final int chunk_size;
	
	double[] iterationsSum;
	double[] iterationsSumSq;
	float[] diff;
	int[] sampleCount;
	
	Position offset;
	DataDescriptor dataDescriptor;
	
	public Chunk(int chunk_size, DataDescriptor dataDescriptor, Position offset) {
		this.chunk_size = chunk_size;
		this.dataDescriptor = dataDescriptor;
		this.offset = offset;
	}
	
	public void instantiateArrays() {
		iterationsSum = new double[chunk_size*chunk_size];
		iterationsSumSq = new double[chunk_size*chunk_size];
		diff = new float[chunk_size*chunk_size];
		sampleCount = new int[chunk_size*chunk_size];
	}
	
	public boolean arraysInstantiated() {
		return iterationsSum != null;
	}
	
	public int getIndex(int relX, int relY) {
		return relX*chunk_size + relY;
	}
	
	public double getX(int iX) {
		return offset.getX() + iX*dataDescriptor.getDelta_x();
	}
	
	public double getY(int iY) {
		return offset.getY() + iY*dataDescriptor.getDelta_y();
	}
}
