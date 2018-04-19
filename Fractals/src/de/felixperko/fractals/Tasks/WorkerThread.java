package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.FractalsMain;

public class WorkerThread extends Thread {
	
	static int ID_COUNTER = 0;
	
	TaskProvider taskProvider;
	String name;
	
	Task task;
	
	String phase = "Idle";

	boolean continueWorking = false;
	
	public WorkerThread(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
		this.name = "WorkerThread "+ID_COUNTER++;
	}
	
	double lastIterationsPerMs = 0;
	long taskFinishedTime = 0;
	
	@Override
	public void run() {
		System.out.println("starting worker thread: "+name);
		
		while (continueWorking || !Thread.interrupted()) {
			continueWorking = false;
			while (taskProvider == null || (task = taskProvider.getTask()) == null) {
				if (taskProvider == null)
					phase = "No provider";
				else
					phase = "Waiting";
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
//					System.out.println("info: thread "+name+" ("+getName()+") interrupted.");
				}
			}
			if (System.nanoTime() - taskFinishedTime < 10000000)
				phase = "Working ("+lastIterationsPerMs+" it/s)";
			else
				phase = "Working";
			if (FractalsMain.taskManager.jobId != task.jobId)
				continue;
//			System.out.println(name+" task started ("+task.startSample+" "+task.getMaxIterations()+")");
			TaskProvider tp = taskProvider;
			try {
				task.run(tp.dataDescriptor);
				taskFinishedTime = System.nanoTime();
				setLastIterationsPerMs(task.samplesPerMs);
			} catch (Exception e) {
				if (FractalsMain.taskManager.jobId != task.jobId)
					continue;
				e.printStackTrace();
			}
			tp.taskFinished(task);
//			System.out.println(name+" task finished... "+task.samplesPerMs+" samples/ms");
		}
		phase = "stopped";
	}

	public void setTaskProvider(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
		continueWorking = true;
		interrupt();
	}
	
	public String getPhase() {
		return phase;
	}
	
	private void setLastIterationsPerMs(double iterationsPerMs) {
		this.lastIterationsPerMs = iterationsPerMs;
	}
}
