package de.felixperko.fractals.renderer.calculators.infrastructure;


import java.lang.reflect.InvocationTargetException;

import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.data.DataDescriptor;

public class CalculatorFactory {
	
	Class<? extends SampleCalculator> calculatorClass;
	DataDescriptor dataDescriptor;
	
	public CalculatorFactory(Class<? extends SampleCalculator> calculatorClass, DataDescriptor dataDescriptor) {
		this.calculatorClass = calculatorClass;
		this.dataDescriptor = dataDescriptor;
	}
	
	public SampleCalculator createCalculator(Task task) {
		try {
			return calculatorClass.getConstructor(DataDescriptor.class, Task.class).newInstance(dataDescriptor, task);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
}
