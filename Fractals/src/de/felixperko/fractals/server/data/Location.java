package de.felixperko.fractals.server.data;

public class Location {
	
	static int COUNTER = 0;
	
	double[] bounds = new double[4];
	String name;
	
	public Location(String serialized) {
		String[] s = serialized.split(",");
		name = s[0];
		for (int i = 0; i < bounds.length; i++) {
			bounds[i] = Double.parseDouble(s[i+1]);
		}
	}
	
	public Location(double minX, double minY, double maxX, double maxY) {
		bounds[0] = minX;
		bounds[1] = maxX;
		bounds[2] = minY;
		bounds[3] = maxY;
	}

	public Location(DataDescriptor dataDescriptor, String name) {
//		midX = dataDescriptor.getStart_x() + dataDescriptor.getDim_sampled_x()*dataDescriptor.getSpacing()/2;
//		midY = dataDescriptor.getStart_y() + dataDescriptor.getDim_sampled_y()*dataDescriptor.getSpacing()/2;
//		spacing = dataDescriptor.getDim_sampled_x()*dataDescriptor.getSpacing()/2;
		this.name = name;
		bounds[0] = dataDescriptor.getStart_x();
		bounds[1] = dataDescriptor.getStart_y();
		bounds[2] = dataDescriptor.getEnd_x();
		bounds[3] = dataDescriptor.getEnd_y();
	}

	public String serialize() {
		StringBuilder builder = new StringBuilder(100);
		builder.append(name).append(", ");
		for (int i = 0; i < 3; i++) {
			builder.append(bounds[i]).append(", ");
		}
		builder.append(bounds[3]);
		return builder.toString();
	}

	public double getX1() {
		return bounds[0];
	}

	public double getY1() {
		return bounds[1];
	}

	public double getX2() {
		return bounds[2];
	}

	public double getY2() {
		return bounds[3];
	}

	public String getName() {
		return name;
	}
}
