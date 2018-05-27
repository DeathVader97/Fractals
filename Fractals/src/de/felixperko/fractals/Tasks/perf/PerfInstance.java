package de.felixperko.fractals.Tasks.perf;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public class PerfInstance {
	
	static CategoryLogger warningLogger = CategoryLogger.WARNING_SERIOUS;
	static CategoryLogger perf = PerformanceMonitor.logger;
	
	public static PerfInstance begin(String name){
		return new PerfInstance(name);
	}
	
	String name;
	
	long startTime;
	long endTime;
	
	boolean started;
	boolean ended;
	boolean logging;
	boolean refreshWarning = true;
	
	List<Runnable> runWhenEnded = new ArrayList<>();
	
	public PerfInstance(String name){
		this.name = name;
	}
	
	public void setLogging(){
		if (logging)
			warningLogger.log("perf/measure", "PerfInstance: logging is already enabled for '"+name+"'! [PerfLogger.setLogging()]");
		logging = true;
		PerfInstanceStats.addInstance(this, name);
	}
	
	public boolean isLogging(){
		return logging;
	}
	
	public void start(){
		if (refreshWarning && started)
			warningLogger.log("perf/measure", "PerfInstance was started while running already! [PerfLogger.start()]");
		started = true;
		startTime = System.nanoTime();
	}
	
	public void end(){
		if (refreshWarning && ended)
			warningLogger.log("perf/measure", "PerfInstance was ended while running already! [PerfLogger.end()]");
		endTime = System.nanoTime();
		ended = true;
		runWhenEnded.forEach(r -> r.run());
		runWhenEnded.clear();
	}
	
	public long getFinalDelta() throws IllegalStateException{
		if (!started)
			throw new IllegalStateException("getDelta() can only be called after measurement started and stopped");
		if (!ended)
			throw new IllegalStateException("getDelta() can only be called after measurement stopped");
		return endTime-startTime;
	}
	
	public long getCurrentDelta() throws IllegalStateException{
		if (!started)
			throw new IllegalStateException("getCurrentDelta() can only be called after measurement started");
		if (ended)
			throw new IllegalStateException("getCurrentDelta() cannot be called after measurement stopped");
		return System.nanoTime()-startTime;
	}
	
	public long getCurrentOrFinalDelta() throws IllegalStateException{
		if (!started)
			throw new IllegalStateException("getCurrentOrFinalDelta() can only be called after measurement started");
		return ended ? getFinalDelta() : getCurrentDelta();
	}
	
	public long getStartTime() throws IllegalStateException{
		if (startTime == 0)
			throw new IllegalStateException("getStartTime() can only be called after measurement started");
		return startTime;
	}

	public long getEndTime() throws IllegalStateException{
		if (endTime == 0)
			throw new IllegalStateException("getEndTime() can only be called after measurement ended");
		return endTime;
	}
	
	public double getDeltaInS(int precision) throws IllegalStateException{
		return NumberUtil.getRoundedDouble(NumberUtil.NS_TO_S*getFinalDelta(), precision);
	}

	public boolean isRefreshWarning() {
		return refreshWarning;
	}

	public void setRefreshWarning(boolean warnOnRefresh) {
		this.refreshWarning = warnOnRefresh;
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean hasEnded() {
		return ended;
	}

	public void executeWhenOrIfEnded(Runnable runnable) {
		if (ended)
			runnable.run();
		else
			runWhenEnded.add(runnable);
	}

	public void printSecondsToLog(int precision) {
		perf.log("instance/"+name, "Finished after "+getDeltaInS(precision)+"s");
	}
}
