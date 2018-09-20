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
		
		ProcessingStepImpl probeStep = getProbeStep(dataDescriptor, 2);
		steps.add(probeStep);
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
	
	private ProcessingStepImpl getProbeStep(DataDescriptor dataDescriptor, int dim) {
		int size = dataDescriptor.getChunkSize();
		int count = dim*dim;
		float step = size/(float)(dim*2);
		
		//get and enable active indices
		int[] indices = new int[count];
		int index = 0;
		BitSet bitSet = new BitSet(count);
		for (float x = step ; x < size-step ; x += step) {
			for (float y = step ; y < size-step ; y += step) {
				int val = Chunk.getIndex(Math.round(x), Math.round(y), size);
				indices[index] = val;
				bitSet.set(val);
				index++;
			}
		}
		
		//configure mask
		IndexMask mask = new IndexMask() {
			
			@Override
			public double getWeight() {
				return 1;
			}
			
			@Override
			public int getIndex(int i) {
				int x = i / size;
				int y = i % size;
				int x2 = (int) Math.floor(0.5*x/step);
				int y2 = (int) Math.floor(0.5*y/step);
				int i2 = x2*dim + y2;
				return indices[i2];
			}
		};
		return new ProcessingStepImpl(dataDescriptor, mask, bitSet);
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
