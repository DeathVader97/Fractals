package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.state.State;

public class SampleCalculator {
	
	DataDescriptor descriptor;
	Task task;
	
	State<Integer> powState = FractalsMain.mainStateHolder.getState("Mandelbrot Power", Integer.class);
	State<Double> biasReal = FractalsMain.mainStateHolder.getState("bias real", Double.class);
	State<Double> biasImag = FractalsMain.mainStateHolder.getState("bias imag", Double.class);
	
	public SampleCalculator(DataDescriptor dataDescriptor, Task task) {
		this.descriptor = dataDescriptor;
		this.task = task;
	}
	
	long run_iterations = 0;
	
	public void calculate_samples(int start, int end, int[] currentIterations, int maxIterations, double[] currentpos_real, double[] currentpos_imag, int[] results) {

		int dim_x = descriptor.getDim_sampled_x();
//		int dim_y = descriptor.getDim_sampled_y();
		
		int pow = powState.getValue();
		double startReal = (double) biasReal.getOutput();
		double startImag = (double) biasImag.getOutput();
		
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
			double creal = (j == 0) ? startReal : descriptor.xcoords[x];
			double cimag = (j == 0) ? startImag : descriptor.ycoords[y];
			
			for ( ; j < maxIterations ; j++) {
				run_iterations++;
				double new_real = 1;
				double new_imag = 1;
				for (int k = 1 ; k < pow ; k++){
					new_real = (real*real - (imag*imag));
					new_imag = (real*imag + (imag*real));
					real = new_real;
					imag = new_imag;
				}
				real += creal;
				imag += cimag;
				
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
