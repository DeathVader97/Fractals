package de.felixperko.fractals.Tasks.steps.patternprovider;

import java.util.List;

import de.felixperko.fractals.util.Position;

public class SinglePatternProvider extends AbstractPatternProviderImpl {
	
	public SinglePatternProvider(int positionCount) {
		if (positionCount == 1)
			addPattern(false, new Position[] {new Position(0,0)});
		else
			addRandomPattern(positionCount);
	}
	
	@Override
	public Pattern getNextPattern(int patternState) {
		if (patternState != -1)
			throw new IllegalArgumentException();
		return patterns.get(0);
	}

	@Override
	public int getMaxState() {
		return 0;
	}

}
