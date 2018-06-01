package de.felixperko.fractals.Tasks.perf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;

public class PerfInstance implements Comparable<PerfInstance>{
	
	private static final Comparator<PerfInstance> COMPARATOR_REVERSED = new Comparator<PerfInstance>() {

		@Override
		public int compare(PerfInstance o1, PerfInstance o2) {
			return o2.compareTo(o1);
		}
	};
	static CategoryLogger warningLogger = CategoryLogger.WARNING_SERIOUS;
	static CategoryLogger perf = PerformanceMonitor.logger;
	
	public static PerfInstance createNewAndBegin(String name){
		PerfInstance inst = new PerfInstance(name);
		inst.start();
		return inst;
	}
	
	public static PerfInstance createNewSubInstanceAndBegin(String name, PerfInstance parent) {
		PerfInstance inst = new PerfInstance(name);
		parent.addChild(inst);
		inst.start();
		return inst;
	}
	
	String name;
	
	long startTime;
	long endTime;
	
	boolean started;
	boolean ended;
	boolean logging;
	boolean refreshWarning = true;
	
	Map<String, PerfInstance> childInstances = new HashMap<>();
	
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
	
	public PerfInstance addChild(PerfInstance child) {
		childInstances.put(child.getName(), child);
		return child; //for chaining
	}
	
	public PerfInstance getChild(String childName) {
		return childInstances.get(childName);
	}
	
	public Map<String, PerfInstance> getAllChilds(){
		return childInstances;
	}

	public String getName() {
		return name;
	}

	public void printChildSecondsToLog(int precision) {
		Map<String, String> messages = new LinkedHashMap<>();
		String parentPrefix = "instance/"+name;
		messages.put(parentPrefix, "Finished after "+getDeltaInS(precision)+"s");
		int maxPrefixLength = parentPrefix.length();
		int maxMessageLength = messages.get(parentPrefix).length();
		for (PerfInstance child : childInstances.values().stream().sorted(COMPARATOR_REVERSED).collect(Collectors.toList())) {
			String childPrefix = getChildLogPrefix(child);
			String childMessage = getPercentageOfParent(child, precision)+"% ("+child.getDeltaInS(precision)+"s)";
			messages.put(childPrefix, childMessage);
			if (childPrefix.length() > maxPrefixLength)
				maxPrefixLength = childPrefix.length();
			if (childMessage.length() > maxMessageLength)
				maxMessageLength = childMessage.length();
		}
		StringBuilder separator = new StringBuilder();
		for (int i = 0 ; i < maxPrefixLength-parentPrefix.length()+maxMessageLength ; i++)
			separator.append("-");
		perf.log(parentPrefix, separator.toString());
		for (Entry<String, String> e : messages.entrySet()) {
			StringBuilder message = new StringBuilder();
			int whitespaces = maxPrefixLength - e.getKey().length();
			for (int i = 0 ; i < whitespaces ; i++)
				message.append(" ");
			message.append(e.getValue());
			perf.log(e.getKey(), message.toString());
		}
	}
	
	private String getChildLogPrefix(PerfInstance child) {
		return "instance/"+name+"/"+child.getName();
	}

	private double getPercentageOfParent(PerfInstance child, int precision) {
		return NumberUtil.getRoundedPercentage((((double)child.getFinalDelta())/getFinalDelta()), precision);
	}

	@Override
	public int compareTo(PerfInstance o) {
		return Long.compare(getCurrentOrFinalDelta(), o.getCurrentOrFinalDelta());
	}
}
