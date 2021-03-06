package de.felixperko.fractals.state;

import java.util.ArrayList;
import java.util.List;

public class StateChangeListener<T> extends StateListener<T> {
	
	boolean changed = false;
	State<T> state;
	
	List<StateChangeAction> actions = new ArrayList<>();
	
	public StateChangeListener(State<T> state) {
		this.state = state;
	}

	@Override
	public void valueChanged(T oldValue, T newValue) {
		changed = true;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public T getValue(boolean resetChanged) {
		if (resetChanged)
			changed = false;
		return state.getValue();
	}

	public void updateIfChanged(boolean resetChanged) {
		if (!changed)
			return;
		if (resetChanged)
			changed = false;
		actions.forEach(a -> a.update());
	}
	
	public void addStateChangeAction(StateChangeAction action) {
		actions.add(action);
	}
}
