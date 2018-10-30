package de.felixperko.fractals.server.tasks;

import de.felixperko.fractals.server.data.DataDescriptor;

public interface TaskManager {

	void generateTasks();

	Task getTask();

	void taskFinished(Task task);

	void clearTasks();
	
	boolean isJobActive(int jobId);

	String getStateText();

	public void setUpdatePriorities();

	void setGenerateTasks();
	
	public int getTaskManagerId();
	
	void start();
	
	DataDescriptor getDataDescriptor();
	void setDataDescriptor(DataDescriptor dataDescriptor);
}