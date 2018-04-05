package de.felixperko.fractals;

public class DataDescriptor {
	
	double start_x, start_y;
	double spacing;
	
	int dim_sampled_x, dim_sampled_y;
	int dim_goal_x, dim_goal_y;
	
	int maxIterations;
	
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
