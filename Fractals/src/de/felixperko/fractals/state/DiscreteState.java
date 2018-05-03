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

	public void setIncrementable(boolean incrementable) {
		this.incrementable = incrementable;
	}

	public boolean isDecrementable() {
		return decrementable;
	}

	public void setDecrementable(boolean decrementable) {
		this.decrementable = decrementable;
	}

	public abstract T getNext();
	public abstract T getPrevious();
}
