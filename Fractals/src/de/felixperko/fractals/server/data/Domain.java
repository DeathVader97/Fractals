package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.server.util.Position;

/**
 * The Domain contains the associated Views and Chunks.
 */
public class Domain {
	
	Instance instance;

	int chunkSize;
	double chunkDimensions;
	
	List<View> views = new ArrayList<>();
	Map<Position, Chunk> chunks = new HashMap<>();
	
	public Domain(int chunkSize, double chunkDimensions) {
		this.chunkSize = chunkSize;
		this.chunkDimensions = chunkDimensions;
	}

	public View getApplicableView(ClientConfiguration config) {
		Position min = config.getSpaceMin();
		Position max = config.getSpaceMax();
		for (View view : views)
			if (view.isApplicable(min, max))
				return view;
		return addView(new View(min, max));
	}

	private View addView(View view) {
		views.add(view);
		view.setDomain(this);
		return view;
	}

	public void removeView(View view) {
		views.remove(view);
		if (views.isEmpty())
			instance.removeDomain(this);
	}
	
	public void viewDisposed(View view) {
		views.remove(view);
		Iterator<Position> it = chunks.keySet().iterator();
		next:
		while (it.hasNext()) {
			Position pos = it.next();
			Chunk c = chunks.get(pos);
			//if still contained in a view -> continue with next chunk
			for (View v : views) {
				if (v.contains(c))
					continue next;
			}
			//not contained in any active views -> dispose
			c.dispose();
			it.remove();
		}
	}

	public void disposeChunks() {
		for (Position pos : chunks.keySet()) {
			chunks.get(pos).dispose();
		}
		chunks.clear();
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}
	
	public boolean isApplicable(int chunkSize, double chunkDimensions) {
		return this.chunkDimensions == chunkDimensions && this.chunkSize == chunkSize;
	}
}
