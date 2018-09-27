package de.felixperko.fractals.server.state;

public abstract class StateChangeAction {
	
	State<?> state;
	
	public State<?> getState() {
		return state;
	}


	public void setState(State<?> state) {
		this.state = state;
	}


	public abstract void update();
}
