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
import de.felixperko.fractals.FractalRenderer;
import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.util.CategoryLogger;

public class TaskManagerImpl implements TaskManager {
	
	CategoryLogger logger = CategoryLogger.INFO.createSubLogger("TaskManager");
	
	int sample_size = 1000;
	int iteration_step_size = 50;
	int iteration_step_size_incr = 100;
	int iteration_step_size_incr_incr = 500;
	
	AtomicInteger unfinishedTasksCount = new AtomicInteger();
	
	List<AtomicInteger> depth_unfinishedTaskCount = new ArrayList<>();
	List<AtomicInteger> depth_closedIterations = new ArrayList<>();
	Map<Integer, Integer> depth_to_index = Collections.synchronizedMap(new HashMap<>());
	Map<Integer, Integer> index_to_depth = Collections.synchronizedMap(new HashMap<>());
	
	HashMap<Integer, SequentialTask> tasks_by_start_index = new HashMap<>();
	
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
	
	FractalRenderer renderer;
		
	public TaskManagerImpl(FractalRenderer renderer) {
		this.renderer = renderer;
	}
	
	/* (non-Javadoc)
	 * @see de.felixperko.fractals.Tasks.TaskManager#generateTasks()
	 */
	@Override
	public synchronized void generateTasks() {
		DataDescriptor dd = renderer.getDataDescriptor();
		int total_samples = dd.getDim_sampled_x()*dd.getDim_sampled_y();
		
		//these samples will always get calculated
		for (int i = 0 ; i < total_samples*denyApproximationSamplesChance ; i++) {
			denyApproximationSamples.add((int)(Math.random()*total_samples));
		}
		
		jobId = random.nextInt();
		generation_time = System.nanoTime();
		int maxIterations = dd.getMaxIterations();
		int step = iteration_step_size;
		int index = 0;
		int depth = iteration_step_size;
		ArrayList<Integer> depthValues = new ArrayList<>();
		
		//set depth steps
		for ( ; depth <= maxIterations ; depth += step) {
			prepare_depth(depth, index);
			depthValues.add(depth);
			step += iteration_step_size_incr;
			iteration_step_size_incr = iteration_step_size_incr_incr;
			index++;
		}
		
		//set final step if necessary
		int remain = depth - maxIterations;
		if (remain != 0)
			prepare_depth(maxIterations, index);
		
		//generate the tasks
		generateTasks(dd, iteration_step_size);
		
		int count = unfinishedTasksCount.get();
		finished = count == 0;
	}

	private void prepare_depth(int depth, int index) {
		depth_to_index.put(depth, index);
		index_to_depth.put(index, depth);
		depth_closedIterations.add(new AtomicInteger());
		depth_unfinishedTaskCount.add(new AtomicInteger());
	}
	
	private void generateTasks(DataDescriptor dd, int depth) {
		int samples_total = dd.getDim_sampled_x()*dd.getDim_sampled_y();
		logger.log("generating "+samples_total+" samples");
		int start = 0;
		
		//generate tasks for chunks of indicies
		for (int end = start+sample_size ; end < samples_total ; end += sample_size) {
			generateTask(dd, depth, start, end);
			start = end;
		}
		//generate final task if necessary
		if (start < samples_total)
			generateTask(dd, depth, start, samples_total);
		
		int count = openTasks.size();
		
		//set unfinished task indicators to detect when all tasks (of a given depth) are done
		unfinishedTasksCount.addAndGet(count);
		for (AtomicInteger i : depth_unfinishedTaskCount) {
			i.set(count);
		}
		logger.log("generated "+count+" tasks");
	}

	private void generateTask(DataDescriptor dd, int depth, int start, int end) {
		SequentialTask t = new SequentialTask(start, end, depth, jobId, dd);
		tasks_by_start_index.put(t.startSample, t);
		openTasks.add(t);
	}
	
	@Override
	public synchronized Task getTask() {
		if (openTasks.size() <= 0) {
			return null;
		}
		Task task = openTasks.get(openTasks.size()-1);
		openTasks.remove(task);
		workingTasks.add(task);
		task.setState(SequentialTask.STATE_ASSINGED);
		return task;
	}
	
