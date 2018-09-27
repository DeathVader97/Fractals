package de.felixperko.fractals.server.data;

import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.steps.StepProvider;

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

	public ProcessingStep getNextProcessingStep() {
		return stepProvider.getStep(stateNumber+1);
	}
	
	public ProcessingStepState increment() {
		if (stateNumber == stepProvider.getMaxState())
			throw new IllegalStateException("The final state can't be incremented!");
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
