package de.felixperko.fractals.server.tasks;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.util.CategoryLogger;

public class LocalTaskProvider extends TaskProvider {
	
	Map<Integer, TaskManager> taskManagers = new HashMap<>();
	
	CategoryLogger logWarning = CategoryLogger.WARNING.createSubLogger("LocalTaskProvider");
	CategoryLogger logInfo = CategoryLogger.INFO.createSubLogger("LocalTaskProvider");
	
	int round_robin = 0;
	
	public LocalTaskProvider() {
		super();
		this.pref_bufferSize = 0;
	}

	@Override
	public Task getTask() throws IllegalStateException{
		if (taskManagers.isEmpty()) {
			TaskManager taskManager = FractalsMain.taskManager;
			if (taskManager == null) {
				logWarning.log("can't provide tasks: TaskManager not set.");
				return null;
			}
			taskManagers.put(taskManager.getTaskManagerId(), taskManager);
		}
		if (dataDescriptor == null) {
			logWarning.log("dataDescriptor isn't set: returned no task");
			return null;
		}
		
		Task t = null;
		
		if (taskManagers.size() == 1)
			t = taskManagers.get(0).getTask();
		else {
			double prio = Double.MAX_VALUE;
			for (TaskManager tm : taskManagers.values()) {
				t = tm.getTask();
				if (t != null)
					break;
				//TODO implement fair load balancing
//				Task t2 = tm.getTask(); //problem: task gets automatically removed from manager
//				if (t2 != null) {
//					double prio2 = t2.getPriority();
//					if (prio2 < prio) {
//						t = t2;
//						prio = prio2;
//					}
//				
//				}
			}
		}
		
		return t;
	}

	@Override
	public void taskFinished(Task task) {
		taskManagers.get(task.getTaskManagerId()).taskFinished(task);
//		taskManager.taskFinished(task);
	}
}
