package de.felixperko.fractals.Tasks.calculators.infra;

import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.Tasks.patternprovider.Pattern;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Position;

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
		Pattern pattern = chunk.getPatternState().getPattern();
		int maxSize = pattern.getPositions().length;
		if (!pattern.isGeneric())
			return maxSize;
		double failRatio = chunk.getFailRatio(index);
		if (failRatio == 0)
			return maxSize;
		double finishedSamples = chunk.sampleCount[index];
		int diff = (int) (Math.ceil(pattern.getSummedCount()*(failRatioSampleCountFactor*failRatio)) - finishedSamples);
		return Math.min(diff, pattern.getPositions().length);
	}
	
	protected void logIfDebug(Chunk chunk, int index) {
		if (!isDebug(chunk, index))
			return;
		logDebug.log(chunk.sampleCount[index]+": "+chunk.currentPosX[index]+"/"+chunk.currentPosY[index]+" -> "+chunk.getAvgIterations(index));
	}

	protected static boolean isDebug(Chunk chunk, int index) {
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
