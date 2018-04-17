package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;

public class SampleCalculator {
	
	DataDescriptor descriptor;
	
	public SampleCalculator(DataDescriptor dataDescriptor) {
		this.descriptor = dataDescriptor;
	}

//	public void calculate_samples(int start_x, int start_y, int end_x, int end_y, int end) {
//		for (int x = start_x ; x < end_x ; x++) {
//			int dim_y = (x == end_x) ? end_y : container.descriptor.dim_sampled_y;
//			int first_y = (x == start_x) ? start_y : 0;
//			for (int y = first_y ; y < dim_y ; ++y) {
//				
//			}
//		}
//	}
	
	long iterations_total = 0;
	
//	public static void main(String[] args) {
//		DataDescriptor dd = new DataDescriptor(-0.763692674785, 0.079272369676, (0.763692674785-0.758830813761)/19200, 1920*10, 1080*10, 1920*10, 1080*10, 1000);
//		System.out.println("xmin: "+dd.start_x+" ymin: "+dd.start_y);
//		System.out.println(dd.getSpacing()*19200);
//		DataContainer dc = new DataContainer(dd);
//		dc.calculateCoords();
//		SampleCalculator sc = new SampleCalculator(dc);
//		long t1 = System.nanoTime();
//		int[] res = sc.calculate_samples(0, 5000000);
//		long t2 = System.nanoTime();
//		System.out.println(sc.iterations_total);
//		System.out.println((t2-t1)/1000000000.);
//	}
	
	public void calculate_samples(int start, int end, int[] currentIterations, int maxIterations, double[] currentpos_real, double[] currentpos_imag, int[] results) {
		int dim_x = descriptor.getDim_sampled_x();
		int dim_y = descriptor.getDim_sampled_y();
		int x = start % dim_x;
		int y = start / dim_x;
		
		mainLoop : 
		for (int i = 0 ; i < end-start ; i++) {
			
			if (results[i] != 0)
				continue;
			
			int j = currentIterations[i];
			double real = currentpos_real[i], imag = currentpos_imag[i];
			double creal = descriptor.xcoords[x];
			double cimag = descriptor.ycoords[y];
			
			for ( ; j < maxIterations ; j++) {
				iterations_total++;
				double new_real = real*real - (imag*imag) + creal;
				double new_imag = real*imag + (imag*real) + cimag;
				real = new_real;
				imag = new_imag;
				
				if (real*real + imag*imag > 4) {//check if outside
					results[i] = j;
					currentIterations[i] = j;
					currentpos_real[i] = real;
					currentpos_imag[i] = imag;
					x++;
					if (x == dim_x) {
						x = 0;
						y++;
					}
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
			x++;
			if (x == dim_x) {
				x = 0;
				y++;
			}
		}
	}
}
