package de.felixperko.fractals;

import de.felixperko.fractals.util.CategoryLogger;

public class DataDescriptor{
	
	private double start_x, start_y;
	private double end_x, end_y;
	private double delta_x, delta_y;
	private double spacing;
	
	private int dim_sampled_x;
	private int dim_sampled_y;
	private int dim_goal_x, dim_goal_y;
	
	private int maxIterations;
	
	private double[] xcoords;
	private double[] ycoords;
	private boolean calculatedCoords = false;
	
	public DataDescriptor(double start_x, double start_y, double end_x, double end_y, int dim_sampled_x, int dim_sampled_y,
			int dim_goal_x, int dim_goal_y, int maxIterations) {
		this.start_x = start_x;
		this.start_y = start_y;
		this.spacing = (end_x - start_x)/dim_sampled_x;
		this.end_x = end_x;
		this.end_y = end_y;
		this.dim_sampled_x = dim_sampled_x;
		this.dim_sampled_y = dim_sampled_y;
		this.dim_goal_x = dim_goal_x;
		this.dim_goal_y = dim_goal_y;
		this.maxIterations = maxIterations;
		this.delta_x = spacing*dim_goal_x;
		this.delta_y = spacing*dim_goal_y;
		this.end_x = start_x + delta_x;
		this.end_y = start_y + delta_y;
	}
	
	public void calculateCoords() {
		xcoords = new double[dim_sampled_x];
		ycoords = new double[dim_sampled_y];
		double samples_x = getDim_sampled_x();
		double samples_y = getDim_sampled_y();
		double start_x = getStart_x();
		double start_y = getStart_y();
		for (int i = 0 ; i < samples_x ; i++) {
			xcoords[i] = start_x + delta_x * i/samples_x;
		}
		for (int i = 0 ; i < samples_y ; i++) {
			ycoords[i] = start_y + delta_y * i/samples_y;
		}
		calculatedCoords = true;
		CategoryLogger.INFO.log("datadescriptor", "Calculated coordinates.");
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
	
	public void scaleBy(double scale){
		spacing *= scale;
		delta_x *= scale;
		delta_y *= scale;
		end_x = start_x + delta_x;
		end_y = start_y + delta_y;
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

	public void setGoalDimensions(int width, int height) {
		this.dim_goal_x = width;
		this.dim_goal_y = height;
	}

	public void setDim_sampled_x(int dim_sampled_x) {
		this.dim_sampled_x = dim_sampled_x;
	}

	public void setDim_sampled_y(int dim_sampled_y) {
		this.dim_sampled_y = dim_sampled_y;
	}
	
	public void setSampleDimensions(int x, int y) {
		this.dim_sampled_x = x;
		this.dim_sampled_y = y;
		this.spacing = delta_x/x;
		if (spacing != delta_y/y) {
			CategoryLogger.WARNING.log("datadescriptor", "Horizontal spacing doesn't align with vertical dimensions (spacingX="+spacing+" spacingY="+(delta_y/y));
		}
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public double getDelta_x() {
		return delta_x;
	}

	public double getDelta_y() {
		return delta_y;
	}

	public void setStart_x(double start_x) {
		this.start_x = start_x;
		this.end_x = start_x + delta_x;
	}

	public void setStart_y(double start_y) {
		this.start_y = start_y;
		this.end_y = start_y + delta_y;
	}

	public double getEnd_x() {
		return end_x;
	}

	public void setEnd_x(double end_x) {
		this.end_x = end_x;
		this.delta_x = end_x - start_x;
	}

	public double getEnd_y() {
		return end_y;
	}

	public void setEnd_y(double end_y) {
		this.end_y = end_y;
		this.delta_y = end_y - start_y;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;
		this.delta_x = dim_sampled_x*spacing;
		this.delta_y = dim_sampled_y*spacing;
		this.end_x = start_x + delta_x;
		this.end_y = start_y + delta_y;
	}
}
