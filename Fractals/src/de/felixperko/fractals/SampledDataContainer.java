package de.felixperko.fractals;

public class SampledDataContainer {
	
	boolean done = false;
	double[][] samples;
	double[][] absSq;
	double[][] fluctuance;
	float[][] notFinishedFraction;
	
	DataContainer container;
	DataDescriptor descriptor;
	
	public SampledDataContainer(DataContainer source, int qualityScaling) {
		this.container = source;
		this.descriptor = source.getDescriptor();
		
		boolean valid = downsample(qualityScaling);
		if (!valid)
			return;
		if (samples != null)
			postprocess();
		
		done = true;
	}

	private boolean downsample(int qualityScaling){
		if (qualityScaling < 0 || (Math.log(qualityScaling)/Math.log(2)) % 1 != 0) //scalefactor is not a natural power of 2
			return false;
		
		int dim_sampled_x = descriptor.getDim_sampled_x();
		int dim_sampled_y = descriptor.getDim_sampled_y();
		int newDimX = dim_sampled_x / qualityScaling;
		int newDimY = dim_sampled_y / qualityScaling;
		
		samples = new double[newDimX][newDimY];
		absSq = new double[newDimX][newDimY];
		notFinishedFraction = new float[newDimX][newDimY];

		
		for (int x = 0 ; x < newDimX ; x++){
			nextSample:
			for (int y = 0 ; y < newDimY ; y++){
				int startX2 = x*qualityScaling;
				int startY2 = y*qualityScaling;
				double summedValue = 0;
				double summedAbsSq = 0;
				int notFinished = 0;
				for (int x2 = startX2 ; x2 < startX2+qualityScaling ; x2++){
					for (int y2 = startY2 ; y2 < startY2+qualityScaling ; y2++){
						int index = y2*dim_sampled_x + x2;
						int sample = container.samples[index];
						if (sample < 0){
							notFinished++;
							continue;
						}
						summedValue += sample;
						double real = container.currentSamplePos_real[index];
						double imag = container.currentSamplePos_imag[index];
						summedAbsSq += real*real+imag*imag;
					}
				}
				double weight = 1d/(qualityScaling*qualityScaling - notFinished);
				samples[x][y] = summedValue*weight;
				absSq[x][y] = summedAbsSq*weight;
				notFinishedFraction[x][y] = notFinished/(qualityScaling*qualityScaling);
			}
		}
		return true;
	}
	
	private void postprocess() {
		
		
		int dim_x = samples.length;
		int dim_y = samples[0].length;
		double[] buff_samples = new double[9];

		fluctuance = new double[dim_x][dim_y];
		
		for (int x = 0 ; x < samples.length ; x++) {
			for (int y = 0 ; y < samples[x].length ; y++) {
				double avg = 0;
				int min_x = (x == 0) ? 0 : x-1;
				int min_y = (y == 0) ? 0 : y-1;
				int max_x = (x == dim_x-1) ? x : x+1;
				int max_y = (y == dim_y-1) ? y : y+1;
				int n = (1+max_x-min_x)*(1+max_y-min_y);
				int i = 0;
				for (int x2 = min_x ; x2 <= max_x ; x2++) {
					for (int y2 = min_y ; y2 <= max_y ; y2++) {
						if (notFinishedFraction[x2][y2] > 0) {
							n--;
							continue;
						}
						double sample = samples[x2][y2];
						sample = Math.sqrt(sample+1-Math.log(Math.log(absSq[x2][y2])*0.5/Math.log(2))/Math.log(2));
						buff_samples[i] = sample;
						avg += sample;
						i++;
					}
				}
				avg /= n;
				double val = 0;
				for (int j = 0 ; j < n ; j++) {
					val += Math.abs(buff_samples[j]-avg);
				}
				fluctuance[x][y] = val;
//				System.out.print(n+" ");
			}
		}
	}
	
	public boolean isDone() {
		return done;
	}
}
