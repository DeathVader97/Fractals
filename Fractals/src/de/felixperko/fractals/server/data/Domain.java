package de.felixperko.fractals.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.server.tasks.TaskManager;
import de.felixperko.fractals.server.util.Position;

/**
 * The Domain contains the associated Views and Chunks.
 * Chunks could be shared between different Views in the same Domain (currently not implemented).
 */
public class Domain {
	
	Instance instance;

	int chunkSize;
	double chunkDimensions;
	
	List<View> views = new ArrayList<>();
	
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
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}
	
	public boolean isApplicable(int chunkSize, double chunkDimensions) {
		return this.chunkDimensions == chunkDimensions && this.chunkSize == chunkSize;
	}

	public void dispose() {
		if (!views.isEmpty()) {
			for (View v : views)
				v.dispose();
		}
	}
}
