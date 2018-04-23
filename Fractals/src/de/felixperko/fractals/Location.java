package de.felixperko.fractals;

public class Location {
	
	double midX, midY, spacing;
	
	public Location(String serialized) {
		String[] s = serialized.split(",");
		midX = Double.parseDouble(s[0]);
		midY = Double.parseDouble(s[1]);
		spacing = Double.parseDouble(s[2]);
	}
	
	public Location(double midX, double midY, double spacing) {
		this.midX = midX;
		this.midY = midY;
		this.spacing = spacing;
	}

	public Location(DataDescriptor dataDescriptor) {
		midX = dataDescriptor.start_x + dataDescriptor.dim_sampled_x*dataDescriptor.spacing/2;
		midY = dataDescriptor.start_y + dataDescriptor.dim_sampled_y*dataDescriptor.spacing/2;
		spacing = dataDescriptor.dim_sampled_x*dataDescriptor.spacing/2;
	}

	public String serialize() {
		StringBuilder builder = new StringBuilder(100);
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
}
