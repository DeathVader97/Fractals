package de.felixperko.fractals.server.tasks;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.util.Position;

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
			chunk.setReadyToDraw(false);
			if (!chunk.arraysInstantiated())
				chunk.instantiateArrays();
			ProcessingStep processingStep = chunk.getProcessingStepState().getNextProcessingStep();
			sampleCalculator.calculate_samples(chunk, processingStep);
//			System.out.println("patternstate = "+(state+1)+"/"+patternProvider.getMaxState()+" ("+chunk.sampleCount[1]+")");
			chunk.getProcessingStepState().increment();
			chunk.calculateDiff();
			chunk.setReadyToCalculate(true);
			
			//set max iterations according to probing if configured
			if (processingStep.isProbeStep()) {
				float max = 0;
				float avg = 0;
				int index;
				int lastIndex = 0;
				int finishedCount = 0;
				int totalCount = processingStep.getActiveCount();
				while ((index = processingStep.getActiveIndices().nextSetBit(lastIndex+1)) > -1) {
					float avgIt = chunk.getAvgIterations(index);
					if (avgIt > 0) {
						finishedCount++;
						avg += avgIt;
						if (avgIt > max)
							max = avgIt;
					}
					lastIndex = index;
				}
//				if (finishedCount <= totalCount/2){
//					chunk.setMaxIterations(0);
//				} else {
//					avg /= finishedCount;
//					chunk.setMaxIterations(10000);
//				}
			}
			
			try {
				//update surrounding chunk positions
				for (Position p : chunk.getNeighbourPositions()) {
					Chunk c = chunk.getGrid().getChunkOrNull(p);
					if (c != null && c.imageCalculated && c != chunk && c.getProcessingStepState().getStateNumber() > state && c.arraysInstantiated()) {
						c.setReadyToDraw(false);
						c.calculateDiff();
						((GridRenderer)FractalsMain.mainWindow.getMainRenderer()).getCalcThread().addChunk(c);
					}
				}
			} catch (Exception e){
				throw e;
			}
			
//			chunk.setStepPriorityMultiplier(chunk.getStepPriorityMultiplier()*2);
		} catch (Exception e) {
			if (!chunk.isDisposed()) {
				System.err.println(e.getClass().getSimpleName()+" at non-disposed chunk task calculation.");
				e.printStackTrace();
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