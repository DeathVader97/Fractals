package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.server.util.Position;

/**
 * The Domain contains the associated Views and Chunks.
 */
public class Domain {
	
	Instance instance;
	double chunkDimensions;
	List<View> views = new ArrayList<>();
	Map<Position, Chunk> chunks = new HashMap<>();
	
	public Domain(Instance instance, double chunkDimensions, int firstClientId) {
	}

	public void removeView(View view) {
		views.remove(view);
		if (views.isEmpty())
			instance.removeDomain(this);
	}
	
	public void disposeChunks() {
		//TODO
	}
}
