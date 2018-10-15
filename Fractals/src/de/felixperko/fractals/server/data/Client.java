package de.felixperko.fractals.server.data;

import de.felixperko.fractals.server.network.ClientConnection;
import de.felixperko.fractals.server.util.Position;

public class Client {

	ClientConfiguration config;
	ClientConnection connection;
	View view;
	
	public Client(ClientConnection connection, ClientConfiguration configuration) {
		this.connection = connection;
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
		
		view.removeClientId(getId());
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
		
		view.removeClientId(getId());
		view = null;
		
		Domain domain = instance.getApplicableDomain(config);
		view = domain.getApplicableView(config);
		
		config.update_domain = false;
		config.update_view = false;
	}

	private void updateView() {
		view.updateParameters(this);
		config.update_view = false;
	}

	public int getId() {
		return connection.getSenderInfo().getClientId();
	}

	public ClientConfiguration getConfig() {
		return config;
	}
	
	public ClientConnection getConnection() {
		return connection;
	}
	
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public void updatePosition(Position min, Position max) {
		//TODO update client position
	}
}
