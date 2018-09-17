package de.felixperko.fractals.state;

public abstract class DiscreteState<T> extends State<T> {
	
	boolean incrementable;
	boolean decrementable;
	
	public DiscreteState(String name, T value) {
		super(name, value);
	}
	
	public boolean setNext() {
		T next = getNext();
		if (next == null)
			return false;
		setValue(next);
		return true;
	}
	
	public boolean setPrevious() {
		T prev = getPrevious();
		if (prev == null)
			return false;
		setValue(prev);
		return true;
	}
	
	public boolean isIncrementable() {
		return incrementable;
	}

	public DiscreteState<T> setIncrementable(boolean incrementable) {
		this.incrementable = incrementable;
		return this;
	}

	public boolean isDecrementable() {
		return decrementable;
	}

	public DiscreteState<T> setDecrementable(boolean decrementable) {
		this.decrementable = decrementable;
		return this;
	}

	public abstract T getNext();
	public abstract T getPrevious();

	public void incrementValue() {
		setValue(getNext());
	}
	
	public void decrementValue() {
		setValue(getPrevious());
	}
}
