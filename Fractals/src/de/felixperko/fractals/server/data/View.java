package de.felixperko.fractals.server.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.network.messages.ChunkUpdateMessage;
import de.felixperko.fractals.server.tasks.ArrayListBatchTaskManager;
import de.felixperko.fractals.server.tasks.TaskManager;
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
	
	TaskManager taskManager;
	
	public View(Position min, Position max) {
		grid = new Grid();
		taskManager = FractalsMain.taskManager;
		grid.setTaskManager(taskManager);
		this.min = min;
		this.max = max;
	}

	public void updateParameters(Client client) {
		setParameters(client);
		
		//TODO update grid, client, chunks
		for (int id : clientIds) {
			if (id == client.getId())
				continue;
			Client c = getClient(id);
			c.updatePosition(min, max);
		}
		updateChunks(client);
	}
	
	private void setParameters(Client client) {
		Position newMin = client.config.getSpaceMin();
		Position newMax = client.config.getSpaceMax();
//		Position deltaOld = this.max.subNew(this.min);
//		Position deltaNew = newMax.subNew(newMin);
//		double deltaChange = deltaNew.length()-deltaOld.length();
		if (this.max == null)
			updateGrid = true;
		this.min = newMin;
		this.max = newMax;
		grid.setSpaceOffset(newMin);
		if (updateGrid) {
			grid.reset();
		}
		updateChunks(client);
		this.midPos = newMax.addNew(newMin).mult(0.5);
	}

	public void addClientId(int id) {
		this.clientIds.add(id);
	}
	
	public void removeClientId(int id) {
		this.clientIds.remove(id);
		if (clientIds.isEmpty()) {
			//dispose chunks
			for (Chunk c : chunks.values()) {
				c.removeFromView(this);
				if (c.inViews.isEmpty())
					c.dispose();
			}
			domain.removeView(this);
		}	
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
		setParameters(client);
	}
	
	private Client getClient(int id) {
		return domain.instance.dataContainer.getClient(id);
	}

	public Position getMidSpacePosition() {
		return midPos;
	}

	public void updateChunks(Client client) {
		
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
		
		createMissingChunks(client);
		taskManager.setUpdatePriorities();
	}

	private void createMissingChunks(Client client) {
		Position drawDimensions = client.getConfig().getDrawDimensions();
		Position min = grid.getGridPosition(new Position(0,0));
		Position max = grid.getGridPosition(drawDimensions);
		for (long x = (long)min.getX() ; x <= Math.ceil(max.getX()) ; x++) {
			for (long y = (long)min.getY() ; y <= Math.ceil(max.getY()) ; y++) {
				Position gridPos = grid.getPosition(x,y);
				Chunk c = grid.getChunkOrNull(gridPos);
				if (c != null)
					continue;
				c = grid.getChunk(gridPos);
				for (View v : domain.views) {
					if (v.contains(c, dispose_distance_limit)) {
						c.addToView(this);
						taskManager.addChunk(c);
					}
				}
			}
		}
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

	public Set<Integer> getClientIds() {
		return clientIds;
	}
}
