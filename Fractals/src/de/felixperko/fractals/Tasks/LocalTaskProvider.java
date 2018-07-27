package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.util.CategoryLogger;

public class LocalTaskProvider extends TaskProvider {
	
	TaskManager taskManager;
	
	public LocalTaskProvider() {
		super();
		this.pref_bufferSize = 0;
	}

	@Override
	public Task getTask() throws IllegalStateException{
		if (taskManager == null) {
			taskManager = FractalsMain.taskManager;
			if (taskManager == null) {
				CategoryLogger.WARNING.log("LocalTaskProvider", "can't provide tasks: TaskManager not set.");
				return null;
			}
		}
		if (dataDescriptor == null) {
			CategoryLogger.WARNING.log("LocalTaskProvider", "dataDescriptor isn't set: returned no task");
			return null;
		}
		return taskManager.getTask();
	}

	@Override
	public void taskFinished(Task task) {
		taskManager.taskFinished((SequentialTask)task);
		//TODO generalize for all tasks
	}
}
