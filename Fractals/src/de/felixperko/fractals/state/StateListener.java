package de.felixperko.fractals.state;

public abstract class StateListener<T> {

	public abstract void valueChanged(T oldValue, T newValue);
}
