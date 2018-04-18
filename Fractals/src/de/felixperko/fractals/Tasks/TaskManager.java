package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;

public class TaskManager {
	
	DataDescriptor dd;
	DataContainer dc;
	
	int sample_size = 10000;
	int iteration_step_size = 100;
	
	AtomicInteger unfinishedTasksCount = new AtomicInteger();
	
	List<AtomicInteger> depth_unfinishedTaskCount = new ArrayList<>();
	List<AtomicInteger> depth_closedIterations = new ArrayList<>();
	List<AtomicInteger> depth_cumulativeClosedIterations = new ArrayList<>();
	HashMap<Integer, Integer> depth_to_index = new HashMap<>();
	
	HashMap<Integer, Task> tasks_by_start_index = new HashMap<>();
	
	long generation_time = 0;
	
	List<Task> openTasks = Collections.synchronizedList(new ArrayList<>());
	
	Random random = new Random(42);
	int jobId = 0;
	
	int finishedDepth = 0;
	
	boolean finished = false;
	
	int minimum_skip_finish = 1000;
	double skip_max_closed_relative = 0.0001;
	
	public double last_step_closed_relative = 1;
	public int last_step_closed_total = 0;
//	ArrayList<Task> activeTasks = new ArrayList<>();
		
	public TaskManager(DataDescriptor dd, DataContainer dc) {
		this.dd = dd;
		this.dc = dc;
	}
	
	public synchronized void generateTasks() {
//		sample_size = dd.dim_sampled_x*dd.dim_sampled_y;
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
//			generateAndAddChildren(maxIterations);
////			generateTasksInSampleRange(maxIterations);
		}
		generateParents(iteration_step_size);
//		for (int i = maxIterations-remain ; i > iteration_step_size ; i -= iteration_step_size) {
//			prepare_depth(i, index);
//			generateAndAddChildren(i);
//			generateTasksInSampleRange(i);
//		}
		addParents();
		
