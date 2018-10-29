package de.felixperko.fractals.server.threads;

import java.awt.Color;
import java.util.List;

import de.felixperko.fractals.client.gui.PhaseProgressionCanvas;
import de.felixperko.fractals.server.tasks.WorkerPhase;
import de.felixperko.fractals.server.tasks.WorkerPhaseChange;

public interface PerformanceThread {
	
	static WorkerPhase PHASE_IDLE = new WorkerPhase("Idle", Color.GRAY);
	static WorkerPhase PHASE_WAITING = new WorkerPhase("Waiting", Color.YELLOW);
	static WorkerPhase PHASE_WORKING = new WorkerPhase("Working", Color.BLUE);
	static WorkerPhase PHASE_STOPPED = new WorkerPhase("Stopped", Color.RED);
	
	public static WorkerPhase DEFAULT_PHASE = PHASE_IDLE;
	
	void setPhase(WorkerPhase phase);

	WorkerPhase getPhase();

	List<WorkerPhaseChange> getPhaseChanges();

	void setPhaseProgressionCanvas(PhaseProgressionCanvas canvas);
	void removePhaseProgressionCanvas(PhaseProgressionCanvas canvas);

	String getName();

}