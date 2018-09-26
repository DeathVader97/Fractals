package de.felixperko.fractals.renderer.steps;

import java.util.BitSet;

import de.felixperko.fractals.data.IndexMask;
import de.felixperko.fractals.renderer.steps.patternprovider.Pattern;

public class DefaultProcessingStep implements ProcessingStep {

	@Override
	public Pattern getPattern() {
		return null;
	}

	@Override
	public IndexMask getIndexMask() {
		return null;
	}

	@Override
	public BitSet getActiveIndices() {
		return null;
	}

	@Override
	public int getMaxIterations() {
		return 0;
	}

	@Override
	public float getDiffScale() {
		return 0;
	}

	@Override
	public int getNeigbourOffset() {
		return 0;
	}

	@Override
	public boolean isDrawable() {
		return false;
	}

	@Override
	public boolean isProbeStep() {
		return false;
	}

	@Override
	public int getActiveCount() {
		return 0;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

}
