package de.felixperko.fractals.renderer.steps;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.IndexMask;
import de.felixperko.fractals.data.UpsampleMask;
import de.felixperko.fractals.renderer.steps.patternprovider.BasicPatternProvider;
import de.felixperko.fractals.renderer.steps.patternprovider.Pattern;
import de.felixperko.fractals.renderer.steps.patternprovider.PatternProvider;
import de.felixperko.fractals.data.ProcessingStepState;

public class DefaultStepProvider implements StepProvider{
	
	static ProcessingStep DEFAULT_STEP = new DefaultProcessingStep();
	
	PatternProvider patternProvider;
	List<ProcessingStep> steps = new ArrayList<>();
	
	public DefaultStepProvider(DataDescriptor dataDescriptor) {
		this.patternProvider = new BasicPatternProvider(100, 10);
		
//		ProcessingStepImpl probeStep = getProbeStep(dataDescriptor, 8);
//		steps.add(probeStep);
		
		ProcessingStepImpl step1 = getUpsamplingStep(dataDescriptor, 8);
		steps.add(step1);
//		ProcessingStepImpl step2 = getUpsamplingStep(dataDescriptor, 4);
//		steps.add(step2)
//		ProcessingStepImpl step3 = getUpsamplingStep(dataDescriptor, 2);;
//		steps.add(step3);
		
		
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
		int i = 0;
		BitSet bitSet = new BitSet(count);
		for (int xi = 0 ; xi < dim ; xi++) {
			for (int yi = 0 ; yi < dim ; yi++) {
				int x = (int) (step*(xi+1));
				int y = (int) (step*(yi+1));
				int index = Chunk.getIndex(Math.round(x), Math.round(y), size);
				indices[i] = index;
				bitSet.set(index);
				i++;
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
				int x2 = (int) Math.floor((x*0.5)/step);
				int y2 = (int) Math.floor((y*0.5)/step);
				int i2 = x2*dim + y2;
				return indices[i2];
			}
		};
		ProcessingStepImpl processingStep = new ProcessingStepImpl(dataDescriptor, mask, bitSet);
		processingStep.setDiffScale(1/(float)dim);
		processingStep.setNeigbourOffset(Math.round(step*2));
		processingStep.setDrawable(false);
		processingStep.setProbeStep(true);
		processingStep.setActiveCount(dim*dim);
		return processingStep;
	}

	private ProcessingStepImpl getUpsamplingStep(DataDescriptor dataDescriptor, int scaling) {
		int chunkSize = dataDescriptor.getChunkSize();
		IndexMask mask = new UpsampleMask(dataDescriptor, scaling);
		BitSet bitSet = new BitSet(chunkSize*chunkSize/scaling);
		int index = 0;
		for (int x = 0 ; x < chunkSize ; x++) {
			for (int y = 0 ; y < chunkSize ; y++) {
				if (x % scaling == 0 && y % scaling == 0)
					bitSet.set(index);
				index++;
			}
		}
		ProcessingStepImpl processingStep = new ProcessingStepImpl(dataDescriptor, mask, bitSet);
		processingStep.setDiffScale(1/(float)scaling);
		processingStep.setNeigbourOffset(scaling);
		processingStep.setActiveCount(chunkSize*chunkSize/(scaling*scaling));
		return processingStep;
	}

	@Override
	public ProcessingStep getStep(int state) {
		if (state > getMaxState())
			throw new IllegalArgumentException("Out of bounds "+state);
		if (state == -1)
			return DEFAULT_STEP;
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
