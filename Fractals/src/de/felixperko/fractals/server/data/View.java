package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	
	public View(Domain domain, int clientId) {
		this.domain = domain;
		addClientId(clientId);
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
}
