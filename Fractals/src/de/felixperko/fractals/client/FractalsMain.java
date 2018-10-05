package de.felixperko.fractals.client;

import de.felixperko.fractals.client.gui.MainWindow;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.client.stateholders.ClientStateHolder;
import de.felixperko.fractals.client.stateholders.RendererStateHolder;
import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.data.LocationHolder;
import de.felixperko.fractals.server.network.Messenger;
import de.felixperko.fractals.server.tasks.LocalTaskProvider;
import de.felixperko.fractals.server.tasks.NewTaskManagerImpl;
import de.felixperko.fractals.server.tasks.TaskManager;
import de.felixperko.fractals.server.tasks.TaskProvider;
import de.felixperko.fractals.server.threads.ThreadManager;
import de.felixperko.fractals.server.util.performance.PerformanceMonitor;

public class FractalsMain{
	
	public static FractalsMain main;
	
	public final static int HELPER_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//	final static int HELPER_THREAD_COUNT = 1;
	
	public static MainWindow mainWindow;
	
	public static ClientStateHolder clientStateHolder;
	public static RendererStateHolder rendererStateHolder;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static PerformanceMonitor performanceMonitor;
	public static TaskProvider taskProvider;
	
	public static LocationHolder locationHolder;
	
	public static Messenger messenger;
	
	public static void main(String[] args) {
		
		main = new FractalsMain();
		messenger = new Messenger();
		
		FractalsServerMain.main(args);
		clientStateHolder = new ClientStateHolder();
		
		mainWindow = new MainWindow();
		
		locationHolder = new LocationHolder();
		
		performanceMonitor = new PerformanceMonitor();
		
		Renderer renderer = new GridRenderer();
		rendererStateHolder = new RendererStateHolder(renderer);
		
		taskProvider = new LocalTaskProvider();

		mainWindow.open(renderer);
		
		threadManager = new ThreadManager();
		threadManager.setThreadCount(HELPER_THREAD_COUNT);
		threadManager.addTaskProvider(taskProvider);
		taskProvider.setDataDescriptor(renderer.getDataDescriptor());
		taskManager = new NewTaskManagerImpl((GridRenderer) renderer);
		taskManager.generateTasks();
		((NewTaskManagerImpl)taskManager).start();
		performanceMonitor.startPhase();
		
		mainWindow.windowLoop();
	}

	public static void shutdown() {
		System.exit(0);
	}
}