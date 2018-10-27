package de.felixperko.fractals.server.tasks;

import org.eclipse.swt.graphics.Color;

public class WorkerPhaseChange {
	
	WorkerPhase phase;
	long time;
	
	public WorkerPhaseChange(WorkerPhase newPhase) {
		this.phase = newPhase;
		this.time = System.nanoTime();
	}

	public WorkerPhase getPhase() {
		return phase;
	}

	public long getTime() {
		return time;
	}

	public Color getSwtColor() {
		return phase.getSwtColor();
	}
}
