package de.felixperko.fractals.Tasks.threading;

import java.awt.Color;
import java.util.ArrayList;

import de.felixperko.fractals.Tasks.WorkerPhase;
import de.felixperko.fractals.Tasks.WorkerPhaseChange;
import de.felixperko.fractals.Tasks.perf.PerformanceMonitor;
import de.felixperko.fractals.util.CategoryLogger;

public class FractalsThread extends Thread {

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
