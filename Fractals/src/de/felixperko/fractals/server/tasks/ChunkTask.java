package de.felixperko.fractals.server.tasks;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.ChunkAccessType;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.util.Position;

public class ChunkTask extends Task {
	
	Chunk chunk;
	DataDescriptor dataDescriptor;
	volatile boolean running = false;
	
	public ChunkTask(Chunk chunk, DataDescriptor dataDescriptor, int taskManagerId) {
		super(dataDescriptor, taskManagerId);
		this.chunk = chunk;
		this.dataDescriptor = dataDescriptor;
	}

	@Override
	protected void calculate() {
		if (running == true)
			throw new IllegalStateException("The Task is already running!");
		running = true;
		int depth = dataDescriptor.getMaxIterations();
		try {
			
			long t1 = System.nanoTime();
			while(!chunk.isReadyToCalculate()) {
				long deltaT = System.nanoTime()-t1;
				if (deltaT > 0.01/NumberUtil.NS_TO_S)
					throw new IllegalStateException("waited too long ("+NumberUtil.getRoundedDouble(deltaT*NumberUtil.NS_TO_S,5)+")");
				Thread.sleep(1);
			}
			long deltaT = System.nanoTime()-t1;
			if (deltaT > 0.001/NumberUtil.NS_TO_S)
				chunk.addStateInfo("waited to get ready for calculation for "+NumberUtil.getRoundedDouble(deltaT*NumberUtil.NS_TO_S,5)+"s");
			
			
			chunk.setReadyToCalculate(false);
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
					float avgIt = chunk.getAvgIterations(index, ChunkAccessType.CALCULATION);
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
						FractalsMain.threadManager.getCalcPixelThread().addChunk(c);
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
		running = false;
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
