package de.felixperko.fractals.server.threads;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.client.gui.PhaseProgressionCanvas;
import de.felixperko.fractals.server.tasks.WorkerPhase;
import de.felixperko.fractals.server.tasks.WorkerPhaseChange;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.performance.PerformanceMonitor;

public abstract class FractalsThread extends Thread implements PerformanceThread {
	WorkerPhase phase = DEFAULT_PHASE;
	
	List<WorkerPhaseChange> phaseChanges = new ArrayList<>();
	List<PhaseProgressionCanvas> phaseProgressionCanvases = new ArrayList<>();
	
	PerformanceMonitor monitor;

	protected CategoryLogger log;

	public FractalsThread(String name, int priority) {
		setName(name);
//		setPriority(priority);
		log = CategoryLogger.INFO.createSubLogger("threads/"+getName());
	}
	
	@Override
	public void setPhase(WorkerPhase phase) {
		if (phase == this.phase)
			return;
		this.phase = phase;
		phaseChanges.add(new WorkerPhaseChange(phase));
		for (PhaseProgressionCanvas canvas : phaseProgressionCanvases) {
			if (!canvas.isDisposed()){
				canvas.getDisplay().asyncExec(() -> {
					if (!canvas.isDisposed()){
						canvas.setRedraw(true);
						canvas.update();
					}
				});
			}
		}
	}
	
	@Override
	public WorkerPhase getPhase() {
		return phase;
	}
	
	@Override
	public List<WorkerPhaseChange> getPhaseChanges(){
		return phaseChanges;
	}
	
	@Override
	public void setPhaseProgressionCanvas(PhaseProgressionCanvas canvas) {
		phaseProgressionCanvases.add(canvas);
		canvas.setPhaseChanges(getPhaseChanges());
	}
	
	@Override
	public void removePhaseProgressionCanvas(PhaseProgressionCanvas canvas) {
		phaseProgressionCanvases.remove(canvas);
	}
}
