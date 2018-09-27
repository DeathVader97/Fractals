package de.felixperko.fractals.server.calculators.infrastructure;

import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.util.Position;

public interface SampleCalculator {
	public void calculate_samples(Chunk chunk, ProcessingStep processingStep);
	public Position getIterationForPosition(Position startPos, Position currentPos);
	
	public void setDescriptor(DataDescriptor dataDescriptor);
	public DataDescriptor getDescriptor();
	
	public void setRunIterations(int iterations);
	public long getRunIterations();
}
