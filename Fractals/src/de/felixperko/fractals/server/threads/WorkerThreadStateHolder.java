package de.felixperko.fractals.server.threads;

import de.felixperko.fractals.server.state.State;
import de.felixperko.fractals.server.state.StateHolder;

public class WorkerThreadStateHolder extends StateHolder {

	WorkerThread thread;
	
	State<Integer> stateIterationsPerSecond;

	public WorkerThreadStateHolder(WorkerThread thread) {
		super();
		this.thread = thread;
	}

	@Override
	protected void stateSetup() {
		stateIterationsPerSecond = new State<Integer>("iterations_per_second", 0);
		addState(stateIterationsPerSecond);
	}

	public State<Integer> getStateIterationsPerSecond() {
		return stateIterationsPerSecond;
	}
}
