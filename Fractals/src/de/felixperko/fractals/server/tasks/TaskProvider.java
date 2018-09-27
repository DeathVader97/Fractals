package de.felixperko.fractals.server.tasks;

import java.util.ArrayList;

import de.felixperko.fractals.server.data.DataDescriptor;

public abstract class TaskProvider {
	
	public int pref_bufferSize;
	ArrayList<Task> buffer;
	public DataDescriptor dataDescriptor;
	
	public TaskProvider() {
	}
	
	public void setDataDescriptor(DataDescriptor dataDescriptor) {
		this.dataDescriptor = dataDescriptor;
	}
	
	public abstract Task getTask();
	public abstract void taskFinished(Task task);
}
