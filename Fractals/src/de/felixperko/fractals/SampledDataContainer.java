package de.felixperko.fractals;

import java.util.Arrays;

import de.felixperko.fractals.Tasks.perf.PerfInstance;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public class SampledDataContainer {
	
	boolean done = false;
	double[][] samples;
//	double[][] absSq;
	double[][] fluctuance;
	float[][] notFinishedFraction;
	int[] fluctuanceDistribution = new int[100];
	
	DataContainer container;
	DataDescriptor descriptor;
	
	public SampledDataContainer(DataContainer source, int qualityScaling, PerfInstance parentPerfInstance) {
		this.container = source;
		this.descriptor = source.getDescriptor();
		PerfInstance downsamplePerf = PerfInstance.createNewSubInstanceAndBegin("downsample", parentPerfInstance);
		boolean valid = downsample(qualityScaling);
		downsamplePerf.end();
		if (!valid)
			return;
		PerfInstance postProcessPerf = PerfInstance.createNewSubInstanceAndBegin("postprocess", parentPerfInstance);
		if (samples != null)
			postprocess();
		postProcessPerf.end();
		
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
//		absSq = new double[newDimX][newDimY];
		notFinishedFraction = new float[newDimX][newDimY];

		
		for (int x = 0 ; x < newDimX ; x++){
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
						if (sample <= 0){
							notFinished++;
							continue;
						}
//						summedValue += sample;
						double real = container.currentSamplePos_real[index];
						double imag = container.currentSamplePos_imag[index];
						summedValue += sample < 0 ? 0 : Math.sqrt( sample + 1 -  Math.log( Math.log(real*real+imag*imag)*0.5 ) / Math.log(2)  );
//						summedAbsSq += real*real+imag*imag;
					}
				}
				double weight = 1d/(qualityScaling*qualityScaling - notFinished);
				samples[x][y] = summedValue*weight;
//				absSq[x][y] = summedAbsSq*weight;
				notFinishedFraction[x][y] = notFinished/((float)qualityScaling*qualityScaling);
			}
		}
		return true;
	}
	
	private void postprocess() {
		
		int rad = 0;
		int boxBlurIterations = 1;
		int radDim = rad*2+1;
		
		int dim_x = samples.length;
		int dim_y = samples[0].length;
//		double[] buff_samples = new double[radDim*radDim];
//		double[] buff_samples = new double[radDim*radDim];
//		double[] buff_weights = new double[radDim*radDim];
//		double[][] weights = new double[radDim][radDim];
//		for (int x = 0 ; x < radDim ; x++) {
//			for (int y = 0 ; y < radDim ; y++) {
//				int dx = x-rad;
//				int dy = y-rad;
//				double distanceValue = 1./(1+dx*dx+dy*dy);
//				weights[x][y] = distanceValue;
//			}
//		}
//		weights[rad][rad] = 0;
		
//		System.out.println("WEIGHTS");
//		for (int y = 0 ; y < weights[0].length ; y++) {
//			StringBuilder sb = new StringBuilder();
//			for (int x = 0 ; x < weights.length-1 ; x++) {
//				sb.append(NumberUtil.getRoundedPercentage(weights[x][y], 1)).append(", ");
//			}
//			sb.append(NumberUtil.getRoundedPercentage(weights[weights.length-1][y], 1));
//			System.out.println(sb.toString());
//		}
		
//		double[][] adjSamples = new double[samples.length][samples[0].length];
//		
//		for (int x = 0 ; x < samples.length ; x++) {
//			for (int y = 0 ; y < samples[x].length ; y++) {
//				double s = samples[x][y];
//				adjSamples[x][y] = s < 0 ? 0 : Math.sqrt( s + 1 -  Math.log( Math.log(absSq[x][y])*0.5 ) / Math.log(2)  );
//			}
//		}
		
		double[][] adjSamples = samples;
		
		double[][] pass1 = new double[samples.length][samples[0].length];
		double[] pass_buffer = new double[radDim];
		int pass_buffer_offset = 0;

		fluctuance = new double[dim_x][dim_y];
		
		int iterationCount = 0;
		
		int kernelMid = rad;
		
		double[][] diff = new double[samples.length][samples[0].length];
		for (int x = 0 ; x < samples.length ; x++) {
			for (int y = 0 ; y < samples[x].length ; y++) {
				double neighbour_avg = 0;
				int c = 0;
				if (x > 0) {
					c++;
					neighbour_avg += replaceNaN(adjSamples[x-1][y]);
				}
				if (y > 0) {
					c++;
					neighbour_avg += replaceNaN(adjSamples[x][y-1]);
				}
				if (x < samples.length - 1) {
					c++;
					neighbour_avg += replaceNaN(adjSamples[x+1][y]);
				}
				if (y < samples[0].length - 1) {
					c++;
					neighbour_avg += replaceNaN(adjSamples[x][y+1]);
				}
				diff[x][y] = Math.abs(replaceNaN(adjSamples[x][y]) - neighbour_avg/c);
				iterationCount += c+1;
			}
		}
		
		double[] buff = new double[radDim];
		double addedValue = 0;
		int buff_index = 0;
		int buff_size = 0;
		
		for (int i = 0 ; i < boxBlurIterations ; i++) {
			//vertical box blur
			for (int x = 0 ; x < samples.length ; x++) {
				//read first values
				for (int y = 0 ; y < rad ; y++) {
					double value = diff[x][y];
					addedValue += value;
					buff[buff_index] = value;
					buff_index++;
					buff_size++;
				}
				//actual loop
				for (int y = 0 ; y < samples[0].length ; y++) {
					if (y+rad >= radDim) {
						addedValue -= buff[buff_index];
						buff_size--;
					}
					if (y+rad <= samples[0].length-1) {
						double value = diff[x][y+rad];
						buff_size++;
						addedValue += value;
						buff[buff_index] = value;
						buff_index = (buff_index+1) % radDim;
					}
					pass1[x][y] = addedValue/radDim;
				}
				buff_index = 0;
				buff_size = 0;
				addedValue = 0;
			}
			//horizontal box blur
			for (int y = 0 ; y < samples[0].length ; y++) {
				//read first values
				for (int x = 0 ; x < rad ; x++) {
					double value = pass1[x][y];
					addedValue += value;
					buff[buff_index] = value;
					buff_index++;
					buff_size++;
				}
				//actual loop
				for (int x = 0 ; x < samples.length ; x++) {
					if (x+rad >= radDim) {
						addedValue -= buff[buff_index];
						buff_size--;
					}
					if (x+rad <= samples.length-1) {
						double value = pass1[x+rad][y];
						buff_size++;
						addedValue += value;
						buff[buff_index] = value;
						buff_index = (buff_index+1) % radDim;
					}
					if (i == boxBlurIterations-1)
						fluctuance[x][y] = addedValue/buff_size;
					else
						diff[x][y] = addedValue/buff_size;
				}
				buff_index = 0;
				buff_size = 0;
				addedValue = 0;
			}
		}
		
//		CategoryLogger.INFO.log("fluctuance_distribution", Arrays.toString(fluctuanceDistribution));
		
		System.out.println("postprocess itertations: "+iterationCount);
	}
	
	private double replaceNaN(double d) {
		if (Double.isNaN(d))
			return 0;
		return d;
	}

	public boolean isDone() {
		return done;
	}
}
