package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.util.Position;

public class ChunkTask extends Task {
	
	static Position[] samplepattern = new Position[] {
			new Position(0,0)
//			,new Position(0.25, 0.25)
//			,new Position(-0.25, -0.25)
//			,new Position(-0.25, 0.25)
//			,new Position(0.25, -0.25)
//			,new Position(+0.25 + 0.25*0.25, +0.25 + 0.25*0.25)
//			,new Position(-0.25 - 0.25*0.25, -0.25 - 0.25*0.25)
//			,new Position(-0.25 - 0.25*0.25, +0.25 + 0.25*0.25)
//			,new Position(+0.25 + 0.25*0.25, -0.25 - 0.25*0.25)
			};
	
//	static Position[] samplepattern = new Position[100];
//	static {
//		for (int i = 0 ; i < samplepattern.length ; i++)
//			samplepattern[i] = new Position(Math.random()-0.5, Math.random()-0.5);
//	}
	
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
		try {
			if (!chunk.arraysInstantiated())
				chunk.instantiateArrays();
			sampleCalculator.calculate_samples(chunk, depth, samplepattern);
			chunk.calculateDiff();
			chunk.calculatePixels();
			chunk.setStepPriorityMultiplier(10000);
		} catch (NullPointerException e) {
			if (!chunk.isDisposed()) {
				System.err.println("NPE at non-disposed chunk task calculation.");
				throw e;
			}
		}
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

	public double getPriority() {
		if (chunk == null)
			return Double.MAX_VALUE;
		return chunk.getPriority();
	}
}
