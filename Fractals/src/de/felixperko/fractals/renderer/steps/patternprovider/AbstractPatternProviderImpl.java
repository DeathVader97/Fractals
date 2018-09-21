package de.felixperko.fractals.renderer.steps.patternprovider;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.util.Position;

public abstract class AbstractPatternProviderImpl implements PatternProvider {

	List<Pattern> patterns = new ArrayList<>();
	int totalCount = 0;
	
	public AbstractPatternProviderImpl() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<Pattern> getPatterns() {
		return patterns;
	}
	
	@Override
	public Pattern getNextPattern(int patternState) {
		return patterns.get(patternState+1);
	}
	
	@Override
	public Pattern getPattern(int patternId) {
		if (patternId == -1)
			return null;
		return patterns.get(patternId);
	}
	
	protected void addPattern(Pattern pattern) {
		Position[] positions = pattern.getPositions();
		totalCount += positions.length;
		pattern.setSummedCount(totalCount);
		patterns.add(pattern);
	}
	
	protected void addPattern(boolean generic, Position... positions) {
		addPattern(new Pattern(generic, positions));
	}
	
	protected void addRandomPattern(int stepSize) {
		addPattern(true, getRandomPatternPositions(stepSize));
	}
	
	private Position[] getRandomPatternPositions(int size) {
		Position[] ans = new Position[size];
		for (int i = 0 ; i < size ; i++) {
			ans[i] = new Position(Math.random()-0.5, Math.random()-0.5);
		}
		return ans;
	}
	
	@Override
	public int getMaxState() {
		return patterns.size()-1;
	}
}
