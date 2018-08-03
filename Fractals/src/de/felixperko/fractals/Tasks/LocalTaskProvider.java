package de.felixperko.fractals.Tasks;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.util.CategoryLogger;

public class LocalTaskProvider extends TaskProvider {
	
	TaskManager taskManager;
	
	CategoryLogger logWarning = CategoryLogger.WARNING.createSubLogger("LocalTaskProvider");
	CategoryLogger logInfo = CategoryLogger.INFO.createSubLogger("LocalTaskProvider");
	
	public LocalTaskProvider() {
		super();
		this.pref_bufferSize = 0;
	}

	@Override
	public Task getTask() throws IllegalStateException{
		if (taskManager == null) {
			taskManager = FractalsMain.taskManager;
			if (taskManager == null) {
				logWarning.log("can't provide tasks: TaskManager not set.");
				return null;
			}
		}
		if (dataDescriptor == null) {
			logWarning.log("dataDescriptor isn't set: returned no task");
			return null;
		}
		Task t = taskManager.getTask();
//		if (t != null)
//			logInfo.log("started "+t);
		return t;
	}

	@Override
	public void taskFinished(Task task) {
//		logInfo.log("finished "+task);
		taskManager.taskFinished(task);
	}
}