		finished = unfinishedTasksCount.get() == 0;
		System.out.println("generated "+unfinishedTasksCount.get()+" tasks");
	}

	private void prepare_depth(int depth, int index) {
		depth_to_index.put(depth, index);
		depth_closedIterations.add(new AtomicInteger());
		depth_unfinishedTaskCount.add(new AtomicInteger());
		depth_cumulativeClosedIterations.add(new AtomicInteger());
	}
	
	HashMap<Integer, Task> parents = new HashMap<>();
	
	private void generateParents(int depth) {
		parents.clear();
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		System.out.println("generating "+samples_total+" samples");
		int start = 0;
		int c = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			parents.put(end, new Task(start, end, depth, jobId));
			System.out.println("samples "+start+" - "+end);
			start = end;
			c++;
		}
		if (start < samples_total) {
			parents.put(samples_total, new Task(start, samples_total, depth, jobId));
			c++;
		}
		System.out.println("generated "+c+" parents");
	}
	
	public void addParents() {
		Collection<Task> coll = parents.values();
		for (Task t : coll) {
			tasks_by_start_index.put(t.startSample, t);
		}
		openTasks.addAll(coll);
		unfinishedTasksCount.addAndGet(coll.size());
		for (AtomicInteger i : depth_unfinishedTaskCount) {
			i.set(coll.size());
		}
//		depth_unfinishedTaskCount.get(depth_to_index.get(iteration_step_size)).set(coll.size());
	}
	
	private void generateAndAddChildren(int depth) {

		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		int start = 0;
		int c = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			openTasks.add(parents.get(end).successor(depth));
		}
		unfinishedTasksCount.addAndGet(c);
		depth_unfinishedTaskCount.get(depth_to_index.get(depth)).set(c);
	}
		
	@Deprecated
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
		if (openTasks.size() <= 0)
			return null;
		Task task = openTasks.get(openTasks.size()-1);
		openTasks.remove(task);
		task.setState(Task.STATE_ASSINGED);
		return task;
	}
	
	public void taskFinished(Task task) {
//		System.out.println(Arrays.toString(task.results));
		if (task.jobId != jobId)
			return;
		System.arraycopy(task.results, 0, dc.samples, task.startSample, task.endSample-task.startSample);
		
//		int dimx = dd.dim_sampled_x;
//		int dimy = dd.dim_sampled_y;
//		int c = 0;
//		for (int s = task.startSample ; s < task.endSample-3 ; s++) {
//			int y = s/dimx;
//			int x = s%dimx;
//			int sample = dc.samples[s];
//			int start_x = x == 0 ? 0 : x-1;
//			int end_x = x == dimx-1 ? x : x+1;
//			int start_y = y == 0 ? 0 : y-1;
//			int end_y = y == dimy-1 ? y : y+1;
//			if (sample > 0) {
//
//				for (int x2 = start_x ; x2 <= end_x ; x2++) {
//					for (int y2 = start_y ; y2 <= end_y ; y2++) {
//						if ((x2 != x || y2 != y) && dc.samples[x2+y2*dimx] == -2) {
//							changeTaskResult(x2+y2*dimx, 0);
//						}
//					}
//				}
////				if (x > 0 && dc.samples[s-1] == -2) {
////					changeTaskResult(s-1, 0);
////				}
////				if (x < dimx-1 && dc.samples[s+1] == -2) {
////					changeTaskResult(s+1, 0);
////				}
////				if (y > 0 && dc.samples[s-dimx] == -2) {
////					changeTaskResult(s-dimx, 0);
////				}
////				if (y < dimy-1 && dc.samples[s+dimx] == -2) {
////					changeTaskResult(s+dimx, 0);
////				}
//			} else {
//				boolean allLowerEqual0 = true;
//				exit : for (int x2 = start_x ; x2 <= end_x ; x2++) {
//					for (int y2 = start_y ; y2 <= end_y ; y2++) {
//						if (dc.samples[x2+y2*dimx] > 0) {
//							allLowerEqual0 = false;
//							break exit;
//						}
//					}
//				}
//				if (allLowerEqual0) {
//					dc.samples[s] = -2;
//					task.results[c] = -2;
//				}
////				if ((x == 0 || dc.samples[s-1] <= 0) && (x == dimx-1 || dc.samples[s+1] <= 0)
////						&& (y == dimy-1 || dc.samples[s+dimx] <= 0) && (y == 0 || dc.samples[s-dimx] <= 0)) {
////					dc.samples[s] = -2;
////					task.results[c] = -2;
////				}
//			}
//			c++;
//		}
		
		System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, task.startSample, task.endSample-task.startSample);
		System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, task.startSample, task.endSample-task.startSample);
		
		postTaskFinish(task);
	}

	private void changeTaskResult(int sample, int result) {
		int offset = sample%sample_size;
		int startIndex = sample-offset;
		tasks_by_start_index.get(startIndex).results[offset] = result;
		dc.samples[sample] = result;
	}

	private synchronized void postTaskFinish(Task task) {
		if (finished)
			return;
		Integer depth = task.getMaxIterations();
		Integer depth_index = depth_to_index.get(depth);
		
		if (depth_index == null) {
			System.err.println("error: depth index not found! ("+depth+")");
			depth_to_index.forEach((k,v) -> System.out.println(" - "+k+" ["+v+"]"));
		} else {
			int prev_depth = task.getPreviousMaxIterations();
			int closedIterations = 0;
			for (int i = 0 ; i < task.results.length ; i++) {
				if (task.currentIterations[i] > prev_depth && task.currentIterations[i] != depth)
					closedIterations++;
			}
			if (depth_closedIterations.size() != 0) {
				int totalClosedAtCurrentDepth = depth_closedIterations.get(depth_index).addAndGet(closedIterations);
				int unfinishedTasks = depth_unfinishedTaskCount.get(depth_index).decrementAndGet();
//				System.out.println("finished t. depth="+depth+" "+unfinishedTasks+" left");
				if (unfinishedTasks == 0) { //Tasks at iteration depth done -> save cumulative and check if result good enough
					int prev_cul = depth_index == 0 ? 0 : depth_cumulativeClosedIterations.get(depth_index - 1).get();
					int cul = prev_cul + totalClosedAtCurrentDepth;
					double rel = totalClosedAtCurrentDepth/(double)cul;
					depth_cumulativeClosedIterations.get(depth_index).set(cul);
					System.out.println("closed "+totalClosedAtCurrentDepth+" iterations at depth "+depth+" "+rel);
					finishedDepth = depth;
					if (rel != Double.NaN) {
						last_step_closed_relative = rel;
						last_step_closed_total = cul;
					}
					if (cul > minimum_skip_finish && rel < skip_max_closed_relative) { //further iterations probably wont improve quality much...
						finished = true;
	//					for (int i = 0 ; i < dc.currentSampleIterations.length ; i++) { //mark not finished samples as in the set
	//						if (dc.currentSampleIterations[i] == depth && dc.samples[i] == 0) {
	//							dc.samples[i] = -1;
	//						}
	//					}
						System.out.println("finished job after "+ ((System.nanoTime()-generation_time)/1000000)/1000.+"s (reached quality goal)");
						clearTasks();
					}
				}
			}
		}
		if (task.getMaxIterations() < dd.getMaxIterations()) {
			task.setState(Task.STATE_NOT_ASSIGNED);
			int newMaxIterations = task.getMaxIterations() + iteration_step_size;
			if (newMaxIterations > dd.getMaxIterations())
				newMaxIterations = dd.getMaxIterations();
			task.setMaxIterations(newMaxIterations);
			openTasks.add(0, task);
		} else {
			if (unfinishedTasksCount.decrementAndGet() == 0) {
				finished = true;
				clearTasks();
				System.out.println("finished job after "+ ((System.nanoTime()-generation_time)/1000000)/1000.+"s (reached max iterations)");
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
