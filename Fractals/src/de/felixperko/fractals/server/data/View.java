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
	
	Position midPos;
	
	public View(Position min, Position max) {
		setParameters(min, max);
	}
	
	private void setParameters(Position min, Position max) {
		this.min = min;
		this.max = max;
		this.midPos = max.addNew(min).mult(0.5);
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
	
	public boolean isDisposed() {
		return disposed;
	}
	
	public void dispose() {
		disposed = true;
		domain.viewDisposed(this);
	}

	public boolean contains(Chunk c, int distanceLimit) {
		Position chunkPos = c.getStartPosition();
		Position delta = c.getDelta();
		return    (chunkPos.getX()-(delta.getX()*distanceLimit) <= max.getX()
				&& chunkPos.getX()+(delta.getX()*(distanceLimit+1)) >= min.getX()
				&& chunkPos.getY()-(delta.getY()*distanceLimit) <= max.getY()
			 	&& chunkPos.getY()+(delta.getY()*(distanceLimit+1)) >= min.getY());
	}

	public void addClient(Client client) {
		addClientId(client.getId());
	}

	public void updateParameters(Client client) {
		setParameters(client.config.getSpaceMin(), client.config.getSpaceMax());
		
		//TODO update clients and chunks
		for (int id : clientIds) {
			if (id == client.id)
				continue;
			Client c = getClient(id);
			c.updatePosition(min, max);
		}
		domain.updateChunks();
	}
	
	private Client getClient(int id) {
		return domain.instance.dataContainer.getClient(id);
	}

	public Position getMidSpacePosition() {
		return midPos;
	}
}
