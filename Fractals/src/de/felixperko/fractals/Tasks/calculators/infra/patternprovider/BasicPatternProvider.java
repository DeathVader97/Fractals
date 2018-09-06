package de.felixperko.fractals.Tasks.calculators.infra.patternprovider;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.util.Position;

public class BasicPatternProvider implements PatternProvider {
	
	List<Position[]> patterns = new ArrayList<>();
	
	public BasicPatternProvider(int maxCount, int stepSize) {
		int count = 0;
		addPattern(new Position(0,0));
		count++;
		int c = maxCount/stepSize;
		int i = 0;
		if (stepSize > 1) {
			addPattern(getRandomPattern(stepSize-1));
			i++;
			count += stepSize-1;
		}
		for ( ; i < c ; i++) {
			int step = stepSize;
			if (count+step > maxCount) {
				step = maxCount - count;
			}
			if (step != 0) {
				addPattern(getRandomPattern(step));
				count += step;
			}
		}
	}
	
	@Override
	public Position[] getNextPattern(int patternState) {
		return patterns.get(patternState+1);
	}
	
	protected void addPattern(Position... pattern) {
		patterns.add(pattern);
	}
	
	private Position[] getRandomPattern(int size) {
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
