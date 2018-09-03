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
			return Double.compare(arg0.getChunk().getPriority(), arg1.getChunk().getPriority());
		}
	};
	
	public void addChunk(Chunk c) {
		synchronized (addChunkList) {
			addChunkList.add(c);
			if (c.getGridPosition().getX() == c.getGridPosition().getY())
				log.log("Queued "+c.getGridPosition().toString()+" -> "+addChunkList.size()+" "+this);
			generateTasks = true;
		}
	}
	
	@Override
	public void run() {
		log.log("started");
		//TODO replace sleep with reentrant lock
		log.log(this+"");
		//Task Manager loop
		while (!Thread.interrupted()) {
			
			idle = true;
			
			generateTasks();
			updatePriorities();
			finishTasks();

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
	
	private void finishTasks() {
		if (finishedTaskList.isEmpty())
			return;
		synchronized (finishedTaskList) {
			FractalsMain.mainWindow.canvas.getDisplay().syncExec(() -> FractalsMain.mainWindow.canvas.redraw());
			finishedTaskList.clear();
		}
		
	}

	@Override
	public void generateTasks() {
		if (!generateTasks){
			return;
		}
		generateTasks = false;
		idle = false;
		setPhase(FractalsThread.PHASE_WORKING);
		
		synchronized (addChunkList) {
			if (!addChunkList.isEmpty()){
				dataDescriptor = renderer.getDataDescriptor();
				for (Chunk add : addChunkList) {
					if (!add.isDisposed()) {
						priorityList.add(new ChunkTask(add, dataDescriptor));
					}
				}
				addChunkList.clear();
			}
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
		// TODO Auto-generated method stub

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
