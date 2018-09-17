package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.Tasks.steps.ProcessingStep;
import de.felixperko.fractals.Tasks.steps.patternprovider.BasicPatternProvider;
import de.felixperko.fractals.Tasks.steps.patternprovider.Pattern;
import de.felixperko.fractals.Tasks.steps.patternprovider.PatternProvider;
import de.felixperko.fractals.Tasks.steps.patternprovider.SinglePatternProvider;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.ProcessingStepState;
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
	
//	public static PatternProvider patternProvider = new SinglePatternProvider(10);
	
//	static int sampleDim = 5;
//	static Position[] samplepattern = new Position[sampleDim*sampleDim];
//	static {
//		double dimD = Math.sqrt(samplepattern.length);
//		int dim = (int) dimD;
//		if (dimD > dim)
//			throw new IllegalStateException("the squareroot of samplepattern.length has to be integer.");
//		
//		double start = -0.5;
//		double totalDelta = 1;
//		double stepDelta = totalDelta/dim;
//		int i = 0;
//		for (int x = 0 ; x < dim ; x++){
//			for (int y = 0 ; y < dim ; y++){
//				samplepattern[i] = new Position(start+stepDelta*x, start+stepDelta*y);
//				i++;
//			}
//		}
//	}

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
			ProcessingStep processingStep = chunk.getProcessingStepState().increment().getProcessingStep();
			chunk.setReadyToDraw(false);
			sampleCalculator.calculate_samples(chunk, processingStep);
//			System.out.println("patternstate = "+(state+1)+"/"+patternProvider.getMaxState()+" ("+chunk.sampleCount[1]+")");
			chunk.calculateDiff();
			chunk.calculatePixels();
			
			//update surrounding chunk positions
//			for (Position p : chunk.getNeighbourPositions()) {
//				Chunk c = chunk.getGrid().getChunkOrNull(p);
//				if (c != null && c.imageCalculated && c != chunk) {
//					c.setReadyToDraw(false);
//					c.calculateDiff();
//					c.calculatePixels();
//					c.setReadyToDraw(true);
//				}
//			}
			
			chunk.setReadyToDraw(true);
//			chunk.setStepPriorityMultiplier(chunk.getStepPriorityMultiplier()*2);
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
			return 1000000000;
		return chunk.getPriority();
	}
}
