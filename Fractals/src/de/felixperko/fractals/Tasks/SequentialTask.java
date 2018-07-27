package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;

public class SequentialTask extends Task{
	
	int startSample, endSample;
	int[] indices;
	
	public SequentialTask(int startSample, int endSample, int maxIterations, int[] currentIterations, double[] currentpos_real,
			double[] currentpos_imag, int[] results, DataDescriptor dataDescriptor) {
		super(maxIterations, currentIterations, currentpos_real, currentpos_imag, results, dataDescriptor);
		this.startSample = startSample;
		this.endSample = endSample;
	}
	
	public SequentialTask(int startSample, int endSample, int maxIterations, int jobId, DataDescriptor dataDescriptor) {
		super();
		this.startSample = startSample;
		this.endSample = endSample;
		this.maxIterations = maxIterations;
		this.jobId = jobId;
		int size = endSample-startSample;
		this.results = new int[size];
		this.indices = new int[size];
		this.sampleCalculator = dataDescriptor.getCalculatorFactory().createCalculator(this);
		instantiateArrays(size);
	}

	@Override
	protected void calculate() {
		for (int i = 0 ; i < indices.length ; i++)
			indices[i] = startSample+i;
		sampleCalculator.calculate_samples(indices, currentIterations, maxIterations, currentpos_real, currentpos_imag, results);
	}
}
