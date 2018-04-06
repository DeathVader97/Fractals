package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;

public class WorkerThread extends Thread {
	
	int id;
	TaskProvider taskProvider;
	
	public WorkerThread(int id, TaskProvider taskProvider) {
		this.id = id;
		this.taskProvider = taskProvider;
	}
	
	@Override
	public void run() {
		System.out.println("starting worker thread "+id+"...");
		
		Task task;
		
		while (!Thread.interrupted()) {
			while ((task = taskProvider.getTask()) == null) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			task.run(taskProvider.dataDescriptor);
			taskProvider.taskFinished(task);
		}
	}
}
