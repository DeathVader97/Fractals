package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.DataDescriptor;

public class LocalTaskProvider extends TaskProvider {
	
	TaskManager taskManager;
	
	public LocalTaskProvider(TaskManager taskManager, DataDescriptor dataDescriptor) {
		super(dataDescriptor);
		this.pref_bufferSize = 0;
		this.taskManager = taskManager;
	}

	@Override
	public Task getTask() {
		return taskManager.getTasks(1).get(0);
	}

	@Override
	public void taskFinished(Task task) {
		taskManager.taskFinished(task);
	}
}
