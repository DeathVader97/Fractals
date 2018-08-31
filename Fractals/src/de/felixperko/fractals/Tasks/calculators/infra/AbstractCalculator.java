package de.felixperko.fractals.Tasks.calculators.infra;

import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.data.DataDescriptor;

public abstract class AbstractCalculator implements SampleCalculator{
	protected DataDescriptor descriptor;
	protected Task task;
	
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
}
