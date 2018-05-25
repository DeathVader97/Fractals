package de.felixperko.fractals.Tasks;


import java.awt.Color;
import java.util.ArrayList;

import de.felixperko.fractals.FractalsMain;

public class WorkerThread extends Thread {
	
	static int ID_COUNTER = 0;
	
	static WorkerPhase PHASE_IDLE = new WorkerPhase("Idle", Color.GRAY);
	static WorkerPhase PHASE_NO_PROVIDER = new WorkerPhase("No provider", Color.ORANGE);
	static WorkerPhase PHASE_WAITING = new WorkerPhase("Waiting", Color.YELLOW);
	static WorkerPhase PHASE_WORKING = new WorkerPhase("Working", Color.BLUE);
	static WorkerPhase PHASE_SAVING = new WorkerPhase("Saving", Color.CYAN);
	static WorkerPhase PHASE_STOPPED = new WorkerPhase("Stopped", Color.RED);
	static WorkerPhase DEFAULT_PHASE = PHASE_IDLE;
	
	TaskProvider taskProvider;
//	public String name;
	
	Task task;
	
	WorkerPhase phase = DEFAULT_PHASE;
	ArrayList<WorkerPhaseChange> phaseChanges = new ArrayList<>();
	long iterations = 0;
	
	PerformanceMonitor monitor;

	boolean continueWorking = false;
	boolean end = false;
	
	public WorkerThread(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
		setName("WorkerThread "+ID_COUNTER++);
		setPriority(3);
	}
	
	double lastIterationsPerMs = 0;
	long taskFinishedTime = 0;
	
	@Override
	public void run() {
		System.out.println("starting worker thread: "+getName());
		
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
//					System.out.println("info: thread "+name+" ("+getName()+") interrupted.");
				}
			}
//			if (System.nanoTime() - taskFinishedTime < 10000000)
//				phase = "Working ("+lastIterationsPerMs+" it/s)";
//			else
//				phase = "Working";
			setPhase(PHASE_WORKING);
			if (FractalsMain.taskManager.jobId != task.jobId)
				continue;
//			System.out.println(name+" task started ("+task.startSample+" "+task.getMaxIterations()+")");
			TaskProvider tp = taskProvider;
			try {
				task.run(tp.dataDescriptor);
				taskFinishedTime = System.nanoTime();
				setLastIterationsPerMs(task.samplesPerMs);
				iterations += task.end_sample_count;
				setPhase(PHASE_SAVING);
				tp.taskFinished(task);
			} catch (Exception e) {
				if (FractalsMain.taskManager.jobId != task.jobId)
					continue;
				e.printStackTrace();
			}
//			int notFinishedCount = 0;
//			for (int it : task.results)
//				if (it == 0)
//					notFinishedCount++;
//			System.out.println(name+" task finished... "+task.samplesPerMs+" samples/ms ; not finished: "+notFinishedCount+"/"+(task.endSample-task.startSample));
		}
		setPhase(PHASE_STOPPED);
	}
	
	public void setPhase(WorkerPhase phase) {
		this.phase = phase;
		phaseChanges.add(new WorkerPhaseChange(phase));
	}

	public void setTaskProvider(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
//		continueWorking = true;
//		interrupt();
	}
	
	public WorkerPhase getPhase() {
		return phase;
	}
	
	private void setLastIterationsPerMs(double iterationsPerMs) {
		this.lastIterationsPerMs = iterationsPerMs;
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
}
