package de.felixperko.fractals.server.data;

import de.felixperko.fractals.server.network.ClientInfo;

public class Client {

	int id;
	ClientConfiguration config;
	ClientInfo info;
	View view;
	
	public Client(int id, ClientConfiguration configuration) {
		this.id = id;
		this.config = configuration;
	}
	
	public void configurationUpdated(ClientConfiguration newConfig) {
		this.config = newConfig;
		if (newConfig.update_instance)
			updateInstance();
		else if (newConfig.update_domain)
			updateDomain();
		else if (newConfig.update_view)
			updateView();
	}

	private void updateInstance() {
		DataContainer container = view.domain.instance.dataContainer;
		
		view.removeClientId(id);
		view = null;
		
		Instance instance = container.getApplicableInstance(config);
		Domain domain = instance.getApplicableDomain(config);
		view = domain.getApplicableView(config);
		
		config.update_instance = false;
		config.update_domain = false;
		config.update_view = false;
	}

	private void updateDomain() {
		Instance instance = view.domain.instance;
		
		view.removeClientId(id);
		view = null;
		
		Domain domain = instance.getApplicableDomain(config);
		view = domain.getApplicableView(config);
		
		config.update_domain = false;
		config.update_view = false;
	}

	private void updateView() {
		view.setParameters(config);
		config.update_view = false;
	}

	public int getId() {
		return id;
	}

	public ClientConfiguration getConfig() {
		return config;
	}
	
	public ClientInfo getClientInfo() {
		return info;
	}
	
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
}
