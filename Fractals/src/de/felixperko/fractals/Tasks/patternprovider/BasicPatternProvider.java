package de.felixperko.fractals.Tasks.patternprovider;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.util.Position;

public class BasicPatternProvider implements PatternProvider {
	
	List<Position[]> patterns = new ArrayList<>();
	List<Integer> summedCount = new ArrayList<>();
	int totalCount = 0;
	
	public BasicPatternProvider(int maxCount, int stepSize) {
		
		addPattern(new Position(0,0));
		addPattern(new Position(-0.25, -0.25), new Position(0.25, 0.25));
		addPattern(new Position(0.25, -0.25), new Position(-0.25, 0.25));
		if (stepSize > 0){
			int c = (maxCount-totalCount)/stepSize;
			for (int i = 0 ; i < c ; i++) {
				addPattern(getRandomPattern(stepSize));
			}
		}
		if (totalCount < maxCount)
			addPattern(getRandomPattern(maxCount - totalCount));
	}
	
	@Override
	public Position[] getNextPattern(int patternState) {
		return patterns.get(patternState+1);
	}
	
	protected void addPattern(Position... pattern) {
		totalCount += pattern.length;
		summedCount.add(totalCount);
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

	public int getSummedSamplesAtState(int i) {
		return summedCount.get(i);
	}

}
