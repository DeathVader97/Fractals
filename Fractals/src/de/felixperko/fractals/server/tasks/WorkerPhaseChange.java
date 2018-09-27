package de.felixperko.fractals.server.tasks;

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
}
