package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Tasks.threading.FractalsThread;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.Grid;
import de.felixperko.fractals.renderer.GridRenderer;
import de.felixperko.fractals.renderer.Renderer;
import de.felixperko.fractals.util.NumberUtil;

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
	List<Chunk> addChunkList = new ArrayList<>();
	List<ChunkTask> finishedTaskList = new ArrayList<>();
	
	boolean idle = true;
	
	public List<ChunkTask> priorityList = new ArrayList<>();
	public Comparator<ChunkTask> priorityComparator = new Comparator<ChunkTask>() {
		@Override
		public int compare(ChunkTask arg0, ChunkTask arg1) {
			if (arg0 == null || arg1 == null) {
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
		synchronized (addChunkList) {
			addChunkList.add(c);
			generateTasks = true;
		}
	}
	long debug_t = 0;
	@Override
	public void run() {
		
		log.log("started");
		log.log(this+"");
		
		//Task Manager loop
		//TODO replace sleep with reentrant lock
		while (!Thread.interrupted()) {
			idle = true;
			
			generateTasks();
			updatePriorities();
			finishTasks();

			setPhase(FractalsThread.PHASE_IDLE);
			if (idle) {//nothing has been done, save some time
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		setPhase(FractalsThread.PHASE_STOPPED);
	}
	
	private void finishTasks() {
		if (finishedTaskList.isEmpty())
			return;
		
//		log.log("finishing tasks...");
		int newlyAdded = 0;
		
		synchronized (finishedTaskList) {
			FractalsMain.mainWindow.canvas.getDisplay().syncExec(() -> FractalsMain.mainWindow.setRedraw(true));
			
			int maxState = dataDescriptor.getPatternProvider().getMaxState();
			for (ChunkTask finishedTask : finishedTaskList) {
				if (finishedTask.getChunk().getPatternState().getId() < maxState) {
					addTask(finishedTask);
					newlyAdded++;
				}
			}
			
			finishedTaskList.clear();
		}
		
		if (newlyAdded > 0) {
			updatePriorities = true;
			updatePriorities();
		}
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
		
		synchronized (addChunkList) {
			if (!addChunkList.isEmpty()){
				setUpdatePriorities();
				dataDescriptor = renderer.getDataDescriptor();
				for (Chunk add : addChunkList) {
					if (!add.isDisposed()) {
						addTask(new ChunkTask(add, dataDescriptor));
					}
				}
				log.log("generated "+addChunkList.size()+" tasks");
				addChunkList.clear();
			}
		}
	}

	private void addTask(ChunkTask task) {
		priorityList.add(task);
	}
	
	public void updatePriorities() {
		if (!updatePriorities)
			return;
		updatePriorities = false;
		idle = false;
		setPhase(FractalsThread.PHASE_WORKING);

//		log.log("updating priorities...");
		
		Collections.sort(priorityList, priorityComparator);
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
	public void clearTasks() {
		priorityList.clear();
	}

	@Override
	public boolean isJobActive(int jobId) {
		// TODO Auto-generated method stub
		return true;
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
