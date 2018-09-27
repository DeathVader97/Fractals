package de.felixperko.fractals.server.steps;

import de.felixperko.fractals.server.data.ProcessingStepState;

public interface StepProvider {
	
	public ProcessingStep getStep(int state);
	public int getMaxState();
	public void incrementStepState(ProcessingStepState processingStepState);
}
