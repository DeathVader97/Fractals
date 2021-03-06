package de.felixperko.fractals.util;

public class NumberUtil {

	public static final double MS_TO_S = 0.001;
	public static final double NS_TO_S = 0.000000001;
	public static final double NS_TO_MS = 0.000001;
	
	public static double getRoundedDouble(double value, int precision) {
		double f = Math.pow(10, precision);
		return Math.round(value*f)/f;
	}
	
	public static double getRoundedPercentage(double value, int precision) {
		double f = Math.pow(10, precision);
		return Math.round(value*100*f)/f;
	}
}
