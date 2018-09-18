package de.felixperko.fractals.Tasks.steps;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.felixperko.fractals.Tasks.steps.patternprovider.BasicPatternProvider;
import de.felixperko.fractals.Tasks.steps.patternprovider.Pattern;
import de.felixperko.fractals.Tasks.steps.patternprovider.PatternProvider;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.IndexMask;
import de.felixperko.fractals.data.UpsampleMask;
import de.felixperko.fractals.data.ProcessingStepState;

public class DefaultStepProvider implements StepProvider{
	
	PatternProvider patternProvider;
	List<ProcessingStep> steps = new ArrayList<>();
	
	public DefaultStepProvider(DataDescriptor dataDescriptor) {
		this.patternProvider = new BasicPatternProvider(100, 10);
		
//		int size = dataDescriptor.getChunkSize();
//		float sizef  = size;
//		float half_sizef = sizef/2;
//		int[] indices = new int[4];
//		indices[0] = Chunk.getIndex(Math.round(sizef/3), Math.round(sizef/3), size);
//		indices[1] = Chunk.getIndex(Math.round(sizef*2/3), Math.round(sizef/3), size);
//		indices[2] = Chunk.getIndex(Math.round(sizef/3), Math.round(sizef*2/3), size);
//		indices[3] = Chunk.getIndex(Math.round(sizef*2/3), Math.round(sizef*2/3), size);
//		BitSet bitSet = new BitSet(size*size);
//		for (int i = 0 ; i < indices.length ; i++) {
//			bitSet.set(indices[i]);
//		}
//		IndexMask mask = new IndexMask() {
//			
//			@Override
//			public double getWeight() {
//				return 1;
//			}
//			
//			@Override
//			public int getIndex(int i) {
//				int x = i / size;
//				int y = i % size;
//				if (x < half_sizef) {
//					if (y < half_sizef)
//						return indices[0];
//					return indices[1];
//				}
//				if (y < half_sizef)
//					return indices[2];
//				return indices[3];
//			}
//		};
//		ProcessingStepImpl probeStep = new ProcessingStepImpl(dataDescriptor, mask, bitSet);
//		steps.add(probeStep);
//		
		ProcessingStepImpl step1 = getUpsamplingStep(dataDescriptor, 4);
//		ProcessingStepImpl step2 = getUpsamplingStep(dataDescriptor, 2);
//		step2.activeIndices.andNot(step1.activeIndices);
		steps.add(step1);
//		steps.add(step2);
		
		
		for (Pattern pattern : patternProvider.getPatterns()) {
			steps.add(new ProcessingStepImpl(dataDescriptor, pattern));
		}
	}
	
	private ProcessingStepImpl getUpsamplingStep(DataDescriptor dataDescriptor, int scaling) {
		int chunkSize = dataDescriptor.getChunkSize();
		IndexMask mask = new UpsampleMask(dataDescriptor, scaling);
		BitSet bitSet = new BitSet(chunkSize*chunkSize);
		int index = 0;
		for (int x = 0 ; x < chunkSize ; x++) {
			for (int y = 0 ; y < chunkSize ; y++) {
				if (x % scaling == 0 && y % scaling == 0)
					bitSet.set(index);
				index++;
			}
		}
		return new ProcessingStepImpl(dataDescriptor, mask, bitSet);
	}

	@Override
	public ProcessingStep getStep(int state) {
		if (state < 0 || state > getMaxState())
			throw new IllegalArgumentException("Out of bounds");
		return steps.get(state);
	}
	
	@Override
	public int getMaxState() {
		return steps.size()-1;
	}

	@Override
	public void incrementStepState(ProcessingStepState processingStepState) {
		processingStepState.setStateNumber(processingStepState.getStateNumber()+1);
	}

}
