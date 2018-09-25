package de.felixperko.fractals.data;

import de.felixperko.fractals.renderer.steps.ProcessingStep;
import de.felixperko.fractals.renderer.steps.StepProvider;

public class ProcessingStepState {
	
	StepProvider stepProvider;
	int stateNumber;
	
	/**
	 * creates a default pattern state (id = -1)
	 * @param patternProvider
	 */
	public ProcessingStepState(StepProvider stepProvider) {
		this.stepProvider = stepProvider;
		stateNumber = -1;
	}
	
	public ProcessingStepState(StepProvider stepProvider, int stateNumber) {
		this.stepProvider = stepProvider;
		this.stateNumber = stateNumber;
	}
	
	public ProcessingStep getProcessingStep() {
		return stepProvider.getStep(stateNumber);
	}
	
	public ProcessingStepState increment() {
		stepProvider.incrementStepState(this);
		return this;
	}

	public ProcessingStepState copy() {
		return new ProcessingStepState(stepProvider, stateNumber);
	}

	public boolean isDefaultState() {
		return stateNumber == -1;
	}

	public int getStateNumber() {
		return stateNumber;
	}

	public void setStateNumber(int stateNumber) {
		this.stateNumber = stateNumber;
	}

	public StepProvider getStepProvider() {
		return stepProvider;
	}
}
