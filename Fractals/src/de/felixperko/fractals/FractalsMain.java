package de.felixperko.fractals;

import de.felixperko.fractals.Tasks.LocalTaskProvider;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.ThreadManager;

public class FractalsMain {
	
	final static int HELPER_THREAD_COUNT = 4;
	
	private static WindowHandler windowHandler;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static TaskProvider taskProvider;
	
	public static void main(String[] args) {
		windowHandler = new WindowHandler();
		
		threadManager = new ThreadManager(HELPER_THREAD_COUNT, null);
		
		FractalRenderer renderer = new FractalRenderer();
		
//		windowHandler.getMainRenderer().setDataContainer(dataContainer);
		
		taskManager = new TaskManager(renderer.getDataDescriptor(), renderer.getDataContainer());
		taskManager.generateTasks();
		
		taskProvider = new LocalTaskProvider(taskManager, renderer.getDataDescriptor());
		threadManager.addTaskProvider(taskProvider);
		
		windowHandler.setMainRenderer(renderer);
		
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
