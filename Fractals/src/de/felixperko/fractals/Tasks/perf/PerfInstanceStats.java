package de.felixperko.fractals.Tasks.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfInstanceStats {
	
	static Map<String, List<PerfInstance>> map = new HashMap<>();
	static Map<String, Integer> finishedCount = new HashMap<>();
	static Map<String, Double> averageTimes = new HashMap<>();
	
	public static void addInstance(PerfInstance instance, String name){
		List<PerfInstance> list = map.get(name);
		if (list == null){
			list = new ArrayList<>();
			map.put(name, list);
		}
		list.add(instance);
		instance.executeWhenOrIfEnded(new Runnable() {
			@Override
			public void run() {
				synchronized (this) {
					Integer currentAmount = finishedCount.getOrDefault(name, 0);
					double oldWeight = (currentAmount/(double)(currentAmount+1));
					double newWeight = 1-oldWeight;
					averageTimes.put(name, (instance.getFinalDelta() * newWeight) + (averageTimes.get(name) * oldWeight));
					finishedCount.put(name, currentAmount+1);
				}
			}
		});
	}
	
	public static Long getAverageTime(String name){
		return averageTimes.getOrDefault(name, new Double(0)).longValue();
	}
	
	public static synchronized long getLastTime(String name){
		List<PerfInstance> list = map.get(name);
		if (list == null){
			return 0;
		}
		return list.get(list.size()-1).getFinalDelta();
	}
	
	public synchronized void reset(){
		map.clear();
		finishedCount.clear();
		averageTimes.clear();
	}
}
