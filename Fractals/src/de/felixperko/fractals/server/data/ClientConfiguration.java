package de.felixperko.fractals.server.data;

import java.io.Serializable;

import de.felixperko.fractals.server.calculators.infrastructure.SampleCalculator;
import de.felixperko.fractals.server.network.ClientConnection;
import de.felixperko.fractals.server.network.Connection;
import de.felixperko.fractals.server.network.ServerConnection;
import de.felixperko.fractals.server.network.messages.UpdateConfigurationMessage;
import de.felixperko.fractals.server.util.Position;

public class ClientConfiguration implements Serializable{
	
	private static final long serialVersionUID = 5797564389751134338L;

	Class<? extends SampleCalculator> calculatorClass;
	
	int chunkSize;
	double chunkDimensions;
	
	Position drawDimensions;
	
	Position spaceMin;
	Position spaceMax;
	
	boolean update_view;
	boolean update_domain;
	boolean update_instance;
	
	transient Connection connection;
	
	public ClientConfiguration(Class<? extends SampleCalculator> calculatorClass, int chunkSize,
			Position spaceMin, Position spaceMax, Position drawDimensions, Connection connection) {
		this.calculatorClass = calculatorClass;
		this.chunkSize = chunkSize;
		Position chunkCounts = drawDimensions.divNew(chunkSize);
		this.chunkDimensions = (spaceMax.getX()-spaceMin.getX())/chunkCounts.getX();
		this.spaceMin = spaceMin;
		this.spaceMax = spaceMax;
		this.drawDimensions = drawDimensions;
		this.connection = connection;
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
		if (connection != null)
			connection.writeMessage(new UpdateConfigurationMessage(this));
	}
	
	public void updateZoom() {
//		if (chunkDimensions == this.chunkDimensions)
//			return;
//		this.chunkDimensions = chunkDimensions;
		update_domain = true;
//		if (connection != null)
//			connection.writeMessage(new UpdateConfigurationMessage(this));
	}
	
	public void updateChunkSize(int chunkSize) {
		if (chunkSize == this.chunkSize)
			return;
		this.chunkSize = chunkSize;
		update_domain = true;
		if (connection != null)
			connection.writeMessage(new UpdateConfigurationMessage(this));
	}
	
	public void updateCalculatorParameters(Class<? extends SampleCalculator> calculatorClass) {
		if (calculatorClass == this.calculatorClass)
			return;
		this.calculatorClass = calculatorClass;
		update_instance = true;
		if (connection != null)
			connection.writeMessage(new UpdateConfigurationMessage(this));
	}

	public Position getDrawDimensions() {
		return drawDimensions;
	}

	public boolean isUpdate_view() {
		return update_view;
	}

	public boolean isUpdate_domain() {
		return update_domain;
	}

	public boolean isUpdate_instance() {
		return update_instance;
	}
}
