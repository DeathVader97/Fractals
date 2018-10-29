package de.felixperko.fractals.server.threads;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.threads.CalcPixelThread;
import de.felixperko.fractals.client.threads.IterationPositionThread;
import de.felixperko.fractals.server.network.ClientWriteThread;
import de.felixperko.fractals.server.network.ServerConnectThread;
import de.felixperko.fractals.server.network.ServerWriteThread;
import de.felixperko.fractals.server.tasks.ArrayListBatchTaskManager;
import de.felixperko.fractals.server.tasks.TaskProvider;

public class ThreadManager {
	
	WorkerThread[] workerThreads;
	IterationPositionThread iterationWorkerThread;
	ArrayListBatchTaskManager taskManagerThread;
	CalcPixelThread calcPixelThread = new CalcPixelThread("calcpixel");
	
	TaskProvider taskProvider;
	
	List<TaskProvider> providers = new ArrayList<>();
	
	ServerConnectThread serverConnectThread = null;
	ArrayList<ServerWriteThread> serverThreads = new ArrayList<>();
	ClientWriteThread clientThread = null;
	
	public ThreadManager() {
	}
	
	public CalcPixelThread getCalcPixelThread() {
		return calcPixelThread;
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
//		FractalsMain.mainWindow.resetPerformanceBars();
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
		
		if (iterationWorkerThread != null)
			iterationWorkerThread.interrupt();
		iterationWorkerThread = new IterationPositionThread();
		iterationWorkerThread.start();
		
		taskManagerThread = (ArrayListBatchTaskManager) FractalsMain.taskManager;
		
		updateProviders();
		threadsChanged();
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
		if (serverConnectThread != null && serverConnectThread.isAlive())
			return;
		serverConnectThread = new ServerConnectThread();
		serverConnectThread.start();
		threadsChanged();
	}
	
	private void threadsChanged() {
		FractalsMain.mainWindow.refreshProgressionThreads();
	}

	public ServerWriteThread startServerSocket(Socket socket) {
		ServerWriteThread thread = new ServerWriteThread(socket);
		serverThreads.add(thread);
		thread.start();
		threadsChanged();
		return thread;
	}
	
	public void startClient() {
		if (clientThread != null && !clientThread.isCloseConnection())
			return;
		try {
			//TODO make configurable
			String host = "localhost";
			int port = 3141;
			clientThread = new ClientWriteThread(new Socket(host, port));
			FractalsMain.serverConnection.setWriteToServer(clientThread);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clientThread.start();
		threadsChanged();
	}

	public IterationPositionThread getIterationWorkerThread() {
		return iterationWorkerThread;
	}

	public ServerConnectThread getServerConnectThread() {
		return serverConnectThread;
	}

//	public void setServerConnectThread(ServerConnectThread serverConnectThread) {
//		this.serverConnectThread = serverConnectThread;
//	}

	public ArrayList<ServerWriteThread> getServerThreads() {
		return serverThreads;
	}

//	public void setServerThreads(ArrayList<ServerWriteThread> serverThreads) {
//		this.serverThreads = serverThreads;
//	}

	public ClientWriteThread getClientThread() {
		return clientThread;
	}

//	public void setClientThread(ClientWriteThread clientThread) {
//		this.clientThread = clientThread;
//	}
}
