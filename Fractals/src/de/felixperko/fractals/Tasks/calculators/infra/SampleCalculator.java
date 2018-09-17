package de.felixperko.fractals.Tasks.calculators.infra;

import de.felixperko.fractals.Tasks.steps.ProcessingStep;
import de.felixperko.fractals.Tasks.steps.patternprovider.Pattern;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.util.Position;

public interface SampleCalculator {
	public void calculate_samples(int[] sampleIndices, int[] currentIterations, int maxIterations,
			double[] currentpos_real, double[] currentpos_imag, int[] results);
	public void calculate_samples(Chunk chunk, ProcessingStep processingStep);
	public Position getIterationForPosition(Position startPos, Position currentPos);
	
	public void setDescriptor(DataDescriptor dataDescriptor);
	public DataDescriptor getDescriptor();
	
	public void setRunIterations(int iterations);
	public long getRunIterations();
}
