package de.felixperko.fractals;

public class DataDescriptor {
	
	double start_x, start_y;
	double spacing;
	
	public int dim_sampled_x;
	public int dim_sampled_y;
	int dim_goal_x, dim_goal_y;
	
	public int maxIterations;
	
	public double[] xcoords;
	public double[] ycoords;
	boolean calculatedCoords = false;
	
	public DataDescriptor(double start_x, double start_y, double spacing, int dim_sampled_x, int dim_sampled_y,
			int dim_goal_x, int dim_goal_y, int maxIterations) {
		this.start_x = start_x;
		this.start_y = start_y;
		this.spacing = spacing;
		this.dim_sampled_x = dim_sampled_x;
		this.dim_sampled_y = dim_sampled_y;
		this.dim_goal_x = dim_goal_x;
		this.dim_goal_y = dim_goal_y;
		this.maxIterations = maxIterations;
	}
	
	public void calculateCoords() {
		xcoords = new double[dim_sampled_x];
		ycoords = new double[dim_sampled_y];
		int samples_x = getDim_sampled_x();
		int samples_y = getDim_sampled_y();
		double start_x = getStart_x();
		double start_y = getStart_y();
		double spacing = getSpacing();
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

	public double getStart_x() {
		return start_x;
	}

	public double getStart_y() {
		return start_y;
	}

	public double getSpacing() {
		return spacing;
	}

	public int getDim_sampled_x() {
		return dim_sampled_x;
	}

	public int getDim_sampled_y() {
		return dim_sampled_y;
	}

	public int getDim_goal_x() {
		return dim_goal_x;
	}

	public int getDim_goal_y() {
		return dim_goal_y;
	}

	public int getMaxIterations() {
		return maxIterations;
	}
}
