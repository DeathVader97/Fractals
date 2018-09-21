package de.felixperko.fractals.renderer.steps;

import java.util.BitSet;

import de.felixperko.fractals.data.IndexMask;
import de.felixperko.fractals.renderer.steps.patternprovider.Pattern;

public interface ProcessingStep {
	
	public Pattern getPattern();
	public IndexMask getIndexMask();
	public BitSet getActiveIndices();
	public int getMaxIterations();
	public float getDiffScale();
}
