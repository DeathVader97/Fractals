package de.felixperko.fractals.data;

import de.felixperko.fractals.Tasks.steps.ProcessingStep;
import de.felixperko.fractals.Tasks.steps.StepProvider;
import de.felixperko.fractals.Tasks.steps.patternprovider.BasicPatternProvider;
import de.felixperko.fractals.Tasks.steps.patternprovider.Pattern;
import de.felixperko.fractals.Tasks.steps.patternprovider.PatternProvider;

public class DefaultStepProvider implements StepProvider{
	
	PatternProvider patternProvider;
	
	public DefaultStepProvider(PatternProvider patternProvider) {
		this.patternProvider = patternProvider;
	}
	
	@Override
	public ProcessingStep getStep(int state) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getMaxState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ProcessingStepState incrementStepState(ProcessingStepState processingStepState) {
		return null;
	}

}
