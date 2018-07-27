package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Tasks.calculators.MandelbrotCalculator;
import de.felixperko.fractals.Tasks.calculators.infra.AbstractCalculator;
import de.felixperko.fractals.Tasks.calculators.infra.SampleCalculator;

public abstract class Task {
	
	final static int STATE_NOT_ASSIGNED = 0,
			STATE_ASSINGED = 1,
			STATE_FINISHED = 2;

	int state = 0;
	
	protected int maxIterations;
	
	double[] currentpos_real;
	double[] currentpos_imag;
	int[] currentIterations;
	int[] results;
	
	long start_time;
	long end_time;
	long end_sample_count;
	private int samplesPerMs;
	
	SampleCalculator sampleCalculator;
	
	int jobId;
	
	public ArrayList<Integer> changedIndices = new ArrayList<>();

	private int previousMaxIterations = -1;
	
	public Task(int maxIterations, int[] currentIterations, double[] currentpos_real, double[] currentpos_imag, int[] results, DataDescriptor dataDescriptor) {
		this.maxIterations = maxIterations;
		this.currentIterations = currentIterations;
		this.currentpos_real = currentpos_real;
		this.currentpos_imag = currentpos_imag;
		this.results = results;
		this.sampleCalculator = dataDescriptor.getCalculatorFactory().createCalculator(this);
	}
	
//	public Task(int maxIterations, int jobId) {
//	}

	public Task() {
		// TODO wip task generalization
	}

	public void run(DataDescriptor dataDescriptor) {
		this.start_time = System.nanoTime();
		changedIndices.clear();
		sampleCalculator.setDescriptor(dataDescriptor);
		sampleCalculator.setRunIterations(0);
		calculate();
		this.end_time = System.nanoTime();
		this.end_sample_count = sampleCalculator.getRunIterations();
		samplesPerMs = ((int)((double)end_sample_count/((end_time-start_time)/1000000.)));
	}
	
	protected void instantiateArrays(int size) {
		this.currentIterations = new int[size];
		this.currentpos_real = new double[size];
		this.currentpos_imag = new double[size];
	}
	
	protected abstract void calculate();

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

	public int getJobId() {
		return jobId;
	}

	public int getSamplesPerMs() {
		return samplesPerMs;
	}

	public long getEnd_Sample_Count() {
		return end_sample_count;
	}
}
