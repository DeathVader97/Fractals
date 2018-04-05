package de.felixperko.fractals;

public class SampleCalculator {
	
	DataContainer container;
	
	public SampleCalculator(DataContainer container) {
		this.container = container;
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
	
	public static void main(String[] args) {
		DataDescriptor dd = new DataDescriptor(-0.763692674785, 0.079272369676, (0.763692674785-0.758830813761)/19200, 1920*10, 1080*10, 1920*10, 1080*10, 1000);
		System.out.println("xmin: "+dd.start_x+" ymin: "+dd.start_y);
		System.out.println(dd.getSpacing()*19200);
		DataContainer dc = new DataContainer(dd);
		dc.calculateCoords();
		SampleCalculator sc = new SampleCalculator(dc);
		long t1 = System.nanoTime();
		int[] res = sc.calculate_samples(0, 5000000);
		long t2 = System.nanoTime();
		System.out.println(sc.iterations_total);
		System.out.println((t2-t1)/1000000000.);
	}
	
	public int[] calculate_samples(int start, int end) {
		int[] samples = new int[end-start];
		int dim_y = container.descriptor.getDim_sampled_y();
		int x = start % dim_y;
		int y = start / dim_y;
		
		mainLoop : 
		for (int i = 0 ; i < end-start ; i++) {
			double real = 0, imag = 0;
			double creal = container.xcoords[x];
			double cimag = container.ycoords[y];
			int j = 0;
			for ( ; j < container.descriptor.maxIterations ; j++) {
				iterations_total++;
				double new_real = real*real - (imag*imag) + creal;
				double new_imag = real*imag + (imag*real) + cimag;
				real = new_real;
				imag = new_imag;
				if (real*real + imag*imag > 4) {
					samples[i] = j;
					if (++y == dim_y) {
						y = 0;
						x++;
					}
					continue mainLoop;
				}
			}
			//still not outside
			samples[i] = -1;
			if (++y == dim_y) {
				y = 0;
				x++;
			}
		}
		return samples;
	}
}
