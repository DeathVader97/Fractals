package de.felixperko.fractals.Tasks.patternprovider;

import de.felixperko.fractals.util.Position;

public interface PatternProvider {

	public Pattern getPattern(int patternId);
	public Pattern getNextPattern(int patternState);

	public int getMaxState();
}
