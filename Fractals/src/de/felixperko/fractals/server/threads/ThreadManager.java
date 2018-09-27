package de.felixperko.fractals.server.threads;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.threads.IterationPositionThread;
import de.felixperko.fractals.server.network.ClientWriteThread;
import de.felixperko.fractals.server.network.ServerConnectThread;
import de.felixperko.fractals.server.network.ServerWriteThread;
import de.felixperko.fractals.server.tasks.NewTaskManagerImpl;
import de.felixperko.fractals.server.tasks.TaskProvider;

public class ThreadManager {
	
	WorkerThread[] workerThreads;
	IterationPositionThread iterationWorkerThread;
	NewTaskManagerImpl taskManagerThread;
	
	TaskProvider taskProvider;
	
	List<TaskProvider> providers = new ArrayList<>();
	
	ServerConnectThread serverConnectThread = null;
	ArrayList<ServerWriteThread> serverThreads = new ArrayList<>();
	ClientWriteThread clientThread = null;
	
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
	
	public ServerWriteThread startServerSocket(Socket socket) {
		ServerWriteThread thread = new ServerWriteThread(socket);
		serverThreads.add(thread);
		thread.start();
		return thread;
	}
	
	public void startClient() {
		if (clientThread != null)
			return;
		try {
			clientThread = new ClientWriteThread();
			FractalsMain.messenger.setWriteToServer(clientThread);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clientThread.start();
	}

	public IterationPositionThread getIterationWorkerThread() {
		return iterationWorkerThread;
	}
}
