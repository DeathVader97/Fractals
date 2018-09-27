package de.felixperko.fractals.server.steps.patternprovider;

import java.util.List;

public interface PatternProvider {

	public List<Pattern> getPatterns();
	public Pattern getPattern(int patternId);
	public Pattern getNextPattern(int patternState);

	public int getMaxState();
}
