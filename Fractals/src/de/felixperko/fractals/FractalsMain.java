package de.felixperko.fractals;

import java.util.List;

import de.felixperko.fractals.Tasks.LocalTaskProvider;
import de.felixperko.fractals.Tasks.PerformanceMonitor;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.Tasks.ThreadManager;
import de.felixperko.fractals.gui.MainWindow;
import de.felixperko.fractals.state.DiscreteState;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.state.StateHolder;

public class FractalsMain extends StateHolder{
	
	public static FractalsMain main;
	
	final static int HELPER_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	final static int HELPER_THREAD_COUNT = 1;
	
	public static WindowHandler windowHandler;
	
	public static MainWindow mainWindow;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static PerformanceMonitor performanceMonitor;
	public static TaskProvider taskProvider;
	
	public static LocationHolder locationHolder;
	
	public static void main(String[] args) {
//		windowHandler = new WindowHandler();
		main = new FractalsMain();
		DiscreteState<Integer> testState = new DiscreteState<Integer>("Teststate", 10) {
			@Override
			public Integer getNext() {
				Integer v = getValue()+10;
				if (v > 100)
					return null;
				return v;
			}
			@Override
			public Integer getPrevious() {
				Integer v = getValue()-10;
				if (v < 0)
					return null;
				return v;
			}
		};
		testState.setIncrementable(true);
		testState.setDecrementable(true);
		main.addState(testState);
		
		mainWindow = new MainWindow();
		
		locationHolder = new LocationHolder();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		threadManager = new ThreadManager(HELPER_THREAD_COUNT, null);
		performanceMonitor = new PerformanceMonitor(threadManager);
		
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
