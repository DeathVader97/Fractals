package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.server.calculators.infrastructure.SampleCalculator;

/**
 * An instance of a fractal.
 * Keeps parameters for the calculation as well as (multiple) Domains which contain the currently active Chunks
 */
public class Instance {
	
	DataContainer dataContainer;
	
	List<Domain> domains = new ArrayList<>();
	Class<? extends SampleCalculator> calculatorClass;
	
	public Instance(Class<? extends SampleCalculator> calculatorClass) {
		this.calculatorClass = calculatorClass;
	}
	
	/**
	 * returns a domain that fits the provided parameters of the client
	 * if no such domain exists one is created and added to the instance
	 * @param config - the ClientConfiguration
	 * @return
	 */
	public Domain getApplicableDomain(ClientConfiguration config) {
		int chunkSize = config.getChunkSize();
		double chunkDimensions = config.getChunkDimensions();
		for (Domain d : domains)
			if (d.isApplicable(chunkSize, chunkDimensions))
				return d;
		return addDomain(new Domain(chunkSize, chunkDimensions));
	}
	
	public void removeDomain(Domain domain) {
		domains.remove(domain);
		domain.dispose();
	}
	
	private Domain addDomain(Domain domain) {
		domains.add(domain);
		domain.setInstance(this);
		return domain;
	}
	
	public boolean isApplicable(Class<?> calculatorClass) {
		return this.calculatorClass.equals(calculatorClass);
	}

	public void setDataContainer(DataContainer dataContainer) {
		this.dataContainer = dataContainer;
	}
}
