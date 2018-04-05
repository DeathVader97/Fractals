package de.felixperko.fractals;

public class DataContainer {
	
	DataDescriptor descriptor;
	
	double[] xcoords;
	double[] ycoords;
	
	int[][] samples;
	int[][] values;
	
	boolean calculatedCoords = false;
	
	
	public DataContainer(DataDescriptor descriptor) {
		
		this.descriptor = descriptor;
		
		int samples_x = descriptor.getDim_sampled_x();
		int samples_y = descriptor.getDim_sampled_y();
		xcoords = new double[samples_x];
		ycoords = new double[samples_y];
		samples = new int[samples_x][samples_y];
		values = new int[descriptor.getDim_goal_x()][descriptor.getDim_goal_y()];
	}
	
	public void calculateCoords() {
		int samples_x = descriptor.getDim_sampled_x();
		int samples_y = descriptor.getDim_sampled_y();
		double start_x = descriptor.getStart_x();
		double start_y = descriptor.getStart_y();
		double spacing = descriptor.getSpacing();
		for (int i = 0 ; i < samples_x ; i++) {
			xcoords[i] = start_x + spacing * i;
		}
		for (int i = 0 ; i < samples_y ; i++) {
			ycoords[i] = start_y + spacing * i;
		}
		calculatedCoords = true;
	}

	public double[] getXcoords() throws Exception{
		if (!calculatedCoords)
			throw new Exception("coordinates not yet calculated!");
		return xcoords;
	}

	public double[] getYcoords() throws Exception{
		if (!calculatedCoords)
			throw new Exception("coordinates not yet calculated!");
		return ycoords;
	}
}
