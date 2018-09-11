package de.felixperko.fractals.Tasks.patternprovider;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.util.Position;

public class BasicPatternProvider extends AbstractPatternProviderImpl {
	
	List<Integer> summedCount = new ArrayList<>();
	
	public BasicPatternProvider(int maxCount, int stepSize) {
		
		addPattern(false, new Position(0,0));
		addPattern(false, new Position(-0.25, -0.25), new Position(0.25, 0.25));
		addPattern(false, new Position(0.25, -0.25), new Position(-0.25, 0.25));
		if (stepSize > 0){
			int count = (maxCount-totalCount)/stepSize;
			for (int i = 0 ; i < count ; i++) {
				addRandomPattern(stepSize);
			}
		}
		if (totalCount < maxCount)
			addRandomPattern(maxCount - totalCount);
	}

	public int getSummedSamplesAtState(int i) {
		return summedCount.get(i);
	}

}
