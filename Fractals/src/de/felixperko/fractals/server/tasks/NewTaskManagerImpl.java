package de.felixperko.fractals.server.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.client.threads.CalcPixelThread;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.data.Grid;
import de.felixperko.fractals.server.data.ProcessingStepState;
import de.felixperko.fractals.server.threads.FractalsThread;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.Position;

public class NewTaskManagerImpl extends FractalsThread implements TaskManager {
	
	public NewTaskManagerImpl(GridRenderer renderer) {
		super("TaskManager", 5);
		this.renderer = renderer;
	}

	GridRenderer renderer;
	Grid grid;
	DataDescriptor dataDescriptor;
	
	boolean generateTasks;
	boolean updatePriorities;
	List<Chunk> addChunkList = new CopyOnWriteArrayList<>();
	List<ChunkTask> finishedTaskList = new CopyOnWriteArrayList<>();
	
	boolean idle = true;
	
	public List<ChunkTask> priorityList = new ArrayList<>();
	Map<Chunk, ChunkTask> taskMap = new ConcurrentHashMap<>();
	public Comparator<ChunkTask> priorityComparator = new Comparator<ChunkTask>() {
		@Override
		public int compare(ChunkTask arg0, ChunkTask arg1) {
			if (arg0 == null || arg1 == null) {//debug: why do i get occasional NPEs here?
				Thread.dumpStack();
				return 0;
			}
			return Double.compare(arg0.getPriority(), arg1.getPriority());
		}
	};
	
	/**
	 * Adds a chunk to be calculated.
	 * @param c
	 */
	public void addChunk(Chunk c) {
//		long t1 = System.nanoTime();
//		synchronized (addChunkList) {
			addChunkList.add(c);
			generateTasks = true;
			updatePriorities = true;
//		}
//		long t2 = System.nanoTime();
	}
	
	long debug_t = 0;
	@Override
	public void run() {
		
		log.log("started");
		log.log(this+"");
		
		//Task Manager loop
		//TODO replace sleep with reentrant lock
		while (!Thread.interrupted()) {
//			long t1 = System.nanoTime();
			
			idle = true;
			
			generateTasks();
//			long t2 = System.nanoTime();
			updatePriorities();
//			long t3 = System.nanoTime();
			finishTasks();
//			long t4 = System.nanoTime();

			setPhase(FractalsThread.PHASE_IDLE);
			if (idle) {//nothing has been done, save some time
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			else {
//				double time = NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS*(System.nanoTime() - t1), 1);
//				double time_genTasks = NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS*(t2-t1), 1);
//				double time_updatePrios = NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS*(t3-t2), 1);
//				double time_finishTasks = NumberUtil.getRoundedDouble(NumberUtil.NS_TO_MS*(t4-t3), 1);
//				System.out.println("TaskManager Tick: "+time_genTasks+"ms, "+time_updatePrios+"ms, "+time_finishTasks+"ms	Total: "+time+"ms");
//			}
		}
		setPhase(FractalsThread.PHASE_STOPPED);
	}

	@Override
	public void generateTasks() {
		if (addChunkList.isEmpty()){
			return;
		}
		generateTasks = false;
		idle = false;
		setPhase(FractalsThread.PHASE_WORKING);

//		log.log("generating tasks...");
		
//		synchronized (addChunkList) {
//			if (!addChunkList.isEmpty()){
				setUpdatePriorities();
				dataDescriptor = renderer.getDataDescriptor();
				for (Chunk add : addChunkList) {
					if (!add.isDisposed()) {
						addTask(new ChunkTask(add, dataDescriptor));
					}
				}
				log.log("generated "+addChunkList.size()+" tasks");
				addChunkList.clear();
//			}
//		}
	}

	private void addTask(ChunkTask task) {
//		if (taskMap.containsKey(task.chunk))
//			throw new IllegalStateException("Chunk already has a task!");
		taskMap.put(task.chunk, task);
		priorityList.add(task);
	}
	
	public void updatePriorities() {
		if (!updatePriorities)
			return;
		updatePriorities = false;
		idle = false;
		setPhase(FractalsThread.PHASE_WORKING);

//		log.log("updating priorities...");
		
//		synchronized (this) {
			int tryCount = 0;
			while (true) {
				try {
					Collections.sort(priorityList, priorityComparator);
					break;
				} catch (Exception e) {
					if (tryCount >= 2)
						throw e;
					CategoryLogger.ERROR.log("TaskManager", "reoccuring error while sorting the priority list (NewTaskManagerImpl.updatePriorities())");
					tryCount++;
				}
			}
//		}
	}
	
	private void finishTasks() {
		if (finishedTaskList.isEmpty())
			return;
		
//		log.log("finishing tasks...");
		int newlyAdded = 0;
		
//		synchronized (finishedTaskList) {
			int maxState = dataDescriptor.getStepProvider().getMaxState();
			CalcPixelThread calcThread = ((GridRenderer)FractalsMain.mainWindow.getMainRenderer()).getCalcThread();
			for (ChunkTask finishedTask : finishedTaskList) {
				calcThread.addChunk(finishedTask.chunk);
				ProcessingStepState state = finishedTask.getChunk().getProcessingStepState();
				Position gridPos = finishedTask.getChunk().getGridPosition();
				double viewDist = renderer.viewGridDist(gridPos.getX(), gridPos.getY());
				if (state.getStateNumber() < maxState && viewDist < 1) {
					addTask(finishedTask);
					newlyAdded++;
				} else {
					taskMap.remove(finishedTask.getChunk());
				}
			}
			
			finishedTaskList.clear();
//		}
		
		if (newlyAdded > 0) {
			updatePriorities = true;
			updatePriorities();
		}
	}

	@Override
	public synchronized Task getTask() {
		if (!idle) //busy
			return null;
		if (priorityList.isEmpty())
			return null;
		ChunkTask c = priorityList.remove(0);
//		log.log("provided task...");
		return c;
	}

	@Override
	public void taskFinished(Task task) {
		if (!(task instanceof ChunkTask))
			throw new IllegalArgumentException();
		synchronized (finishedTaskList) {
			finishedTaskList.add((ChunkTask)task);
		}
	}

	@Override
	public synchronized void clearTasks() {
		addChunkList.clear();
		priorityList.clear();
		taskMap.clear();
	}

	@Override
	public boolean isJobActive(int jobId) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getStateText() {
		return getPhase().getName();
	}

	public void setUpdatePriorities() {
		updatePriorities = true;
	}
	
	public void setGenerateTasks() {
		generateTasks = true;
	}

	public void removeChunkTask(Chunk c) {
		if (!addChunkList.remove(c)) {
			ChunkTask task = taskMap.remove(c);
			if (task != null) {
				if (!priorityList.remove(task))
					finishedTaskList.remove(task);
			}
		}
	}

}
