package de.felixperko.fractals.Tasks.patternprovider;

import de.felixperko.fractals.util.Position;

public class SinglePatternProvider implements PatternProvider {
	
	Position[] pattern;
	
	public SinglePatternProvider(int positionCount) {
		if (positionCount == 1)
			pattern = new Position[] {new Position(0,0)};
		else
			pattern = getRandomPattern(positionCount);
	}
	
	@Override
	public Position[] getNextPattern(int patternState) {
		if (patternState != -1)
			throw new IllegalArgumentException();
		return pattern;
	}

	@Override
	public int getMaxState() {
		return 0;
	}
	
	private Position[] getRandomPattern(int size) {
		Position[] ans = new Position[size];
		for (int i = 0 ; i < size ; i++) {
			ans[i] = new Position(Math.random()-0.5, Math.random()-0.5);
		}
		return ans;
	}

}
