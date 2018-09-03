package de.felixperko.fractals.Tasks.threading;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Tasks.NewTaskManagerImpl;
import de.felixperko.fractals.Tasks.TaskProvider;
import de.felixperko.fractals.network.ClientThread;
import de.felixperko.fractals.network.ServerConnectThread;
import de.felixperko.fractals.network.ServerThread;

public class ThreadManager {
	
	WorkerThread[] workerThreads;
	IterationPositionThread iterationWorkerThread;
	NewTaskManagerImpl taskManagerThread;
	
	TaskProvider taskProvider;
	
	List<TaskProvider> providers = new ArrayList<>();
	
	ServerConnectThread serverConnectThread = null;
	ArrayList<ServerThread> serverThreads = new ArrayList<>();
	ClientThread clientThread = null;
	
	public ThreadManager() {
	}
	
	public void setThreadCount(int threadCount) {
		if (workerThreads == null) {
			startThreads(threadCount);
		} else {
			if (workerThreads.length > threadCount)
				endThreads(workerThreads.length - threadCount);
			else if (workerThreads.length < threadCount)
				startThreads(threadCount - workerThreads.length);
		}
		FractalsMain.mainWindow.resetPerformanceBars();
	}

	private void endThreads(int count) {
		for (int i = workerThreads.length-1-count ; i < workerThreads.length-1 ; i++) {
			workerThreads[i].end();
		}
		workerThreads = Arrays.copyOf(workerThreads, workerThreads.length-count);
		//TODO providers
	}

	private void startThreads(int count) {
		
		WorkerThread[] newThreads;
		if (workerThreads == null) {
			newThreads = new WorkerThread[count];
			for (int i = 0 ; i < count ; i++) {
				newThreads[i] = startThread();
			}
		} else {
			newThreads = Arrays.copyOf(workerThreads, count+workerThreads.length);
			for (int i = workerThreads.length ; i < newThreads.length ; i++) {
				newThreads[i] = startThread();
			}
		}
		this.workerThreads = newThreads;
		
		iterationWorkerThread = new IterationPositionThread();
		iterationWorkerThread.start();
		
		taskManagerThread = (NewTaskManagerImpl) FractalsMain.taskManager;
		
		updateProviders();
	}

	private WorkerThread startThread() {
		WorkerThread thread = new WorkerThread(taskProvider);
		thread.start();
		return thread;
	}
	
	public void addTaskProvider(TaskProvider tp) {
		if (!providers.contains(tp))
			providers.add(tp);
		updateProviders();
	}
	
	public void removeTaskProvider(TaskProvider tp) {
		providers.remove(tp);
		updateProviders();
	}
	
	public void updateProviders() {
		if (providers.size() == 0)
			return;
		double remainder = 0;
		double step = workerThreads.length / providers.size();
		int nextFreeThread = 0;
		for (TaskProvider tp : providers) {
			remainder += step % 1;
			int assignedThreads = (int)step;
			if (remainder > 1) {
				remainder--;
				assignedThreads++;
			}
			for (int i = nextFreeThread ; i < nextFreeThread+assignedThreads ; i++) {
				workerThreads[i].setTaskProvider(tp);
			}
		}
	}

	public WorkerThread[] getThreads() {
		return workerThreads;
	}
	
	public void startServer() {
		if (serverConnectThread != null)
			return;
		serverConnectThread = new ServerConnectThread();
		serverConnectThread.start();
	}
	
	public void startServerSocket(Socket socket) {
		ServerThread thread = new ServerThread(socket);
		serverThreads.add(thread);
		thread.start();
	}
	
	public void startClient() {
		if (clientThread != null)
			return;
		clientThread = new ClientThread();
		clientThread.start();
	}

	public IterationPositionThread getIterationWorkerThread() {
		return iterationWorkerThread;
	}
}
