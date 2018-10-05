package de.felixperko.fractals.server.data;

import java.util.HashSet;
import java.util.Set;

import de.felixperko.fractals.server.util.Position;

/**
 * A view contains the associated clients (usually one) and is linked to a Domain
 */
public class View {
	
	Domain domain;
	Set<Integer> clientIds = new HashSet<>();
	Position min, max;
	Grid grid;
	boolean disposed;
	
	public View(Position min, Position max) {
		this.min = min;
		this.max = max;
	}
	
	public void addClientId(int id) {
		this.clientIds.add(id);
	}
	
	public void removeClientId(int id) {
		this.clientIds.remove(id);
		if (clientIds.isEmpty())
			domain.removeView(this);
	}
	
	public boolean hasClientConnected(int clientId) {
		return clientIds.contains(clientId);
	}
	
	public boolean isApplicable(Position min, Position max) {
		return this.min.equals(min) && this.max.equals(max);
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public int getClientCount() {
		return clientIds.size();
	}
	
	public void setParameters(Position min, Position max) {
	}
	
	public boolean isDisposed() {
		return disposed;
	}
	
	public void dispose() {
		disposed = true;
		domain.viewDisposed(this);
	}

	public boolean contains(Chunk c) {
		// TODO
		return false;
	}

	public void addClient(Client client) {
		addClientId(client.getId());
	}

	public void setParameters(ClientConfiguration configuration) {
		this.min = configuration.getSpaceMin();
		this.max = configuration.getSpaceMax();
		//TODO update clients and chunks
		for (int id : clientIds) {
			Client c = domain.instance.dataContainer.getClient(id);
			c.updatePosition(min, max);
		}
		domain.updateChunks();
	}
}
