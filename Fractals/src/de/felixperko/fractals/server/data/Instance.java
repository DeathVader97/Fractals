package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.server.calculators.infrastructure.CalculatorFactory;

/**
 * An instance of a fractal.
 * Keeps parameters for the calculation as well as (multiple) Domains which contain the currently active Chunks
 */
public class Instance {
	
	List<Domain> domains = new ArrayList<>();
	CalculatorFactory calculatorFactory;
	
	
	public void removeDomain(Domain domain) {
		domains.remove(domain);
		domain.disposeChunks();
	}
	
}
