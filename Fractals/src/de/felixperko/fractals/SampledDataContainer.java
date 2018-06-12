package de.felixperko.fractals;

import java.util.Arrays;

import de.felixperko.fractals.Tasks.perf.PerfInstance;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public class SampledDataContainer {
	
	boolean done = false;
	double[][] samples;
	double[][] absSq;
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
		absSq = new double[newDimX][newDimY];
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
						summedValue += sample;
						double real = container.currentSamplePos_real[index];
						double imag = container.currentSamplePos_imag[index];
						summedAbsSq += real*real+imag*imag;
					}
				}
				double weight = 1d/(qualityScaling*qualityScaling - notFinished);
				samples[x][y] = summedValue*weight;
				absSq[x][y] = summedAbsSq*weight;
				notFinishedFraction[x][y] = notFinished/((float)qualityScaling*qualityScaling);
			}
		}
		return true;
	}
	
	private void postprocess() {
		
		int rad = 3;
		int radDim = rad*2+1;
		
		int dim_x = samples.length;
		int dim_y = samples[0].length;
//		double[] buff_samples = new double[radDim*radDim];
		double[] buff_samples = new double[radDim];
		double[] buff_weights = new double[radDim*radDim];
		double[][] weights = new double[radDim][radDim];
		for (int x = 0 ; x < radDim ; x++) {
			for (int y = 0 ; y < radDim ; y++) {
				int dx = x-rad;
				int dy = y-rad;
				double distanceValue = 1./(1+dx*dx+dy*dy);
				weights[x][y] = distanceValue;
			}
		}
		weights[rad][rad] = 0;
		
//		System.out.println("WEIGHTS");
//		for (int y = 0 ; y < weights[0].length ; y++) {
//			StringBuilder sb = new StringBuilder();
//			for (int x = 0 ; x < weights.length-1 ; x++) {
//				sb.append(NumberUtil.getRoundedPercentage(weights[x][y], 1)).append(", ");
//			}
//			sb.append(NumberUtil.getRoundedPercentage(weights[weights.length-1][y], 1));
//			System.out.println(sb.toString());
//		}
		
		double[][] adjSamples = new double[samples.length][samples[0].length];
		for (int x = 0 ; x < samples.length ; x++) {
			for (int y = 0 ; y < samples[x].length ; y++) {
				double s = samples[x][y];
				adjSamples[x][y] = s < 0 ? s : Math.sqrt( s + 1 -  Math.log( Math.log(absSq[x][y])*0.5 ) / Math.log(2)  );
			}
		}
		double[][] pass1 = new double[samples.length][samples[0].length];
		double[] pass_buffer = new double[radDim];
		int pass_buffer_offset = 0;

		fluctuance = new double[dim_x][dim_y];
		
		int iterationCount = 0;
		
		int kernelMid = rad;
		
		for (int x = 0 ; x < samples.length ; x++) {
			int buffered_value_count = rad;
			for (int initY = 0 ; initY < buffered_value_count ; initY++) {
				pass_buffer[initY] = adjSamples[x][initY];
			}
			pass_buffer_offset = buffered_value_count;
			for (int y = 0 ; y < samples[x].length ; y++) {
				int newSampleY = y+rad;
				if (newSampleY < dim_y-1) {
					pass_buffer[(pass_buffer_offset)%radDim] = adjSamples[x][newSampleY];
					if (buffered_value_count < radDim)
						buffered_value_count++;
				}
				int startOffset = (buffered_value_count < radDim) ? radDim-buffered_value_count : 0;
				double avg = pass_buffer[(pass_buffer_offset-rad)%radDim];
				pass_buffer_offset++;
				double summedDelta = 0;
				double totalWeight = 0;
				for (int i = 0 ; i < buffered_value_count ; i++) {
					int pass_buffer_index = (pass_buffer_offset+startOffset+i)%radDim;
					double weight = weights[kernelMid][(i+startOffset)%radDim];
					double value = pass_buffer[pass_buffer_index];
					if (weight > 0 && value > 0) {
						summedDelta += weight*Math.abs(value - avg);
						totalWeight += weight;
					}
				}
				fluctuance[x][y] = summedDelta/totalWeight;
			}
		}

//		for (int y = 0 ; y < samples[0].length ; y++) {
//			int buffered_value_count = rad+1;
//			for (int initX = 0 ; initX < buffered_value_count ; initX++) {
//				pass_buffer[initX] = pass1[initX][y];
//			}
//			for (int x = 0 ; x < samples.length ; x++) {
//				int newSampleX = x+rad;
//				if (newSampleX < dim_x-1) {
//					pass_buffer[(pass_buffer_offset)%radDim] = pass1[newSampleX][y];
//					if (buffered_value_count < radDim)
//						buffered_value_count++;
//				}
//				int startOffset = (buffered_value_count < radDim) ? radDim-buffered_value_count : 0;
//				pass_buffer_offset++;
//				double summedDelta = 0;
//				double totalWeight = 0;
//				for (int i = 0 ; i < buffered_value_count ; i++) {
//					int pass_buffer_index = (startOffset+i)%radDim;
//					double weight = weights[kernelMid][i];
//					double value = pass_buffer[pass_buffer_index];
//					summedDelta += value*weight;
//					totalWeight += weight;
//				}
//				fluctuance[x][y] = summedDelta/totalWeight;
//			}
//			pass_buffer_offset = 0;
//		}
		
//		for (int x = 0 ; x < samples.length ; x++) {
//			for (int y = 0 ; y < samples[x].length ; y++) {
//				double avg = 0;
//				int min_x = (x-rad < 0) ? 0 : x-rad;
//				int min_y = (y-rad < 0) ? 0 : y-rad;
//				int max_x = (x+rad >= dim_x-1) ? dim_x-1 : x+rad;
//				int max_y = (y+rad >= dim_y-1) ? dim_y-1 : y+rad;
//				int n = (1+max_x-min_x)*(1+max_y-min_y);
//				double totalWeight = 0;
//				int i = 0;
//				for (int x2 = min_x ; x2 <= max_x ; x2++) {
//					for (int y2 = min_y ; y2 <= max_y ; y2++) {
//						iterationCount++;
//						double weight = weights[x2-min_x][y2-min_y];
//						double sample = adjSamples[x2][y2];
//						if (sample < 0)
//							continue;
//						buff_samples[i] = sample;
//						buff_weights[i] = weight;
//						avg += sample*weight;
//						totalWeight += weight;
//						i++;
//					}
//				}
//				avg /= totalWeight;
//				double val = 0;
//				double notFinishedFactor = 0;
//				for (int j = 0 ; j < n ; j++) {
//					double a = buff_weights[j]*Math.abs(buff_samples[j]-avg);
//					if (a > 0)
//						val += a;
//					else{
////						val += buff_weights[j]*10000;
//						continue;
//					}
//				}
////				val -= 1;
////				fluctuance[x][y] = val >= 2 ? val : 2;
//				fluctuance[x][y] = val/totalWeight;
//				int distributionIndex = (int)(val*10);
//				if (distributionIndex >= fluctuanceDistribution.length-1)
//					distributionIndex = fluctuanceDistribution.length-1;
//				fluctuanceDistribution[distributionIndex]++;
//				
////				System.out.print(n+" ");
//			}
//		}
		
//		CategoryLogger.INFO.log("fluctuance_distribution", Arrays.toString(fluctuanceDistribution));
		
		System.out.println("postprocess itertations: "+iterationCount);
	}
	
	public boolean isDone() {
		return done;
	}
}
