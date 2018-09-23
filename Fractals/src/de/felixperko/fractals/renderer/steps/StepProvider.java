package de.felixperko.fractals.renderer.steps;

import de.felixperko.fractals.data.ProcessingStepState;

public interface StepProvider {
	
	public ProcessingStep getStep(int state);
	public int getMaxState();
	public void incrementStepState(ProcessingStepState processingStepState);
}