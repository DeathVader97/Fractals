package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;

public class SampleCalculator {
	
	DataDescriptor descriptor;
	Task task;
	
	public SampleCalculator(DataDescriptor dataDescriptor, Task task) {
		this.descriptor = dataDescriptor;
		this.task = task;
	}
	
	long run_iterations = 0;
	
	public void calculate_samples(int start, int end, int[] currentIterations, int maxIterations, double[] currentpos_real, double[] currentpos_imag, int[] results) {

		int dim_x = descriptor.getDim_sampled_x();
//		int dim_y = descriptor.getDim_sampled_y();
		
		mainLoop : 
		for (int i = 0 ; i < end-start ; i++) {
			
			if (results[i] != 0)
				continue;
			
			task.changedIndices.add(i);
			
			int x = (i+start) % dim_x;
			int y = (i+start) / dim_x;
			
			int j = currentIterations[i];
			double real = currentpos_real[i], imag = currentpos_imag[i];
//			int j = 0;
//			double real = 0, imag = 0;
			double creal = descriptor.xcoords[x];
			double cimag = descriptor.ycoords[y];
			
			for ( ; j < maxIterations ; j++) {
				run_iterations++;
				double new_real = real*real - (imag*imag) + creal;
				double new_imag = real*imag + (imag*real) + cimag;
				real = new_real;
				imag = new_imag;
				
				if (real*real + imag*imag > 4) {//outside -> done
					results[i] = j;
					currentIterations[i] = j;
					currentpos_real[i] = real;
					currentpos_imag[i] = imag;
					continue mainLoop;
				}
			}
			
			//still not outside
			if (maxIterations < descriptor.maxIterations) { //not done -> store temp result
				currentIterations[i] = maxIterations;
				currentpos_real[i] = real;
				currentpos_imag[i] = imag;
			} else { //max iterations reached -> declared as in the mandelbrot set
				results[i] = -1;
			}
		}
	}
}
