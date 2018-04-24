package de.felixperko.fractals.Tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import de.felixperko.fractals.DataContainer;
import de.felixperko.fractals.DataDescriptor;
import de.felixperko.fractals.FractalsMain;

public class TaskManager {
	
	DataDescriptor dd;
	DataContainer dc;
	
	int sample_size = 10000;
	int iteration_step_size = 10000;
	
	AtomicInteger unfinishedTasksCount = new AtomicInteger();
	
	List<AtomicInteger> depth_unfinishedTaskCount = new ArrayList<>();
	List<AtomicInteger> depth_closedIterations = new ArrayList<>();
	Map<Integer, Integer> depth_to_index = Collections.synchronizedMap(new HashMap<>());
	
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
	
	boolean fastApproximation = true;
	float denyApproximationSamplesChance = 0.0001f;
	HashSet<Integer> denyApproximationSamples = new HashSet<>();
//	ArrayList<Task> activeTasks = new ArrayList<>();
	
	ArrayList<Task> workingTasks = new ArrayList<>();
		
	public TaskManager(DataDescriptor dd, DataContainer dc) {
		this.dd = dd;
		this.dc = dc;
	}
	
	public synchronized void generateTasks() {
		FractalsMain.performanceMonitor.startPhase();
		int total_samples = dd.dim_sampled_x*dd.dim_sampled_y;
		for (int i = 0 ; i < total_samples ; i += Math.round(1/denyApproximationSamplesChance)) {
			denyApproximationSamples.add(i);
		}
		
		jobId = random.nextInt();
		generation_time = System.nanoTime();
		int maxIterations = dd.maxIterations;
		
		int remain = maxIterations % iteration_step_size;
		
		int index = 0;
		for (int depth = iteration_step_size ; depth <= maxIterations ; depth += iteration_step_size) {
			prepare_depth(depth, index);
			index++;
		}
		if (remain != 0)
			prepare_depth(maxIterations, index);
		generateTasks(iteration_step_size);
		
		finished = unfinishedTasksCount.get() == 0;
		System.out.println("generated "+unfinishedTasksCount.get()+" tasks");
	}

	private void prepare_depth(int depth, int index) {
		depth_to_index.put(depth, index);
		depth_closedIterations.add(new AtomicInteger());
		depth_unfinishedTaskCount.add(new AtomicInteger());
	}
	
	private void generateTasks(int depth) {
		int samples_total = dd.dim_sampled_x*dd.dim_sampled_y;
		System.out.println("generating "+samples_total+" samples");
		int start = 0;
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			generateTask(depth, end, start);
			start = end;
		}
		if (start < samples_total)
			generateTask(depth, samples_total, start);
		int count = openTasks.size();
		unfinishedTasksCount.addAndGet(count);
		for (AtomicInteger i : depth_unfinishedTaskCount) {
			i.set(count);
		}
		System.out.println("generated "+count+" tasks");
	}

	private void generateTask(int depth, int samples_total, int start) {
		Task t = new Task(start, samples_total, depth, jobId);
		tasks_by_start_index.put(t.startSample, t);
		openTasks.add(t);
	}
	
	public synchronized Task getTask() {
		if (openTasks.size() <= 0)
			return null;
		Task task = openTasks.get(openTasks.size()-1);
		openTasks.remove(task);
		workingTasks.add(task);
		task.setState(Task.STATE_ASSINGED);
		return task;
	}
	
	public synchronized void taskFinished(Task task) {
		
		if (task.jobId != jobId)
			return;

		workingTasks.remove(task);
		int size = task.endSample - task.startSample;
		
		if (task.changedIndices.isEmpty()) {
			boolean done = true;
			for (int i = 0 ; i < size ; i++) {
				if (task.results[i] <= 0) {
					done = false;
					break;
				}
			}
			if (done) {
				for (int i = 0 ; i < depth_unfinishedTaskCount.size() ; i++) {
					depth_unfinishedTaskCount.get(i).decrementAndGet();
				}
				return;
			}
//			depth_unfinishedTaskCount.get(depth_to_index.get(task.getMaxIterations())).decrementAndGet();
		} else {
		
			System.arraycopy(task.results, 0, dc.samples, task.startSample, size);
			
			if (fastApproximation && cumulativeClosedIterations > minimum_skip_finish)
				updateCulling(task);
			
			System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, task.startSample, size);
			System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, task.startSample, size);
			System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, task.startSample, size);
		}
		
		postTaskFinish(task);
	}

	private void updateCulling(Task task) {
		if (task.jobId != jobId)
			return;
		int dimx = dd.dim_sampled_x;
		int dimy = dd.dim_sampled_y;
		int c = 0;
		for (int s = task.startSample ; s < task.endSample-3 ; s++) {
			int y = s/dimx;
			int x = s%dimx;
			int sample = dc.samples[s];
			int start_x = x == 0 ? 0 : x-1;
			int end_x = x == dimx-1 ? x : x+1;
			int start_y = y == 0 ? 0 : y-1;
			int end_y = y == dimy-1 ? y : y+1;
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
						if (dc.samples[x2+y2*dimx] > 0) {
							allLowerEqual0 = false;
							break exit;
						}
					}
				}
				if (allLowerEqual0) {
					dc.samples[s] = -2;
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
		dc.samples[sample] = result;
	}
	
	int cumulativeClosedIterations = 0;

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
			for (Integer i : task.changedIndices) {
				if (task.currentIterations[i] > prev_depth && task.currentIterations[i] != depth)
					closedIterations++;
			}
			if (depth_closedIterations.size() != 0) {
				int totalClosedAtCurrentDepth = depth_closedIterations.get(depth_index).addAndGet(closedIterations);
				int unfinishedTasks = depth_unfinishedTaskCount.get(depth_index).decrementAndGet();
//				System.out.println("finished t. depth="+depth+" "+unfinishedTasks+" left");
				if (unfinishedTasks <= 0) { //Tasks at iteration depth done -> save cumulative and check if result good enough
					cumulativeClosedIterations += totalClosedAtCurrentDepth;
					double rel = totalClosedAtCurrentDepth/(double)cumulativeClosedIterations;
					System.out.println("closed "+totalClosedAtCurrentDepth+" (total "+cumulativeClosedIterations+") iterations at depth "+depth+" "+rel);
					finishedDepth = depth;
					if (rel != Double.NaN) {
						last_step_closed_relative = rel;
						last_step_closed_total = cumulativeClosedIterations;
					}
					if (cumulativeClosedIterations > minimum_skip_finish && rel < skip_max_closed_relative) { //further iterations probably wont improve quality much...
						finish("reached quality goal");
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
				finish("reached max iterations");
			}
		}
	}

	private void finish(String msg) {
		finished = true;
		clearTasks();
		FractalsMain.performanceMonitor.endPhase();
		System.out.println("finished job after "+ ((System.nanoTime()-generation_time)/1000000)/1000.+"s ("+msg+")");
	}

	public void clearTasks() {
		openTasks.clear();
		workingTasks.clear();
		unfinishedTasksCount.set(0);
		depth_closedIterations.clear();
		depth_to_index.clear();
		depth_unfinishedTaskCount.clear();
		finishedDepth = 0;
		cumulativeClosedIterations = 0;
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

	public int getJobId() {
		return jobId;
	}
}
