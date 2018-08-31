package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.felixperko.fractals.Tasks.threading.FractalsThread;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.Grid;
import de.felixperko.fractals.renderer.GridRenderer;

public class NewTaskManagerImpl extends FractalsThread implements TaskManager {
	
	public NewTaskManagerImpl() {
		super("TaskManager", 5);
	}

	GridRenderer renderer;
	Grid grid;
	DataDescriptor dataDescriptor;
	
	boolean generateTasks;
	boolean updatePriorities;
	public List<Chunk> addChunkList = new ArrayList<>();
	
	boolean idle = true;
	
	public List<ChunkTask> priorityList = new ArrayList<>();
	public Comparator<ChunkTask> priorityComparator = new Comparator<ChunkTask>() {
		@Override
		public int compare(ChunkTask arg0, ChunkTask arg1) {
			return Double.compare(arg0.getChunk().getPriority(), arg1.getChunk().getPriority());
		}
	};
	
	public void addChunk(Chunk c) {
		synchronized (addChunkList) {
			addChunkList.add(c);
		}
	}
	
	@Override
	public void run() {
		//TODO replace sleep with reentrant lock
		
		//Task Manager loop
		while (!Thread.interrupted()) {
			
			idle = true;
			
			generateTasks();
			updatePriorities();

			setPhase(FractalsThread.PHASE_IDLE);
			if (idle) {//nothing has been done, save some time
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		setPhase(FractalsThread.PHASE_STOPPED);
	}
	
	@Override
	public void generateTasks() {
		if (!generateTasks)
			return;
		generateTasks = false;
		idle = false;
		setPhase(FractalsThread.PHASE_WORKING);
		
		synchronized (addChunkList) {
			for (Chunk add : addChunkList) {
				priorityList.add(new ChunkTask(add, dataDescriptor));
			}
			addChunkList.clear();
		}
	}
	
	public void updatePriorities() {
		if (!updatePriorities)
			return;
		updatePriorities = false;
		idle = false;
		setPhase(FractalsThread.PHASE_WORKING);
		
		Collections.sort(priorityList, priorityComparator);
	}

	@Override
	public Task getTask() {
		if (!idle) //busy
			return null;
		
		ChunkTask c = priorityList.remove(0);
		
		return c;
	}

	@Override
	public void taskFinished(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearTasks() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isJobActive(int jobId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStateText() {
		// TODO Auto-generated method stub
		return "PLACEHOLDER";
	}

	public void setUpdatePriorities() {
		updatePriorities = true;
	}
	
	public void setGenerateTasks() {
		generateTasks = true;
	}

}
