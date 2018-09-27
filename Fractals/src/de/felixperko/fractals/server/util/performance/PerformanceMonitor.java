package de.felixperko.fractals.server.util.performance;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.tasks.WorkerPhase;
import de.felixperko.fractals.server.tasks.WorkerPhaseChange;
import de.felixperko.fractals.server.threads.ThreadManager;
import de.felixperko.fractals.server.threads.WorkerThread;
import de.felixperko.fractals.server.util.CategoryLogger;

public class PerformanceMonitor {
	
	public static CategoryLogger logger = new CategoryLogger("perf", Color.BLUE);
	
	static WorkerPhase PHASE_UNDEFINED = new WorkerPhase("undefined", Color.MAGENTA);
	ThreadManager threadManager;
	
	Map<String, PerfInstanceStats> stats = new HashMap<>();
	
	long startTime = 0;
	
	public PerformanceMonitor() {
	}
	
	public void startPhase() throws IllegalStateException{
		
		if (threadManager == null) {
			threadManager = FractalsMain.threadManager;
			if (threadManager == null)
				throw new IllegalStateException("PerformanceMonitor can't start: ThreadManager not set.");
		}
		
		for (WorkerThread thread : threadManager.getThreads()) {
			thread.resetPerformanceMonitor(this);
		}
		startTime = System.nanoTime();
	}
	
	public void endPhase() {
		long endTime = System.nanoTime();
		long totalTime = endTime-startTime;
		logger.log("");
		logger.log("----Performance Review---- Total Time: "+NumberUtil.getRoundedDouble(NumberUtil.NS_TO_S*totalTime, 2)+"s");
		
		for (WorkerThread thread : threadManager.getThreads()) {
			
			HashMap<WorkerPhase, Long> phaseTimes = new HashMap<>();
			WorkerPhase currentPhase = PHASE_UNDEFINED;
			long lastTime = 0;
			boolean first = true;
			
			for (WorkerPhaseChange change : thread.getPerformanceData()) {
				if (!first) {
					phaseTimes.put(currentPhase, phaseTimes.getOrDefault(currentPhase,0L) + (change.getTime()-lastTime));
				} else
					first = false;
				currentPhase = change.getPhase();
				lastTime = change.getTime();
			}
			
			phaseTimes.put(currentPhase, (phaseTimes.containsKey(currentPhase) ? phaseTimes.get(currentPhase) : 0) + (endTime - lastTime));
			HashMap<WorkerPhase, Long> sortedMap = phaseTimes.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			StringBuilder builder = new StringBuilder();
			builder.append((int)(thread.getIterations()/(totalTime/1000000.))).append("	it/ms	");
			for (Entry<WorkerPhase, Long> e : sortedMap.entrySet()) {
				builder.append(e.getKey().getName()).append(":").append(Math.round(e.getValue()*1000./totalTime)/10.).append("%	");
			}
			logger.log("report/"+thread.getName(), builder.toString());
		}
	}
}
