package de.felixperko.fractals;

import java.util.Arrays;

import de.felixperko.fractals.Tasks.perf.PerfInstance;
import de.felixperko.fractals.util.CategoryLogger;

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
		
		int rad = 2;
		int radDim = rad*2+1;
		
		int dim_x = samples.length;
		int dim_y = samples[0].length;
		double[] buff_samples = new double[radDim*radDim];
		double[] buff_weights = new double[radDim*radDim];
		double[][] weights = new double[radDim][radDim];
		for (int x = 0 ; x < radDim ; x++) {
			for (int y = 0 ; y < radDim ; y++) {
				int dx = x-rad;
				int dy = y-rad;
				weights[x][y] = 1./(1+dx*dx+dy*dy);
			}
		}
		
		double[][] adjSamples = new double[samples.length][samples[0].length];
		for (int x = 0 ; x < samples.length ; x++) {
			for (int y = 0 ; y < samples[x].length ; y++) {
				double s = samples[x][y];
				adjSamples[x][y] = s < 0 ? s : Math.sqrt( s + 1 -  Math.log( Math.log(absSq[x][y])*0.5 ) / Math.log(2)  );
			}
		}

		fluctuance = new double[dim_x][dim_y];
		
		int iterationCount = 0;
		
		for (int x = 0 ; x < samples.length ; x++) {
			for (int y = 0 ; y < samples[x].length ; y++) {
				double avg = 0;
				int min_x = (x-rad < 0) ? 0 : x-rad;
				int min_y = (y-rad < 0) ? 0 : y-rad;
				int max_x = (x+rad >= dim_x-1) ? dim_x-1 : x+rad;
				int max_y = (y+rad >= dim_y-1) ? dim_y-1 : y+rad;
				int n = (1+max_x-min_x)*(1+max_y-min_y);
				double totalWeight = 0;
				int i = 0;
				for (int x2 = min_x ; x2 <= max_x ; x2++) {
					for (int y2 = min_y ; y2 <= max_y ; y2++) {
						iterationCount++;
						double weight = weights[x2-min_x][y2-min_y];
						double sample = adjSamples[x2][y2];
						if (sample < 0)
							continue;
						buff_samples[i] = sample;
						buff_weights[i] = weight;
						avg += sample;
						totalWeight += weight;
						i++;
					}
				}
				avg /= n;
				double val = 0;
				double notFinishedFactor = 0;
				for (int j = 0 ; j < n ; j++) {
					double a = buff_weights[j]*buff_weights[j]*Math.abs(buff_samples[j]-avg);
					if (a > 0)
						val += a;
					else{
						val = 10000;
						break;
					}
				}
//				val -= 1;
//				fluctuance[x][y] = val >= 2 ? val : 2;
				fluctuance[x][y] = val;
				int distributionIndex = (int)(val*10);
				if (distributionIndex >= fluctuanceDistribution.length-1)
					distributionIndex = fluctuanceDistribution.length-1;
				fluctuanceDistribution[distributionIndex]++;
				
//				System.out.print(n+" ");
			}
		}
		
//		CategoryLogger.INFO.log("fluctuance_distribution", Arrays.toString(fluctuanceDistribution));
			
			
//			int dim_x = samples.length;
//			int dim_y = samples[0].length;
//			double[][] buff_samples = new double[3][3];
//			int offsetX = 0;
//			int offsetY = 0;
//			
//			fluctuance = new double[dim_x][dim_y];
//			
//			int iterationCount = 0;
//			double log2 = Math.log(2);
//
//			for (int x = 0 ; x < 2 ; x++) {
//				for (int y = 0 ; y < 1 ; y++) {
//					double s = samples[x][y];
//					s = s < 0 ? 0 : Math.sqrt( s -  Math.log( Math.log(absSq[x][y])*0.5/log2 ) / log2  );
//					buff_samples[x][y] = s;
//				}
//			}
//			offsetX = 2;
//
//			for (int y = 0 ; y < samples[0].length ; y++) {
//				for (int x = 0 ; x < samples.length ; x++) {
//					int min_x = (x == 0) ? 0 : x-1;
//					int min_y = (y == 0) ? 0 : y-1;
//					int max_x = (x == dim_x-1) ? x : x+1;
//					int max_y = (y == dim_y-1) ? y : y+1;
//					
//					int x2 = x+1;
//					for (int y2 = min_y ; y2 <= max_y ; y2++) {
//						double s = samples[x2][y2];
//						s = s < 0 ? 0 : Math.sqrt( s -  Math.log( Math.log(absSq[x][y])*0.5/log2 ) / log2  );
//						buff_samples[offsetX][offsetY] = s;
//						offsetY++;
//						offsetY %= max_y-min_y;
//					}
//					
////					int n = (1+max_x-min_x)*(1+max_y-min_y);
////					int i = 0;
////					for (int x2 = min_x ; x2 <= max_x ; x2++) {
////						for (int y2 = min_y ; y2 <= max_y ; y2++) {
////							iterationCount++;
////							double sample = samples[x2][y2];
////							if (sample < 0) {
//////								n--;
//////								continue;
////								sample = 0;
////							} else {
////								sample = Math.sqrt( sample -  Math.log( Math.log(absSq[x2][y2])*0.5/log2 ) / log2  );
////							}
////							buff_samples[i] = sample;
////							avg += sample;
////							i++;
////						}
////					}
//					avg /= n;
//					double val = 0;
//					for (int j = 0 ; j < n ; j++) {
//						double a = Math.abs(buff_samples[j]-avg);
//						if (a >= 0)
//							val += a;
//					}
////					val -= 1;
//					if (val > 0)
//						fluctuance[x][y] = val;
////					System.out.print(n+" ");
//				}
//		}
		System.out.println("postprocess itertations: "+iterationCount);
	}
	
	public boolean isDone() {
		return done;
	}
}
