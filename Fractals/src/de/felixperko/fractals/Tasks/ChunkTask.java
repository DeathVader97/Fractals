package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;

public class ChunkTask extends Task {
	
	Chunk chunk;
	DataDescriptor dataDescriptor;
	
	public ChunkTask(Chunk chunk, DataDescriptor dataDescriptor) {
		super(dataDescriptor);
		this.chunk = chunk;
		this.dataDescriptor = dataDescriptor;
	}

	@Override
	protected void calculate() {
		int depth = dataDescriptor.getMaxIterations();
		sampleCalculator.calculate_samples(chunk, depth);
		chunk.calculatePixels();
		chunk.setStepPriorityMultiplier(10000);
	}
	
	public Chunk getChunk() {
		return chunk;
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}

	public DataDescriptor getDataDescriptor() {
		return dataDescriptor;
	}

	public void setDataDescriptor(DataDescriptor dataDescriptor) {
		this.dataDescriptor = dataDescriptor;
	}
}
