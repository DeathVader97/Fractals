package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import de.felixperko.fractals.data.DataContainer;
import de.felixperko.fractals.data.DataDescriptor;

public class Job {
	
	boolean culling_enabled = true;
	
	DataDescriptor dataDescriptor;
	DataContainer dataContainer;
	
	double priorityWeight;
	
	ArrayList<Task> availableTasks = new ArrayList<>();
	
	HashMap<Integer, Integer> depth_to_index = new HashMap<>();
	HashMap<Integer, Integer> index_to_depth = new HashMap<>();
	
	int jobId = new Random().nextInt();
	
	public Job(DataContainer container) {
		this.dataContainer = container;
		this.dataDescriptor = container.getDescriptor();
	}
	
	public Job(DataDescriptor descriptor) {
		this.dataDescriptor = descriptor;
	}

	public double getPriorityWeight() {
		return priorityWeight;
	}

	public void setPriorityWeight(double priorityWeight) {
		this.priorityWeight = priorityWeight;
	}
	
	private int generateNextDepth() {
		int index = depth_to_index.size();
		int depth = Math.round((index == 0) ? 50 : index_to_depth.get(index-1) * 1.1f);
		index_to_depth.put(index, depth);
		depth_to_index.put(depth, index);
		return depth;
	}
	
	public synchronized Task getTask() {
		int size = availableTasks.size();
		if (size > 0) {
			Task task = availableTasks.remove(size-1);
			return task;
		}
		return null;
	}
	
	public synchronized void taskFinished(Task task) {
		if (!applicable(task))
			return;
		
		copyData(task);
		
		if (culling_enabled)
			updateCulling(task);
		
		postTaskFinished(task);
	}

	private boolean applicable(Task task) {
		return task.jobId == jobId;
	}

	private void copyData(Task task) {
		
	}

	private void updateCulling(SequentialTask task) {
		int dimx = dataDescriptor.dim_sampled_x;
		int dimy = dataDescriptor.dim_sampled_y;
		for (Integer c : task.changedIndices) {
			int s = c+task.startSample;
			int y = s/dimx;
			int x = s%dimx;
			int sample = dataContainer.samples[s];
			int rad = sample < task.getPreviousMaxIterations() ? 5 : 1;
			
			int start_x = x-rad;
			int end_x = x+rad;
			int start_y = y-rad;
			int end_y = y+rad;
			
			if (start_x < 0)
				start_x = 0;
			if (start_y < 0)
				start_y = 0;
			if (end_x >= dimx)
				end_x = dimx-1;
			if (end_y >= dimy)
				end_y = dimy-1;
			
			if (sample > 0) {
				for (int x2 = start_x ; x2 <= end_x ; x2++) {
					for (int y2 = start_y ; y2 <= end_y ; y2++) {
						if ((x2 != x || y2 != y) && dc.samples[x2+y2*dimx] == -2) {
							changeTaskResult(x2+y2*dimx, 0);
						}
					}
				}
			} else if (!denyApproximationSamples.contains((Integer)s)){
				boolean allLowerEqual0 = true;
				exit : for (int x2 = start_x ; x2 <= end_x ; x2++) {
					for (int y2 = start_y ; y2 <= end_y ; y2++) {
						if (dataContainer.samples[x2+y2*dimx] > 0) {
							allLowerEqual0 = false;
							break exit;
						}
					}
				}
				if (allLowerEqual0) {
					dataContainer.samples[s] = -2;
					task.results[c] = -2;
				}
			}
			c++;
		}
	}

	private void changeTaskResult(int sample, int result) {
		int offset = sample%sample_size;
		int startIndex = sample-offset;
		tasks_by_start_index.get(startIndex).results[offset] = result;
		//TODO What if the Task is done on another machine?
		dataContainer.samples[sample] = result;
	}

	private void postTaskFinished(Task task) {
		
	}
}
