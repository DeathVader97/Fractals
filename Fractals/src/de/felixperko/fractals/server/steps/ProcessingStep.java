package de.felixperko.fractals.server.steps;

import java.util.BitSet;

import de.felixperko.fractals.server.steps.masks.IndexMask;
import de.felixperko.fractals.server.steps.patternprovider.Pattern;

public interface ProcessingStep {
	
	public Pattern getPattern();
	public IndexMask getIndexMask();
	public BitSet getActiveIndices();
	public int getMaxIterations();
	public float getDiffScale();
	public int getNeigbourOffset();
	public boolean isDrawable();
	public boolean isProbeStep();
	public int getActiveCount();
	public boolean isDefault();
}
