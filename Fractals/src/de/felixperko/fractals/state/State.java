package de.felixperko.fractals.state;

import java.util.ArrayList;
import java.util.List;

public class State<T> {

	//TODO introduce UI interfaces to separate state value restrictions from ui restrictions
	
	List<StateListener<T>> listeners = new ArrayList<>();
	
	String name;
	
	T value;
	
	boolean setable;
	boolean configurable;
	boolean visible = true;
	
	public State(String name, T value) {
		this.name = name;
		this.value = value;
		this.setable = false;
		this.configurable = false;
	}

	public State<T> addStateListener(StateListener listener) {
		listeners.add(listener);
		return this;
	}
	
	public boolean removeStateListener(StateListener<T> listener) {
		return listeners.remove(listener);
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		listeners.forEach(l -> l.valueChanged(this.value, value));
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public boolean isSetable() {
		return setable;
	}

	public void setSetable(boolean setable) {
		this.setable = setable;
	}

	public boolean isConfigurable() {
		return configurable;
	}

	public void setConfigurable(boolean configurable) {
		this.configurable = configurable;
	}
	
	public String getValueString(){
		return getStringOutput(value);
	}
	
	/**
	 * The Output of getValueString() can be customized by overriding this method.
	 * Normally just does value.toString()
	 * @param value
	 * @return
	 */
	protected String getStringOutput(T value) {
		return getOutput().toString();
	}

	/**
	 * The Output of getOutput() can be customized by overriding this method.
	 * Normally just does value.toString()
	 * @param value
	 * @return
	 */
	public Object getOutput() {
		return getValue();
	}

	public boolean isVisible() {
		return visible;
	}

	public State<T> setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}
}
