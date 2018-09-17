package de.felixperko.fractals.Tasks.steps;

import java.util.BitSet;

import de.felixperko.fractals.Tasks.steps.patternprovider.Pattern;
import de.felixperko.fractals.data.IndexMask;

public interface ProcessingStep {
	
	public Pattern getPattern();
	public IndexMask getIndexMask();
	public BitSet getActiveIndices();
	public int getMaxIterations();
}
