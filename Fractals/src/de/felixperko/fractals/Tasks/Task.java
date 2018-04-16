package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;

public class Task {
	
	final static int STATE_NOT_ASSIGNED = 0,
			STATE_ASSINGED = 1,
			STATE_FINISHED = 2;

	int state = 0;
	
	int startSample, endSample;
	
	int maxIterations;
	
	double[] currentpos_real;
	double[] currentpos_imag;
	int[] currentIterations;
	int[] results;
	
	long start_time;
	long end_time;
	long end_sample_count;
	int samplesPerMs;
	
	int jobId;
	
	public Task(int startSample, int endSample, int maxIterations, int[] currentIterations, double[] currentpos_real,
			double[] currentpos_imag, int[] results) {
		this.startSample = startSample;
		this.endSample = endSample;
		this.maxIterations = maxIterations;
		this.currentIterations = currentIterations;
		this.currentpos_real = currentpos_real;
		this.currentpos_imag = currentpos_imag;
		this.results = results;
	}
	
	public Task(int startSample, int endSample, int maxIterations, int jobId) {
		this.startSample = startSample;
		this.endSample = endSample;
		this.maxIterations = maxIterations;

		this.results = new int[endSample-startSample];
		this.currentIterations = new int[endSample-startSample];
		this.currentpos_real = new double[endSample-startSample];
		this.currentpos_imag = new double[endSample-startSample];
		
		this.jobId = jobId;
	}
	
	public void run(DataDescriptor dataDescriptor) {
		this.start_time = System.nanoTime();
		SampleCalculator sc = new SampleCalculator(dataDescriptor);
		sc.calculate_samples(startSample, endSample, currentIterations, maxIterations, currentpos_real, currentpos_imag, results);
		this.end_time = System.nanoTime();
		this.end_sample_count = sc.iterations_total;
		this.samplesPerMs = (int)((double)end_sample_count/((end_time-start_time)/1000000.));
	}
}
