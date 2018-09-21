package de.felixperko.fractals.renderer.steps;

import java.util.BitSet;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.DefaultMask;
import de.felixperko.fractals.data.Grid;
import de.felixperko.fractals.data.IndexMask;
import de.felixperko.fractals.renderer.steps.patternprovider.Pattern;
import de.felixperko.fractals.util.Position;

public class ProcessingStepImpl implements ProcessingStep {
	
	static Pattern defaultPattern = new Pattern(false, new Position(0,0));
	static IndexMask defaultIndexMask = new DefaultMask();
	
	Pattern pattern;
	IndexMask indexMask;
	BitSet activeIndices;
	int maxIterations;
	boolean render;
	float diff_scale = 1;
	
	public ProcessingStepImpl(DataDescriptor dataDescriptor) {
		int chunkSize = dataDescriptor.getChunkSize();
		BitSet activeIndices = new BitSet(chunkSize*chunkSize);
		activeIndices.set(0, activeIndices.size());
		configure(defaultPattern, defaultIndexMask, activeIndices, dataDescriptor.getMaxIterations());
	}
	
	public ProcessingStepImpl(DataDescriptor dataDescriptor, Pattern pattern) {
		int chunkSize = dataDescriptor.getChunkSize();
		BitSet activeIndices = new BitSet(chunkSize*chunkSize);
		activeIndices.set(0, activeIndices.size());
		configure(pattern, defaultIndexMask, activeIndices, dataDescriptor.getMaxIterations());
	}
	
	public ProcessingStepImpl(DataDescriptor dataDescriptor, IndexMask mask) {
		int chunkSize = dataDescriptor.getChunkSize();
		BitSet activeIndices = new BitSet(chunkSize*chunkSize);
		activeIndices.set(0, activeIndices.size());
		configure(defaultPattern, mask, activeIndices, dataDescriptor.getMaxIterations());
	}
	
	public ProcessingStepImpl(DataDescriptor dataDescriptor, Pattern pattern, IndexMask mask) {
		int chunkSize = dataDescriptor.getChunkSize();
		BitSet activeIndices = new BitSet(chunkSize*chunkSize);
		activeIndices.set(0, activeIndices.size());
		configure(pattern, mask, activeIndices, dataDescriptor.getMaxIterations());
	}

	public ProcessingStepImpl(DataDescriptor dataDescriptor, IndexMask mask, BitSet activeIndices) {
		configure(defaultPattern, mask, activeIndices, dataDescriptor.getMaxIterations());
	}
	
	public ProcessingStepImpl(DataDescriptor dataDescriptor, Pattern pattern, IndexMask mask, BitSet activeIndices) {
		configure(pattern, mask, activeIndices, dataDescriptor.getMaxIterations());
	}

	private void configure(Pattern pattern, IndexMask indexMask, BitSet activeIndices, int maxIterations) {
		this.pattern = pattern;
		this.indexMask = indexMask;
		this.activeIndices = activeIndices;
		this.maxIterations = maxIterations;
	}
	
	@Override
	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public IndexMask getIndexMask() {
		return indexMask;
	}

	@Override
	public BitSet getActiveIndices() {
		return activeIndices;
	}

	@Override
	public int getMaxIterations() {
		return maxIterations;
	}

	@Override
	public float getDiffScale() {
		return diff_scale;
	}
	
	public void setDiffScale(float diff_scale){
		this.diff_scale = diff_scale;
	}

}
