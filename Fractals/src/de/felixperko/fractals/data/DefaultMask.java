package de.felixperko.fractals.data;

public class DefaultMask implements IndexMask {
	
	public static DefaultMask instance = new DefaultMask();

	@Override
	public double getWeight() {
		return 0;
	}

	@Override
	public int getIndex(int i) {
		return i;
	}

}
