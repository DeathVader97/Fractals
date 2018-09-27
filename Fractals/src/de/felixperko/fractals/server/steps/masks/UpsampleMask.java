package de.felixperko.fractals.server.steps.masks;

import de.felixperko.fractals.server.data.DataDescriptor;

public class UpsampleMask implements IndexMask {
	
	DataDescriptor dataDescriptor;
	int factor;
	
	public UpsampleMask(DataDescriptor dataDescriptor, int factor) {
		this.dataDescriptor = dataDescriptor;
		this.factor = factor;
	}

	@Override
	public double getWeight() {
		return 1;
	}

	@Override
	public int getIndex(int i) {
		int chunkSize = dataDescriptor.getChunkSize();
		int x = (i / chunkSize);
		int y = (i % chunkSize);
		x -= x % factor;
		y -= y % factor;
		return x * chunkSize + y;
	}

}
