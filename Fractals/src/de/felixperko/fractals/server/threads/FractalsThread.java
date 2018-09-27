package de.felixperko.fractals.server.threads;

import java.awt.Color;
import java.util.ArrayList;

import de.felixperko.fractals.server.tasks.WorkerPhase;
import de.felixperko.fractals.server.tasks.WorkerPhaseChange;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.performance.PerformanceMonitor;

public abstract class FractalsThread extends Thread {

	protected static WorkerPhase PHASE_IDLE = new WorkerPhase("Idle", Color.GRAY);
	protected static WorkerPhase PHASE_WAITING = new WorkerPhase("Waiting", Color.YELLOW);
	protected static WorkerPhase PHASE_WORKING = new WorkerPhase("Working", Color.BLUE);
	protected static WorkerPhase PHASE_STOPPED = new WorkerPhase("Stopped", Color.RED);
	
	protected static WorkerPhase DEFAULT_PHASE = PHASE_IDLE;
	WorkerPhase phase = DEFAULT_PHASE;
	
	ArrayList<WorkerPhaseChange> phaseChanges = new ArrayList<>();
	
	PerformanceMonitor monitor;

	protected CategoryLogger log;

	public FractalsThread(String name, int priority) {
		setName(name);
//		setPriority(priority);
		log = CategoryLogger.INFO.createSubLogger("threads/"+getName());
	}
	
	public void setPhase(WorkerPhase phase) {
		if (phase == this.phase)
			return;
		this.phase = phase;
		phaseChanges.add(new WorkerPhaseChange(phase));
	}
	
	public WorkerPhase getPhase() {
		return phase;
	}
}
