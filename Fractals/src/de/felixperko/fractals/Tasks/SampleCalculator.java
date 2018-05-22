package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.util.Position;

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
		//TODO bias zu DataDescriptor
		
		mainLoop : 
		for (int i = 0 ; i < end-start ; i++) {
			
			if (results[i] != 0)
				continue;
			
			task.changedIndices.add(i);
			
			int x = (i+start) % dim_x;
			int y = (i+start) / dim_x;
			
			int j = currentIterations[i];
			double real = (j == 0) ? startReal : descriptor.xcoords[x];
			double imag = (j == 0) ? startImag : descriptor.ycoords[y];
			double creal = descriptor.xcoords[x];
			double cimag = descriptor.ycoords[y];
//			double real = currentpos_real[i], imag = currentpos_imag[i];
////			int j = 0;
////			double real = 0, imag = 0;
//			double creal = (j == 0) ? startReal : descriptor.xcoords[x];
//			double cimag = (j == 0) ? startImag : descriptor.ycoords[y];
			
			for ( ; j < maxIterations ; j++) {
				run_iterations++;
				double new_real = 1;
				double new_imag = 1;
				double realSq = 0;
				double imagSq = 0;
				for (int k = 1 ; k < pow ; k++){
//					real = Math.abs(real);
//					imag = Math.abs(imag);
					realSq = real*real;
					imagSq = imag*imag;
					new_real = realSq - imagSq;
					new_imag = 2*real*imag;
					real = new_real;
					imag = new_imag;
				}
				real += creal;
				imag += cimag;
				
				if (realSq + imagSq > 4) {//outside -> done
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
	
	public Position getIterationForPosition(Position startPos, Position currentPos){
		
		int pow = powState.getValue();
		
		double real = currentPos != null ? currentPos.getX() : (double) biasReal.getOutput();
		double imag = currentPos != null ? currentPos.getY() : (double) biasImag.getOutput();
		
		run_iterations++;
		
		double new_real = 1;
		double new_imag = 1;
		
		for (int k = 1 ; k < pow ; k++){
//			real = Math.abs(real);
//			imag = Math.abs(imag);
			new_real = (real*real - (imag*imag));
			new_imag = 2*(real*imag);
			real = new_real;
			imag = new_imag;
		}
		real += startPos.getX();
		imag += startPos.getY();
		
		return new Position(real, imag);
	}

	public DataDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(DataDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}
