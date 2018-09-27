package de.felixperko.fractals.server.tasks;

public interface TaskManager {

	void generateTasks();

	Task getTask();

	void taskFinished(Task task);

	void clearTasks();
	
	boolean isJobActive(int jobId);

	String getStateText();

}