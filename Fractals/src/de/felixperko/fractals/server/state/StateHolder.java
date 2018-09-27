package de.felixperko.fractals.server.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StateHolder {
	
	Map<String, State<?>> states = new HashMap<>();
	List<State<?>> statesFIFO = new ArrayList<State<?>>();
	
	public StateHolder() {
		stateSetup();
	}
	
	protected abstract void stateSetup();
	
	public State<?> getState(String name) {
		return states.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> State<T> getState(String name, Class<T> valueCls) {
		try {
			return (State<T>) states.get(name);
		} catch (ClassCastException e){
			return null;
		}
	}
	
	public void addState(State<?> state) {
		states.put(state.getName(), state);
		statesFIFO.add(state);
	}
	
	@SuppressWarnings("unchecked")
	public <U> Map<String, State<U>> getStatesForValueType(Class<U> valueClass) {
		Map<String, State<U>> map = new HashMap<>();
		states.entrySet().stream().filter(e -> valueClass.isInstance(e.getValue().getValue())).forEach(e -> map.put(e.getKey(), (State<U>) e.getValue()));
		return map;
	}

	public List<State<?>> getStates() {
		return statesFIFO;
	}
}
