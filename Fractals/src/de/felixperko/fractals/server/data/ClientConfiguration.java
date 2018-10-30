package de.felixperko.fractals.server.data;

import java.io.Serializable;

import de.felixperko.fractals.server.calculators.infrastructure.SampleCalculator;
import de.felixperko.fractals.server.util.Position;

public class ClientConfiguration implements Serializable{
	
	private static final long serialVersionUID = 5797564389751134338L;

	Class<? extends SampleCalculator> calculatorClass;
	
	int chunkSize;
	double chunkDimensions;
	
	Position spaceMin;
	Position spaceMax;
	
	boolean update_view;
	boolean update_domain;
	boolean update_instance;
	
	public ClientConfiguration(Class<? extends SampleCalculator> calculatorClass, int chunkSize, double chunkDimensions,
			Position spaceMin, Position spaceMax) {
		this.calculatorClass = calculatorClass;
		this.chunkSize = chunkSize;
		this.chunkDimensions = chunkDimensions;
		this.spaceMin = spaceMin;
		this.spaceMax = spaceMax;
	}

	public Class<? extends SampleCalculator> getCalculatorClass() {
		return calculatorClass;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public double getChunkDimensions() {
		return chunkDimensions;
	}

	public Position getSpaceMin() {
		return spaceMin;
	}

	public Position getSpaceMax() {
		return spaceMax;
	}
	
	public void updateShiftView(Position spaceMin, Position spaceMax) {
		if (spaceMin.equals(this.spaceMin) && spaceMax.equals(this.spaceMax))
			return;
		this.spaceMin = spaceMin;
		this.spaceMax = spaceMax;
		update_view = true;
	}
	
	public void updateZoom(double chunkDimensions) {
		if (chunkDimensions == this.chunkDimensions)
			return;
		this.chunkDimensions = chunkDimensions;
		update_domain = true;
	}
	
	public void updateChunkSize(int chunkSize) {
		if (chunkSize == this.chunkSize)
			return;
		this.chunkSize = chunkSize;
		update_domain = true;
	}
	
	public void updateCalculatorParameters(Class<? extends SampleCalculator> calculatorClass) {
		if (calculatorClass == this.calculatorClass)
			return;
		this.calculatorClass = calculatorClass;
		update_instance = true;
	}
}
