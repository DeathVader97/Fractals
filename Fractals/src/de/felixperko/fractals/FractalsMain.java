package de.felixperko.fractals;

import de.felixperko.fractals.Tasks.LocalTaskProvider;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.ThreadManager;
import de.felixperko.fractals.gui.MainWindow;

public class FractalsMain {
	
	final static int HELPER_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	final static int HELPER_THREAD_COUNT = 1;
	
	public static WindowHandler windowHandler;
	
	public static MainWindow mainWindow;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static TaskProvider taskProvider;
	
	public static LocationHolder locationHolder;
	
	public static void main(String[] args) {
//		windowHandler = new WindowHandler();
		mainWindow = new MainWindow();
		
		locationHolder = new LocationHolder();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		threadManager = new ThreadManager(HELPER_THREAD_COUNT, null);
		
//		FractalRenderer renderer = new FractalRenderer();
		FractalRendererSWT renderer = new FractalRendererSWT(mainWindow.getDisplay());
		
//		windowHandler.getMainRenderer().setDataContainer(dataContainer);
		
		taskManager = new TaskManager(renderer.getDataDescriptor(), renderer.getDataContainer());
		taskManager.generateTasks();
		
		taskProvider = new LocalTaskProvider(taskManager, renderer.getDataDescriptor());
		threadManager.addTaskProvider(taskProvider);
		
		mainWindow.setMainRenderer(renderer);
		mainWindow.open();
		
//		startRendering();
	}

	private static void startRendering() {
		while (!Thread.interrupted())
			windowHandler.render();
	}

	public static WindowHandler getWindowHandler() {
		return windowHandler;
	}

	public static void shutdown() {
		System.exit(0);
	}
}
