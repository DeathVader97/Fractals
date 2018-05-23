package de.felixperko.fractals;

public class DataContainer {
	
	private DataDescriptor descriptor;
	
	public double[] currentSamplePos_real;
	public double[] currentSamplePos_imag;
	public int[] currentSampleIterations;
	
	public int[] samples;
	int[][] values;
	
	
	public DataContainer(DataDescriptor descriptor) {
		
		this.descriptor = descriptor;
		
		int samples_x = descriptor.getDim_sampled_x();
		int samples_y = descriptor.getDim_sampled_y();
		samples = new int[samples_x*samples_y];
		values = new int[descriptor.getDim_goal_x()][descriptor.getDim_goal_y()];
		
		currentSamplePos_real = new double[samples_x*samples_y];
		currentSamplePos_imag = new double[samples_x*samples_y];
		currentSampleIterations = new int[samples_x*samples_y];
	}

	public DataDescriptor getDescriptor() {
		return descriptor;
	}
}
