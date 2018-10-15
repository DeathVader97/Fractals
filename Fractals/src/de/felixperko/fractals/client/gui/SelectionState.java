package de.felixperko.fractals.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.server.state.State;

public class SelectionState<T> extends State<T> {

	private static final long serialVersionUID = 3091608516380158765L;
	
	List<T> options = new ArrayList<>();

	public SelectionState(String name, T value) {
		super(name, value);
		setShowValueLabel(false);
	}
	
	public void addOptions(List<T> options) {
		this.options.addAll(options);
	}

	public String[] getOptionNames() {
		String[] optionNames = new String[options.size()];
		for (int i = 0 ; i < optionNames.length ; i++) {
			optionNames[i] = getName(options.get(i));
		}
		return optionNames;
	}

	public String getName(T obj) {
		return obj.toString();
	}

	public T getOption(int optionIndex) {
		return options.get(optionIndex);
	}

	public int getCurrentSelectionIndex() {
		if (options.isEmpty())
			return 0;
		T val = getValue();
		int i = 0;
		for (T option : options) {
			if (option.equals(val))
				return i;
			i++;
		}
		return 0;
	}

}
