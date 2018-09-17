package de.felixperko.fractals.data;

import de.felixperko.fractals.Tasks.steps.ProcessingStep;
import de.felixperko.fractals.Tasks.steps.StepProvider;

public class ProcessingStepState {
	
	StepProvider stepProvider;
	int state;
	
	/**
	 * creates a default pattern state (id = -1)
	 * @param patternProvider
	 */
	public ProcessingStepState(StepProvider stepProvider) {
		this.stepProvider = stepProvider;
		state = -1;
	}
	
	public ProcessingStepState(StepProvider stepProvider, int state) {
		this.stepProvider = stepProvider;
		this.state = state;
	}
	
	public ProcessingStep getProcessingStep() {
		return stepProvider.getStep(state);
	}
	
	public ProcessingStepState increment() {
		return stepProvider.incrementStepState(this);
	}

	public ProcessingStepState copy() {
		return new ProcessingStepState(stepProvider, state);
	}

	public boolean isDefaultState() {
		return state == -1;
	}

	public int getId() {
		return state;
	}
}
