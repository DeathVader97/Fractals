package de.felixperko.fractals.Tasks;

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
//				System.out.println(name+" ("+getName()+") no task");
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
//					System.out.println("info: thread "+name+" ("+getName()+") interrupted.");
				}
			}
			
			TaskProvider tp = taskProvider;
			task.run(tp.dataDescriptor);
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
