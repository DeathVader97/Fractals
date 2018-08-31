package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.data.DataDescriptor;

public class SequentialTask extends Task{
	
	int startSample, endSample;
	int[] indices;
	
	double[] currentpos_real;
	double[] currentpos_imag;
	int[] currentIterations;
	int[] results;
	
	public SequentialTask(int startSample, int endSample, int maxIterations, int[] currentIterations, double[] currentpos_real,
			double[] currentpos_imag, int[] results, DataDescriptor dataDescriptor) {
		super(maxIterations, dataDescriptor);
		this.startSample = startSample;
		this.endSample = endSample;
		this.currentIterations = currentIterations;
		this.currentpos_real = currentpos_real;
		this.currentpos_imag = currentpos_imag;
		this.results = results;
	}
	
	public SequentialTask(int startSample, int endSample, int maxIterations, int jobId, DataDescriptor dataDescriptor) {
		super(dataDescriptor);
		this.startSample = startSample;
		this.endSample = endSample;
		this.maxIterations = maxIterations;
		this.jobId = jobId;
		int size = endSample-startSample;
		instantiateArrays(size);
	}

	@Override
	protected void calculate() {
		for (int i = 0 ; i < indices.length ; i++)
			indices[i] = startSample+i;
		sampleCalculator.calculate_samples(indices, currentIterations, maxIterations, currentpos_real, currentpos_imag, results);
	}
	
	protected void instantiateArrays(int size) {
		this.results = new int[size];
		this.indices = new int[size];
		this.currentIterations = new int[size];
		this.currentpos_real = new double[size];
		this.currentpos_imag = new double[size];
	}
}
