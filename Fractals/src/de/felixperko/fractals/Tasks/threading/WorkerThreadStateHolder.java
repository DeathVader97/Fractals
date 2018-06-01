package de.felixperko.fractals.Tasks.threading;

import de.felixperko.fractals.state.State;
import de.felixperko.fractals.state.StateHolder;

public class WorkerThreadStateHolder extends StateHolder {

	WorkerThread thread;
	
	State<Integer> stateIterationsPerSecond;

	public WorkerThreadStateHolder(WorkerThread thread) {
		this.thread = thread;
		stateIterationsPerSecond = new State<Integer>("iterations_per_second", 0);
		addState(stateIterationsPerSecond);
	}

	public State<Integer> getStateIterationsPerSecond() {
		return stateIterationsPerSecond;
	}
}
