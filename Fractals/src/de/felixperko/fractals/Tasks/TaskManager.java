package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;

public class TaskManager {
	
	DataDescriptor dd;
	DataContainer dc;
	
	int sample_size = 1000;
	
	AtomicInteger unfinishedTasksCount = new AtomicInteger();
	
	long generation_time = 0;
	
	ArrayList<Task> openTasks = new ArrayList<>();
	
	Random random = new Random(42);
	int jobId = 0;
	
	boolean finished = false;
//	ArrayList<Task> activeTasks = new ArrayList<>();
		
	public TaskManager(DataDescriptor dd, DataContainer dc) {
		this.dd = dd;
		this.dc = dc;
	}
	
	public synchronized void generateTasks() {
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		int start = 0;
		int c = 0;
		jobId = random.nextInt();
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			openTasks.add(new Task(start, end, dd.maxIterations, jobId));
			start = end;
			c++;
		}
		unfinishedTasksCount.set(c);
		generation_time = System.nanoTime();
		finished = c == 0;
		System.out.println("generated "+c+" tasks");
	}

//	public synchronized ArrayList<Task> getTasks(int amount){
//		ArrayList<Task> tasks = new ArrayList<>();
//		System.out.println("get "+amount+" tasks ("+openTasks.size()+" left)");
//		for (int i = openTasks.size()-1 ; i >= 0 ; i--) {
//			Task task = openTasks.remove(i);
//			task.state = Task.STATE_ASSINGED;
////			activeTasks.add(task);
//			tasks.add(task);
//		}
//		System.out.println("open tasks after retrival: "+openTasks.size());
//		return tasks;
//	}
	
	public synchronized Task getTask() {
		if (openTasks.size() == 0)
			return null;
		Task task = openTasks.get(openTasks.size()-1);
		openTasks.remove(task);
		task.state = Task.STATE_ASSINGED;
		return task;
	}
	
	public void taskFinished(Task task) {
//		System.out.println(Arrays.toString(task.results));
		if (task.jobId != jobId)
			return;
		System.arraycopy(task.results, 0, dc.samples, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, task.startSample, task.endSample-task.startSample);
		if (unfinishedTasksCount.decrementAndGet() == 0) {
			System.out.println("finished tasks after "+(System.nanoTime()-generation_time)/1000000000.+"s");
			finished = true;
		}
	}

	public void clearTasks() {
		openTasks.clear();
	}

	public DataContainer getDataContainer() {
		return dc;
	}

	public void setDataContainer(DataContainer dc) {
		this.dc = dc;
	}

	public boolean isFinished() {
		return finished;
	}
}
