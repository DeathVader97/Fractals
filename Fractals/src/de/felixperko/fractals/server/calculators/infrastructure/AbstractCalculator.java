package de.felixperko.fractals.server.calculators.infrastructure;

import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.ChunkAccessType;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.steps.patternprovider.Pattern;
import de.felixperko.fractals.server.tasks.Task;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.Position;

public abstract class AbstractCalculator implements SampleCalculator{
	
	public static Position debugChunkPosition = null;
	public static int debugChunkIndex = 0;
	
	protected DataDescriptor descriptor;
	protected Task task;
	
	protected static float failRatioSampleCountFactor = 0.95f;
	
	CategoryLogger logDebug = CategoryLogger.INFO.createSubLogger("debug/calculator");
	
	public AbstractCalculator(DataDescriptor dataDescriptor, Task task) {
		this.descriptor = dataDescriptor;
		this.task = task;
	}
	
	public long run_iterations = 0;
	
	protected abstract void innerLoop();

	public DataDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(DataDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public void setRunIterations(int iterations) {
		run_iterations = iterations;
	}

	@Override
	public long getRunIterations() {
		return run_iterations;
	}
	
	protected int getRequiredSampleCount(Chunk chunk, int index) {
		boolean debug = isDebug(chunk, index);
		if (debug) {
			debug = false;
		}
		
		//get pattern
		Pattern pattern = chunk.getProcessingStepState().getNextProcessingStep().getPattern();
		int maxSize = pattern.getPositions().length;
		//skip nothing if custom
		if (!pattern.isGeneric())
			return maxSize;
		
		int finishedSamples = chunk.getSampleCount(index, ChunkAccessType.CALCULATION);
		double failRatio = chunk.getFailRatio(finishedSamples, index, ChunkAccessType.CALCULATION);
		if (failRatio < 1)
			failRatio = 0;
		double diffMultiplier = Math.min(chunk.getDiff(index, ChunkAccessType.CALCULATION)*5, 1);
		if (failRatio == 0 && diffMultiplier == 1)
			return maxSize;
		
		double failRatioMultiplier = (1- (failRatioSampleCountFactor*failRatio));
		
		int diff = (int) (Math.ceil(pattern.getSummedCount()*failRatioMultiplier*diffMultiplier) - finishedSamples);
		if (diff <= 0)
			return 0;
		return Math.min(diff, pattern.getPositions().length);
	}
	
	protected void logIfDebug(Chunk chunk, int index) {
		if (!isDebug(chunk, index))
			return;
		logDebug.log(chunk.getSampleCount(index, ChunkAccessType.CALCULATION)+": "
			+chunk.getAvgIterations(index, ChunkAccessType.CALCULATION)+" \u00B1 "
			+chunk.getStandardDeviation(index, ChunkAccessType.CALCULATION));
	}

	public static boolean isDebug(Chunk chunk, int index) {
		if (debugChunkPosition == null)
			return false;
		return chunk.getGridPosition().equals(debugChunkPosition) && index == debugChunkIndex;
	}
	
	public static void setDebug(Position chunkPos, int index) {
		debugChunkIndex = index;
		debugChunkPosition = chunkPos;
	}
	
	public static void resetDebug() {
		debugChunkPosition = null;
	}
}
