package de.felixperko.fractals;

import de.felixperko.fractals.Tasks.LocalTaskProvider;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.perf.PerformanceMonitor;
import de.felixperko.fractals.Tasks.threading.ThreadManager;
import de.felixperko.fractals.data.LocationHolder;
import de.felixperko.fractals.gui.MainWindow;
import de.felixperko.fractals.renderer.GridRenderer;
import de.felixperko.fractals.renderer.Renderer;
import de.felixperko.fractals.state.stateholders.MainStateHolder;

public class FractalsMain{
	
	public static FractalsMain main;
	
	public final static int HELPER_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	final static int HELPER_THREAD_COUNT = 1;
	
	public static MainWindow mainWindow;
	
	public static MainStateHolder mainStateHolder;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static PerformanceMonitor performanceMonitor;
	public static TaskProvider taskProvider;
	
	public static LocationHolder locationHolder;
	
	public static void main(String[] args) {
		main = new FractalsMain();
		mainStateHolder = new MainStateHolder(main);
		
		mainWindow = new MainWindow();
		
		locationHolder = new LocationHolder();
		
		performanceMonitor = new PerformanceMonitor();
		
		Renderer renderer = new GridRenderer();
		
		taskProvider = new LocalTaskProvider();

		mainWindow.open(renderer);
	}

	public static void shutdown() {
		System.exit(0);
	}
}
