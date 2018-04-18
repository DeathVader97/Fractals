package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.FractalsMain;

public class WorkerThread extends Thread {
	
	static int ID_COUNTER = 0;
	
	TaskProvider taskProvider;
	String name;
	
	boolean continueWorking = false;
	
	public WorkerThread(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
		this.name = "WorkerThread "+ID_COUNTER++;
	}
	
	@Override
	public void run() {
		System.out.println("starting worker thread: "+name);
		
		Task task;
		
		while (continueWorking || !Thread.interrupted()) {
			continueWorking = false;
			while (taskProvider == null || (task = taskProvider.getTask()) == null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
//					System.out.println("info: thread "+name+" ("+getName()+") interrupted.");
				}
			}
			if (FractalsMain.taskManager.jobId != task.jobId)
				continue;
//			System.out.println(name+" task started ("+task.startSample+" "+task.getMaxIterations()+")");
			TaskProvider tp = taskProvider;
			try {
				task.run(tp.dataDescriptor);
			} catch (Exception e) {
				if (FractalsMain.taskManager.jobId != task.jobId)
					continue;
				e.printStackTrace();
			}
			tp.taskFinished(task);
//			System.out.println(name+" task finished... "+task.samplesPerMs+" samples/ms");
		}
	}

	public void setTaskProvider(TaskProvider taskProvider) {
		this.taskProvider = taskProvider;
		continueWorking = true;
		interrupt();
	}
}
