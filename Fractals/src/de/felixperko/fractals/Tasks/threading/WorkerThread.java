package de.felixperko.fractals.Tasks.threading;


import java.awt.Color;
import java.util.ArrayList;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.WorkerPhase;
import de.felixperko.fractals.Tasks.WorkerPhaseChange;
import de.felixperko.fractals.renderer.perf.PerformanceMonitor;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Logger;

public class WorkerThread extends FractalsThread {
	
	
	static int ID_COUNTER = 0;
	
	static WorkerPhase PHASE_NO_PROVIDER = new WorkerPhase("No provider", Color.ORANGE);
	static WorkerPhase PHASE_SAVING = new WorkerPhase("Saving", Color.CYAN);
	
	TaskProvider taskProvider;
	Task task;
	
	WorkerThreadStateHolder stateHolder = new WorkerThreadStateHolder(this);
	
	private long iterations = 0;

	boolean continueWorking = false;
	boolean end = false;
	
	public WorkerThread(TaskProvider taskProvider) {
		super("WT_"+ID_COUNTER++, 3);
		this.taskProvider = taskProvider;
	}
	
	double lastIterationsPerMs = 0;
	double[] last10IterationsPerS = new double[10];
	int last10IterationsIndex = 0;
	long taskFinishedTime = 0;
	
	@Override
	public void run() {
		log.log("started");
		
		workLoop :
		while (!end && (continueWorking || !Thread.interrupted())) {
			continueWorking = false;
			if (end)
				break workLoop;
			while (taskProvider == null || (task = taskProvider.getTask()) == null) {
				if (end)
					break workLoop;
				if (taskProvider == null)
					setPhase(PHASE_NO_PROVIDER);
				else
					setPhase(PHASE_WAITING);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					log.log("interrupted.");
				}
			}
			
			setPhase(PHASE_WORKING);
			
			if (!FractalsMain.taskManager.isJobActive(task.getJobId()))
				continue;
			
			TaskProvider tp = taskProvider;
			try {
				task.run(tp.dataDescriptor);
				taskFinishedTime = System.nanoTime();
				setLastIterationsPerMs(task.getSamplesPerMs());
				iterations = getIterations() + task.getEnd_Sample_Count();
				setPhase(PHASE_SAVING);
				tp.taskFinished(task);
			} catch (Exception e) {
				if (!FractalsMain.taskManager.isJobActive(task.getJobId()))
					continue;
				e.printStackTrace();
			}
		}
		setPhase(PHASE_STOPPED);
	}

	public void setTaskProvider(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
//		continueWorking = true;
//		interrupt();
	}
	
	private void setLastIterationsPerMs(double iterationsPerMs) {
		this.lastIterationsPerMs = iterationsPerMs;
		last10IterationsPerS[last10IterationsIndex] = (int)(lastIterationsPerMs*1000);
		last10IterationsIndex++;
		last10IterationsIndex %= last10IterationsPerS.length;
		double v = 0;
		for (double itps : last10IterationsPerS)
			v += itps;
		stateHolder.stateIterationsPerSecond.setValue((int)v);
	}

	public void resetPerformanceMonitor(PerformanceMonitor performanceMonitor) {
		monitor = performanceMonitor;
		phaseChanges.clear();
		iterations = 0;
		phaseChanges.add(new WorkerPhaseChange(phase));
	}
	
	public ArrayList<WorkerPhaseChange> getPerformanceData(){
		return new ArrayList<WorkerPhaseChange>(phaseChanges);
	}

	public void end() {
		end = true;
	}

	public long getIterations() {
		return iterations;
	}

	public WorkerThreadStateHolder getStateHolder() {
		return stateHolder;
	}
}
