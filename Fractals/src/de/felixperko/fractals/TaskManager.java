package de.felixperko.fractals;

import java.util.ArrayList;

public class TaskManager {
	
	DataDescriptor dd;
	
	int sample_size = 1000;
	
	ArrayList<Task> openTasks = new ArrayList<>();
	ArrayList<Task> activeTasks = new ArrayList<>();
	
	public void generateTasks() {
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		int start = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			openTasks.add(new Task(start, end));
			start = end;
		}
	}
	
	public ArrayList<Task> getTasks(int amount){
		ArrayList<Task> tasks = new ArrayList<>();
		for (int i = openTasks.size()-1 ; i >= 0 ; i--) {
			Task task = openTasks.remove(i);
			task.state = Task.STATE_ASSINGED;
			activeTasks.add(task);
			tasks.add(task);
		}
		return tasks;
	}
}
