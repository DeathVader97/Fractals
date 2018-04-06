package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.DataDescriptor;

public abstract class TaskProvider {
	
	public int pref_bufferSize;
	ArrayList<Task> buffer;
	public DataDescriptor dataDescriptor;
	
	public TaskProvider(DataDescriptor dataDescriptor) {
		this.dataDescriptor = dataDescriptor;
	}
	
	public abstract Task getTask();
	public abstract void taskFinished(Task task);
}
