
package de.felixperko.fractals.client;
import de.felixperko.fractals.client.gui.MainWindow;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.client.stateholders.ClientStateHolder;
import de.felixperko.fractals.client.stateholders.RendererStateHolder;
import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.data.LocationHolder;
import de.felixperko.fractals.server.network.ServerConnection;
import de.felixperko.fractals.server.tasks.LocalTaskProvider;
import de.felixperko.fractals.server.tasks.ArrayListBatchTaskManager;
import de.felixperko.fractals.server.tasks.TaskManager;
import de.felixperko.fractals.server.tasks.TaskProvider;
import de.felixperko.fractals.server.threads.ThreadManager;
import de.felixperko.fractals.server.util.performance.PerformanceMonitor;

public class FractalsMain{
	
	public static FractalsMain main;
	
	public final static int CORE_COUNT = Runtime.getRuntime().availableProcessors();
//	public final static int HELPER_THREAD_COUNT = CORE_COUNT*2/3 > 4 ? Math.max(CORE_COUNT*2/3, 4) : CORE_COUNT;
	public final static int HELPER_THREAD_COUNT = CORE_COUNT/2;
//	final static int HELPER_THREAD_COUNT = 1;
	
	public static MainWindow mainWindow;
	
	public static ClientStateHolder clientStateHolder;
	public static RendererStateHolder rendererStateHolder;
	
	public static ThreadManager threadManager;
	public static TaskManager taskManager;
	public static PerformanceMonitor performanceMonitor;
	public static TaskProvider taskProvider;
	
	public static LocationHolder locationHolder;
	
	public static ServerConnection serverConnection;
	
	public static void main(String[] args) {
		
		main = new FractalsMain();
		serverConnection = new ServerConnection();
		
		FractalsServerMain.main(args);
		clientStateHolder = new ClientStateHolder();
		
		mainWindow = new MainWindow();
		
		locationHolder = new LocationHolder();
		
		performanceMonitor = new PerformanceMonitor();

		threadManager = new ThreadManager();
		Renderer renderer = new GridRenderer();
		rendererStateHolder = new RendererStateHolder(renderer);
		
		taskProvider = new LocalTaskProvider();

		threadManager.setThreadCount(HELPER_THREAD_COUNT);
		mainWindow.open(renderer);
		
		threadManager.addTaskProvider(taskProvider);
		taskProvider.setDataDescriptor(renderer.getDataDescriptor());
		taskManager = new ArrayListBatchTaskManager((GridRenderer) renderer);
		taskManager.start();
		((GridRenderer) renderer).setTaskManager(FractalsMain.taskManager);
		((GridRenderer) renderer).boundsChanged();
		performanceMonitor.startPhase();
		
		mainWindow.windowLoop();
	}

	public static void shutdown() {
		System.exit(0);
	}
}
