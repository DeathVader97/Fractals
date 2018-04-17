package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;

public class TaskManager {
	
	DataDescriptor dd;
	DataContainer dc;
	
	int sample_size = 10000;
	int iteration_step_size = 500;
	
	AtomicInteger unfinishedTasksCount = new AtomicInteger();
	
	List<AtomicInteger> depth_unfinishedTaskCount = new ArrayList<>();
	List<AtomicInteger> depth_closedIterations = new ArrayList<>();
	List<AtomicInteger> depth_cumulativeClosedIterations = new ArrayList<>();
	HashMap<Integer, Integer> depth_to_index = new HashMap<>();
	
	long generation_time = 0;
	
	ArrayList<Task> openTasks = new ArrayList<>();
	
	Random random = new Random(42);
	int jobId = 0;
	
	int finishedDepth = 0;
	
	boolean finished = false;
//	ArrayList<Task> activeTasks = new ArrayList<>();
		
	public TaskManager(DataDescriptor dd, DataContainer dc) {
		this.dd = dd;
		this.dc = dc;
	}
	
	public synchronized void generateTasks() {
		jobId = random.nextInt();
		generation_time = System.nanoTime();
		int maxIterations = dd.maxIterations;
		
		int remain = maxIterations % iteration_step_size;
		
		int index = 0;
		for (int depth = iteration_step_size ; depth <= maxIterations ; depth += iteration_step_size) {
			prepare_depth(depth, index);
			index++;
		}
		if (remain != 0) {
			prepare_depth(maxIterations, index);
			generateTasksInSampleRange(maxIterations);
		}
		for (int i = maxIterations-remain ; i > 0 ; i -= iteration_step_size) {
			generateTasksInSampleRange(i);
		}
		
		finished = unfinishedTasksCount.get() == 0;
		System.out.println("generated "+unfinishedTasksCount.get()+" tasks");
	}

	private void prepare_depth(int depth, int index) {
		depth_to_index.put(depth, index);
		depth_closedIterations.add(new AtomicInteger());
		depth_unfinishedTaskCount.add(new AtomicInteger());
		depth_cumulativeClosedIterations.add(new AtomicInteger());
	}

	private int generateTasksInSampleRange(int depth) {
		
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		int start = 0;
		int c = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			openTasks.add(new Task(start, end, depth, jobId));
			start = end;
			c++;
		}
		
		unfinishedTasksCount.addAndGet(c);
		depth_unfinishedTaskCount.get(depth_to_index.get(depth)).set(c);
		
		return c;
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
	
	int minimum_skip_finish = 1000;
	double skip_max_closed_relative = 0.002;
	
	public void taskFinished(Task task) {
//		System.out.println(Arrays.toString(task.results));
		if (task.jobId != jobId)
			return;
		System.arraycopy(task.results, 0, dc.samples, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, task.startSample, task.endSample-task.startSample);
		
		postTaskFinish(task);
		
		if (unfinishedTasksCount.decrementAndGet() == 0) {
			finished = true;
			clearTasks();
		}
	}

	private synchronized void postTaskFinish(Task task) {
		if (finished)
			return;
		Integer depth = task.maxIterations;
		Integer depth_index = depth_to_index.get(depth);
		
		if (depth_index == null) {
			System.err.println("error: depth index not found!");
		} else {
			int prev_depth = depth_index == 0 ? -1 : depth_to_index.entrySet().stream()
		              .filter(entry -> entry.getValue() == depth_index-1)
		              .map(Map.Entry::getKey)
		              .collect(java.util.stream.Collectors.toSet()).iterator().next();
			int closedIterations = 0;
			for (int i = 0 ; i < task.results.length ; i++) {
				if (task.currentIterations[i] > prev_depth && task.currentIterations[i] != depth)
					closedIterations++;
			}
			if (depth_closedIterations.size() != 0) {
				int totalClosedAtCurrentDepth = depth_closedIterations.get(depth_index).addAndGet(closedIterations);
				int unfinishedTasks = depth_unfinishedTaskCount.get(depth_index).decrementAndGet();
				
				if (unfinishedTasks == 0) { //Tasks at iteration depth done -> save cumulative and check if result good enough
					int prev_cul = depth_index == 0 ? 0 : depth_cumulativeClosedIterations.get(depth_index - 1).get();
					int cul = prev_cul + totalClosedAtCurrentDepth;
					double rel = totalClosedAtCurrentDepth/(double)cul;
					depth_cumulativeClosedIterations.get(depth_index).set(cul);
					System.out.println("closed "+totalClosedAtCurrentDepth+" iterations at depth "+depth+" "+rel);
					finishedDepth = depth;
					if (cul > minimum_skip_finish && rel < skip_max_closed_relative) { //further iterations probably wont improve quality...
						finished = true;
	//					for (int i = 0 ; i < dc.currentSampleIterations.length ; i++) { //mark not finished samples as in the set
	//						if (dc.currentSampleIterations[i] == depth && dc.samples[i] == 0) {
	//							dc.samples[i] = -1;
	//						}
	//					}
						System.out.println("finished job after "+ ((System.nanoTime()-generation_time)/1000000)/1000.+"s");
						clearTasks();
					}
				}
			}
		}
	}

	public void clearTasks() {
		openTasks.clear();
		unfinishedTasksCount.set(0);
		depth_closedIterations.clear();
		depth_cumulativeClosedIterations.clear();
		depth_to_index.clear();
		depth_unfinishedTaskCount.clear();
		finishedDepth = 0;
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

	public int getFinishedDepth() {
		return finishedDepth;
	}
}
