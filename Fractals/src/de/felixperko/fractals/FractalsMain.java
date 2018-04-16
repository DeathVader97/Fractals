package de.felixperko.fractals;

import de.felixperko.fractals.Tasks.LocalTaskProvider;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.ThreadManager;

public class FractalsMain {
	
	final static int HELPER_THREAD_COUNT = 8;
	
	private static WindowHandler windowHandler;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static DataDescriptor dataDescriptor;
	public static DataContainer dataContainer;
	public static TaskProvider taskProvider;
	
	public static void main(String[] args) {
		windowHandler = new WindowHandler();
		
		threadManager = new ThreadManager(HELPER_THREAD_COUNT, null);
		
		dataDescriptor = new DataDescriptor(-2, -2, 4./1080, 1920, 1080, 1920, 1080, 100);
		dataContainer = new DataContainer(dataDescriptor);
		
//		windowHandler.getMainRenderer().setDataContainer(dataContainer);
		
		taskManager = new TaskManager(dataDescriptor, dataContainer);
		taskManager.generateTasks();
		
		taskProvider = new LocalTaskProvider(taskManager, dataDescriptor);
		threadManager.addTaskProvider(taskProvider);
		
		startRendering();
	}

	private static void startRendering() {
		while (!Thread.interrupted())
			windowHandler.render();
	}

	public static WindowHandler getWindowHandler() {
		return windowHandler;
	}
}
