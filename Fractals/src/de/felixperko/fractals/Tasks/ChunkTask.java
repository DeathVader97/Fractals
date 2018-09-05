package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.util.Position;

public class ChunkTask extends Task {
	
//	static Position[] samplepattern = new Position[] {
//			new Position(0,0)
//			,new Position(0.25, 0.25)
//			,new Position(-0.25, -0.25)
//			,new Position(-0.25, 0.25)
//			,new Position(0.25, -0.25)
//			,new Position(+0.25 + 0.25*0.25, +0.25 + 0.25*0.25)
//			,new Position(-0.25 - 0.25*0.25, -0.25 - 0.25*0.25)
//			,new Position(-0.25 - 0.25*0.25, +0.25 + 0.25*0.25)
//			,new Position(+0.25 + 0.25*0.25, -0.25 - 0.25*0.25)
//			};
	
	static int sampleDim = 4;
	static Position[] samplepattern = new Position[sampleDim*sampleDim];
	static {
		double dimD = Math.sqrt(samplepattern.length);
		int dim = (int) dimD;
		if (dimD > dim)
			throw new IllegalStateException("the squareroot of samplepattern.length has to be integer.");
		
		double start = -0.5;
		double totalDelta = 1;
		double stepDelta = totalDelta/dim;
		int i = 0;
		for (int x = 0 ; x < dim ; x++){
			for (int y = 0 ; y < dim ; y++){
				samplepattern[i] = new Position(start+stepDelta*x, start+stepDelta*y);
				i++;
			}
		}
	}

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
			throw new NullPointerException();
		return chunk.getPriority();
	}
}
