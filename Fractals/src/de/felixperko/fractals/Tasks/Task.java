package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;

public class Task {
	
	final static int STATE_NOT_ASSIGNED = 0,
			STATE_ASSINGED = 1,
			STATE_FINISHED = 2;

	int state = 0;
	
	int startSample, endSample;
	
	private int maxIterations;
	
	double[] currentpos_real;
	double[] currentpos_imag;
	int[] currentIterations;
	int[] results;
	
	long start_time;
	long end_time;
	long end_sample_count;
	int samplesPerMs;
	
	SampleCalculator sampleCalculator = new SampleCalculator(null, this);
	
	int jobId;
	
	public ArrayList<Integer> changedIndices = new ArrayList<>();

	private int previousMaxIterations = -1;
	
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
		
		this.jobId = jobId;
		instantiateArrays();
	}
	
	public void instantiateArrays() {
		this.results = new int[endSample-startSample];
		this.currentIterations = new int[endSample-startSample];
		this.currentpos_real = new double[endSample-startSample];
		this.currentpos_imag = new double[endSample-startSample];
	}
	
	public void run(DataDescriptor dataDescriptor) {
		this.start_time = System.nanoTime();
		changedIndices.clear();
		sampleCalculator.descriptor = dataDescriptor;
		sampleCalculator.run_iterations = 0;
		sampleCalculator.calculate_samples(startSample, endSample, currentIterations, maxIterations, currentpos_real, currentpos_imag, results);
		this.end_time = System.nanoTime();
		this.end_sample_count = sampleCalculator.run_iterations;
		this.samplesPerMs = (int)((double)end_sample_count/((end_time-start_time)/1000000.));
	}
	
	public Task successor(int depth) {
		return new Task(startSample, endSample, depth, currentIterations, currentpos_real, currentpos_imag, results);
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.previousMaxIterations = this.maxIterations;
		this.maxIterations = maxIterations;
	}

	public int getPreviousMaxIterations() {
		return previousMaxIterations;
	}
}
