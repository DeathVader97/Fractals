package de.felixperko.fractals.server.network.messages;

import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.data.ClientConfiguration;
import de.felixperko.fractals.server.network.Message;

public class UpdateConfigurationMessage extends Message {

	private static final long serialVersionUID = 1476570289262051108L;
	
	ClientConfiguration configuration;

	public UpdateConfigurationMessage(ClientConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	protected void process() {
		log.log("updating configuration: view:"+configuration.isUpdate_view()+" domain:"+configuration.isUpdate_domain()+" instance:"+configuration.isUpdate_instance());
		FractalsServerMain.dataContainer.getClient(connection.getSenderInfo().getClientId()).configurationUpdated(configuration);
	}

}
