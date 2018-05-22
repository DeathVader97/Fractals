package de.felixperko.fractals;

public class Location {
	
	static int COUNTER = 0;
	
	double midX, midY, spacing;
	String name;
	
	public Location(String serialized) {
		String[] s = serialized.split(",");
		name = s[0];
		midX = Double.parseDouble(s[1]);
		midY = Double.parseDouble(s[2]);
		spacing = Double.parseDouble(s[3]);
	}
	
	public Location(double midX, double midY, double spacing) {
		this.midX = midX;
		this.midY = midY;
		this.spacing = spacing;
	}

	public Location(DataDescriptor dataDescriptor) {
		midX = dataDescriptor.getStart_x() + dataDescriptor.getDim_sampled_x()*dataDescriptor.getSpacing()/2;
		midY = dataDescriptor.getStart_y() + dataDescriptor.getDim_sampled_y()*dataDescriptor.getSpacing()/2;
		spacing = dataDescriptor.getDim_sampled_x()*dataDescriptor.getSpacing()/2;
	}

	public String serialize() {
		StringBuilder builder = new StringBuilder(100);
		builder.append(name).append(", ");
		builder.append(midX).append(", ");
		builder.append(midY).append(", ");
		builder.append(spacing);
		return builder.toString();
	}

	public double getX1() {
		return midX - spacing;
	}

	public double getY1(double ratio) {
		return midY - spacing/ratio;
	}

	public double getX2() {
		return midX + spacing;
	}

	public double getY2(double ratio) {
		return midY + spacing/ratio;
	}

	public String getName() {
		return name;
	}
}
