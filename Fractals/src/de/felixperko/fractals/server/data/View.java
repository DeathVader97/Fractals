package de.felixperko.fractals.server.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.server.util.Position;

/**
 * A view contains the associated clients (usually one) and is linked to a Domain
 */
public class View {
	
	Domain domain;
	Set<Integer> clientIds = new HashSet<>();
	
	Grid grid;

	Position min, max;
	Position midPos;
	
	Map<Position, Chunk> chunks = new HashMap<>();
	
	boolean disposed;
	
	int dispose_distance_limit = 5;
	int calculation_distance_limit = 1;
	
	boolean updateGrid = false;
	
	public View(Position min, Position max) {
		setParameters(min, max);
	}
	
	private void setParameters(Position min, Position max) {
		if (this.max == null || !this.max.subNew(this.min).equals(max.subNew(min)))
			updateGrid = true;
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
		disposeChunks();
//		Iterator<Position> it = chunks.keySet().iterator();
//		next:
//		while (it.hasNext()) {
//			Position pos = it.next();
//			Chunk c = chunks.get(pos);
//			//if still contained in a view -> continue with next chunk
//			for (View v : domain.views) {
//				if (v.contains(c, dispose_distance_limit))
//					continue next;
//			}
//			//not contained in any active views -> dispose
//			c.dispose();
//			it.remove();
//		}
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
		
		//TODO update grid, client, chunks
		for (int id : clientIds) {
			if (id == client.getId())
				continue;
			Client c = getClient(id);
			c.updatePosition(min, max);
		}
		updateChunks();
	}
	
	private Client getClient(int id) {
		return domain.instance.dataContainer.getClient(id);
	}

	public Position getMidSpacePosition() {
		return midPos;
	}

	public void updateChunks() {
		
		Iterator<Chunk> it = chunks.values().iterator();
		//update distances and remove out of bounds chunks
		while (it.hasNext()) {
			Chunk c = it.next();
			Position spacePos = c.getStartPosition();
			double lowestDistance = Double.MAX_VALUE;
			for (View v : domain.views) {
				if (!v.contains(c, dispose_distance_limit)) {
					c.removeFromView(v);
					continue;
				}
				c.addToView(v);
				double distance = spacePos.distance(v.getMidSpacePosition());
				if (distance < lowestDistance)
					lowestDistance = distance;
			}
			if (lowestDistance == Double.MAX_VALUE) { //not in any view (anymore) -> dispose
				//TODO delay of removal necessary?
				c.dispose();
				it.remove();
			} else { //is in view, set distance
				c.setDistanceToMid(lowestDistance);
			}
		}
		domain.taskManager.setUpdatePriorities();
	}

	public void disposeChunks() {
		for (Position pos : chunks.keySet()) {
			Chunk c = chunks.get(pos);
			if (c.getViews().size() == 1 && c.getViews().contains(this))
				c.dispose();
		}
		chunks.clear();
	}

	public int getCalculationDistanceLimit() {
		return calculation_distance_limit;
	}
}
