package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.server.calculators.infrastructure.SampleCalculator;

public class DataContainer {
	
	List<Instance> instances = new ArrayList<>();
	Map<Integer, Client> clients = new HashMap<>();
	
	public Instance getApplicableInstance(ClientConfiguration config) {
		Class<? extends SampleCalculator> calculatorClass = config.getCalculatorClass();
		for (Instance instance : instances) {
			if (instance.isApplicable(calculatorClass))
				return instance;
		}
		return addInstance(new Instance(calculatorClass));
	}

	private Instance addInstance(Instance instance) {
		instances.add(instance);
		instance.setDataContainer(this);
		return instance;
	}
	
	public void newClient(Client client) {
		ClientConfiguration config = client.getConfig();
		
		Instance inst = getApplicableInstance(config);
		Domain domain = inst.getApplicableDomain(config);
		View view = domain.getApplicableView(config);

		client.setView(view);
		view.addClient(client);
		clients.put(client.getId(), client);
	}
	
	public Client getClient(int id) {
		return clients.get(id);
	}
}
