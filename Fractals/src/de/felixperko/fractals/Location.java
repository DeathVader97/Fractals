package de.felixperko.fractals;

public class Location {
	
	double startX, startY, spacing;
	
	public Location(String serialized) {
		String[] s = serialized.split(",");
		startX = Double.parseDouble(s[0]);
		startY = Double.parseDouble(s[1]);
		spacing = Double.parseDouble(s[2]);
	}
	
	public Location(double startX, double startY, double spacing) {
		this.startX = startX;
		this.startY = startY;
		this.spacing = spacing;
	}
	
	public String serialize() {
		StringBuilder builder = new StringBuilder(100);
		builder.append(startX).append(", ");
		builder.append(startY).append(", ");
		builder.append(spacing);
		return builder.toString();
	}
}
