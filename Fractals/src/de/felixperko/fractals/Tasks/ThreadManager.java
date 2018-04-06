package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

public class ThreadManager {
	
	WorkerThread[] workerThreads;
	TaskProvider taskProvider;
	
	public ThreadManager(int threads, TaskProvider taskProvider) {
		this.workerThreads = new WorkerThread[threads];
		for (int i = 0 ; i < threads ; i++) {
			workerThreads[i] = new WorkerThread(i, taskProvider);
		}
		
		for (int i = 0; i < workerThreads.length; i++) {
			workerThreads[i].run();
		}
	}
}
