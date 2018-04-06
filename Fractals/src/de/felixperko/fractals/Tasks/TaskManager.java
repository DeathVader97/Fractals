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
	
	public void generateTasks() {
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		int start = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			openTasks.add(new Task(start, end, dd.maxIterations));
			start = end;
		}
	}
	
	public synchronized ArrayList<Task> getTasks(int amount){
		ArrayList<Task> tasks = new ArrayList<>();
		for (int i = openTasks.size()-1 ; i >= 0 ; i--) {
			Task task = openTasks.remove(i);
			task.state = Task.STATE_ASSINGED;
//			activeTasks.add(task);
			tasks.add(task);
		}
		return tasks;
	}
	
	public void taskFinished(Task task) {
		System.arraycopy(task.results, 0, dc.samples, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, task.startSample, task.endSample-task.startSample);
	}
}
