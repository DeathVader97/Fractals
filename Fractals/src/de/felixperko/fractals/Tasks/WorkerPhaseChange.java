package de.felixperko.fractals.Tasks;

public class WorkerPhaseChange {
	
	WorkerPhase phase;
	long time;
	
	public WorkerPhaseChange(WorkerPhase newPhase) {
		this.phase = newPhase;
		this.time = System.nanoTime();
	}
}
