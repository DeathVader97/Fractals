package de.felixperko.fractals.Tasks;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public class PerformanceMonitor {
	
	CategoryLogger logger = new CategoryLogger("perf");
	
	static WorkerPhase PHASE_UNDEFINED = new WorkerPhase("undefined", Color.MAGENTA);
	ThreadManager threadManager;
	
	long startTime = 0;
	
	public PerformanceMonitor(ThreadManager threadManager) {
		this.threadManager = threadManager;
	}
	
	public void startPhase() {
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
					phaseTimes.put(currentPhase, phaseTimes.getOrDefault(currentPhase,0L) + (change.time-lastTime));
				} else
					first = false;
				currentPhase = change.phase;
				lastTime = change.time;
			}
			phaseTimes.put(currentPhase, (phaseTimes.containsKey(currentPhase) ? phaseTimes.get(currentPhase) : 0) + (endTime - lastTime));
			HashMap<WorkerPhase, Long> sortedMap = phaseTimes.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			StringBuilder builder = new StringBuilder();
			builder.append(thread.getName()).append(": ");
			builder.append((int)(thread.iterations/(totalTime/1000000.))).append("	it/ms	");
			for (Entry<WorkerPhase, Long> e : sortedMap.entrySet()) {
				builder.append(e.getKey().name).append(":").append(Math.round(e.getValue()*1000./totalTime)/10.).append("%	");
			}
			logger.log(builder.toString());
		}
	}
}
