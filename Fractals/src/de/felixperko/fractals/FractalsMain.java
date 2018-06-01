package de.felixperko.fractals;

import de.felixperko.fractals.Tasks.LocalTaskProvider;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.perf.PerformanceMonitor;
import de.felixperko.fractals.Tasks.threading.ThreadManager;
import de.felixperko.fractals.gui.MainWindow;
import de.felixperko.fractals.state.MainStateHolder;
import de.felixperko.fractals.util.Logger;

public class FractalsMain{
	
	public static FractalsMain main;
	
	public final static int HELPER_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	final static int HELPER_THREAD_COUNT = 1;
	
	public static WindowHandler windowHandler;
	
	public static MainWindow mainWindow;
	
	public static MainStateHolder mainStateHolder;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static PerformanceMonitor performanceMonitor;
	public static TaskProvider taskProvider;
	
	public static LocationHolder locationHolder;
	
	public static void main(String[] args) {
//		windowHandler = new WindowHandler();
		main = new FractalsMain();
		mainStateHolder = new MainStateHolder(main);
		
		mainWindow = new MainWindow();
		
		locationHolder = new LocationHolder();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		performanceMonitor = new PerformanceMonitor();
		
//		FractalRenderer renderer = new FractalRenderer();
		FractalRendererSWT renderer = new FractalRendererSWT(mainWindow.getDisplay());
		
//		windowHandler.getMainRenderer().setDataContainer(dataContainer);
		
		taskProvider = new LocalTaskProvider();

		mainWindow.open(renderer);
		
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
