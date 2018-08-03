package de.felixperko.fractals.Tasks;

public interface TaskManager {

	void generateTasks();

	Task getTask();

	void taskFinished(Task task);

	void clearTasks();
	
	boolean isJobActive(int jobId);

	String getStateText();

}