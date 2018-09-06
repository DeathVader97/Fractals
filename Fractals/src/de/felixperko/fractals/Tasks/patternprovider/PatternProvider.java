package de.felixperko.fractals.Tasks.patternprovider;

import de.felixperko.fractals.util.Position;

public interface PatternProvider {
	
	public Position[] getNextPattern(int patternState);

	public int getMaxState();
}
