package de.felixperko.fractals.data;

import de.felixperko.fractals.Tasks.patternprovider.Pattern;
import de.felixperko.fractals.Tasks.patternprovider.PatternProvider;

public class PatternState {
	
	PatternProvider patternProvider;
	int id;
	
	/**
	 * creates a default pattern state (id = -1)
	 * @param patternProvider
	 */
	public PatternState(PatternProvider patternProvider) {
		this.patternProvider = patternProvider;
		id = -1;
	}
	
	public PatternState(PatternProvider patternProvider, int id) {
		this.patternProvider = patternProvider;
		this.id = id;
	}
	
	public Pattern getPattern() {
		return patternProvider.getPattern(id);
	}
	
	public void increment() {
		if (this.id == patternProvider.getMaxState())
			throw new IllegalStateException("The pattern state is already maxed.");
		this.id++;
	}

	public PatternState copy() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDefaultState() {
		return id == -1;
	}

	public int getId() {
		return id;
	}
}
