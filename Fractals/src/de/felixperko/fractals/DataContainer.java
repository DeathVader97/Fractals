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

	public double[][] downsample(int qualityScaling){
		if (qualityScaling < 0 || (Math.log(qualityScaling) / Math.log(2)) %1 != 0) //scalefactor is not a natural power of 2
			return null;
		int newDimX = descriptor.dim_sampled_x / qualityScaling;
		int newDimY = descriptor.dim_sampled_y / qualityScaling;
		double[][] newSamples = new double[newDimX][newDimY];
		for (int x = 0 ; x < newDimX ; x++){
			for (int y = 0 ; y < newDimY ; y++){
				int startX2 = x*qualityScaling;
				int startY2 = y*qualityScaling;
				double summedValue = 0;
				for (int x2 = startX2 ; x2 < startX2+qualityScaling ; x2++){
					for (int y2 = startY2 ; y2 < startY2+qualityScaling ; y2++){
						int index = y2*descriptor.dim_sampled_x + x2;
						summedValue += samples[index];
					}
				}
				newSamples[x][y] = summedValue/(qualityScaling*qualityScaling);
			}
		}
		return newSamples;
	}
}
