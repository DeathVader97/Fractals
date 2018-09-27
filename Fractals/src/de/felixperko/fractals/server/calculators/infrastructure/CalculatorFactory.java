package de.felixperko.fractals.server.calculators.infrastructure;

import java.lang.reflect.InvocationTargetException;

import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.tasks.Task;

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
