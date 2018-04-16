package de.felixperko.fractals.Tasks;

import java.util.ArrayList;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;

public class TaskManager {
	
	DataDescriptor dd;
	DataContainer dc;
	
	int sample_size = 1000;
	
	ArrayList<Task> openTasks = new ArrayList<>();
//	ArrayList<Task> activeTasks = new ArrayList<>();
		
	public TaskManager(DataDescriptor dd, DataContainer dc) {
		this.dd = dd;
		this.dc = dc;
	}
	
	public void generateTasks() {
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		int start = 0;
		int c = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			openTasks.add(new Task(start, end, dd.maxIterations));
			start = end;
			c++;
		}
		System.out.println("generated "+c);
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
		System.arraycopy(task.results, 0, dc.samples, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, task.startSample, task.endSample-task.startSample);
	}
}