	@Override
	public synchronized void taskFinished(Task task) {
		DataContainer dc = renderer.getDataContainer();
		DataDescriptor dd = renderer.getDataDescriptor();
		long copyTime = 0;
		long cullingTime = 0;
		long postTime = 0;
		long t1 = System.nanoTime();
		if (!isJobActive(task.jobId))
			return;
		if (task instanceof SequentialTask) {
			SequentialTask sTask = (SequentialTask)task;
			try {
				workingTasks.remove(task);
				int size = sTask.endSample - sTask.startSample;
				
				if (task.changedIndices.isEmpty()) {
					boolean done = true;
					for (int i = 0 ; i < size ; i++) {
						if (task.results[i] <= 0) {
							done = false;
							break;
						}
					}
					if (done) {
						int depth = task.getMaxIterations();
						int prev_depth = task.getPreviousMaxIterations();
						int closedIterations = 0;
						for (Integer i : task.changedIndices) {
							if (task.currentIterations[i] > prev_depth && task.currentIterations[i] != depth)
								closedIterations++;
						}
		//				System.out.println("task is done... "+depth);
						int depth_index = depth_to_index.get(depth);
						int unfinishedTasks = depth_unfinishedTaskCount.get(depth_index).decrementAndGet();
						int totalClosedAtCurrentDepth = depth_closedIterations.get(depth_index).addAndGet(closedIterations);
						for (int i = depth_index+1 ; i < depth_unfinishedTaskCount.size() ; i++) {
							depth_unfinishedTaskCount.get(i).decrementAndGet();
						}
						if (unfinishedTasks == 0) { //Tasks at iteration depth done -> save cumulative and check if result good enough
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
						return;
					}
		//			depth_unfinishedTaskCount.get(depth_to_index.get(task.getMaxIterations())).decrementAndGet();
				} else {
					long startCopy = System.nanoTime();
					System.arraycopy(task.results, 0, dc.samples, sTask.startSample, size);
					long startCulling = System.nanoTime();
					if (fastApproximation && cumulativeClosedIterations > minimum_skip_finish)
						updateCulling(sTask);
					long endCulling = System.nanoTime();
					System.arraycopy(task.currentIterations, 0, dc.currentSampleIterations, sTask.startSample, size);
					System.arraycopy(task.currentpos_real, 0, dc.currentSamplePos_real, sTask.startSample, size);
					System.arraycopy(task.currentpos_imag, 0, dc.currentSamplePos_imag, sTask.startSample, size);
					long endCopy = System.nanoTime();
					cullingTime = endCulling-startCulling;
					copyTime = endCopy-startCopy - cullingTime;
				}
				long postStart = System.nanoTime();
				postTaskFinish(dd, task);
				long t2 = System.nanoTime();
				postTime = t2-postStart;
				double totalTime = t2-t1;
			} catch (Exception e) {
				if (!isJobActive(task.jobId))
					return;
				e.printStackTrace();
			}
		}
//		System.out.println("total save time: "+totalTime/1000000+"ms copy="+percent(totalTime,copyTime)+"% culling="+percent(totalTime,cullingTime)+"% post="+percent(totalTime,postTime)+"%");
	}
	
	public double percent(double totalTime, long time) {
		return ((double)Math.round(time*1000/totalTime))/10;
	}

	private void updateCulling(SequentialTask task) {
		DataDescriptor dd = renderer.getDataDescriptor();
		DataContainer dc = renderer.getDataContainer();
		if (task.jobId != jobId)
			return;
		int dimx = dd.getDim_sampled_x();
		int dimy = dd.getDim_sampled_y();
		
		//iterate changed indices of task
		for (Integer c : task.changedIndices) {
			int s = c+task.startSample;
			int y = s/dimx;
			int x = s%dimx;
			int sample = dc.samples[s];
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
			
			if (sample > 0) { //set surrounding samples to be calculated if this sample was finished
				for (int x2 = start_x ; x2 <= end_x ; x2++) {
					for (int y2 = start_y ; y2 <= end_y ; y2++) {
						if ((x2 != x || y2 != y) && dc.samples[x2+y2*dimx] == -2) {
							changeTaskResult(dc, x2+y2*dimx, 0);
						}
					}
				}
			} else if (!denyApproximationSamples.contains((Integer)s)){ //set sample to not be calculated if no surrounding sample is finished
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

	private void changeTaskResult(DataContainer dc, int sample, int result) {
		int offset = sample%sample_size;
		int startIndex = sample-offset;
		tasks_by_start_index.get(startIndex).results[offset] = result;
		dc.samples[sample] = result;
	}
	
	int cumulativeClosedIterations = 0;

	private synchronized void postTaskFinish(DataDescriptor dd, Task task) {
		if (finished)
			return;
		Integer depth = task.getMaxIterations();
		Integer depth_index = depth_to_index.get(depth);
		
		if (depth_index == null) {
			System.err.println("error: depth index not found! ("+depth+")");
//			depth_to_index.forEach((k,v) -> System.out.println(" - "+k+" ["+v+"]"));
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
//				System.out.println(depth+": "+unfinishedTasks);
//				System.out.println("finished t. depth="+depth+" "+unfinishedTasks+" left");
				if (unfinishedTasks == 0) { //Tasks at iteration depth done -> save cumulative and check if result good enough
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
			if (task.getMaxIterations() < dd.getMaxIterations()) {
				task.setState(SequentialTask.STATE_NOT_ASSIGNED);
				int newMaxIterations = index_to_depth.get(depth_index+1);
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
	}

	private void finish(String msg) {
		finished = true;
		clearTasks();
		FractalsMain.performanceMonitor.endPhase();
		logger.log("finished job after "+ ((System.nanoTime()-generation_time)/1000000)/1000.+"s ("+msg+")");
	}

	@Override
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

	public boolean isFinished() {
		return finished;
	}

	public int getFinishedDepth() {
		return finishedDepth;
	}

	public int getJobId() {
		return jobId;
	}

	@Override
	public boolean isJobActive(int jobId) {
		return jobId == this.jobId;
	}

	@Override
	public String getStateText() {
		return isFinished() ? "fertig" : ""+getFinishedDepth();
	}
}
