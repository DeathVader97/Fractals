package de.felixperko.fractals.Tasks.steps.patternprovider;

import java.util.List;

import de.felixperko.fractals.util.Position;

public interface PatternProvider {

	public List<Pattern> getPatterns();
	public Pattern getPattern(int patternId);
	public Pattern getNextPattern(int patternState);

	public int getMaxState();
}
