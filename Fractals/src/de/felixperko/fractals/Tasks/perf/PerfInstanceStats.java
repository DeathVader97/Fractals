package de.felixperko.fractals.Tasks.perf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PerfInstanceStats {
	
	static Map<String, List<PerfInstance>> map = new HashMap<>();
	static Map<String, Integer> finishedCount = new HashMap<>();
//	static Map<String, Double> averageTimes = new HashMap<>();
	
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
//					averageTimes.put(name, (instance.getFinalDelta() * newWeight) + (averageTimes.get(name) * oldWeight));
					finishedCount.put(name, currentAmount+1);
				}
			}
		});
	}
	
	public static Long getAverageTime(String name){
		double avgTime = 0;
		int count = 0;
		for (PerfInstance inst : map.get(name)) {
			if (inst.hasEnded()) {
				avgTime += inst.getFinalDelta();
				count++;
			}
		}
		if (count == 0)
			return 0L;
		return (long)(avgTime/count);
	}
	
	public static LinkedHashMap<String, Long> getAverageTimeForChildren(String... namePath) throws IllegalArgumentException{
		
		if (namePath == null || namePath.length == 0)
			throw new IllegalArgumentException("the name path is empty or null");
		
		HashMap<String, Long> times = new HashMap<>();
		double count = 0;
		
		List<PerfInstance> parentInstances = map.get(namePath[0]);
		if (parentInstances == null)
			throw new IllegalArgumentException("the root isn't logged. index=0 value='"+namePath[0]+"'");
		
		for (PerfInstance currentInstance : parentInstances) {
			//go down to desired layer
			for (int i = 1 ; i < namePath.length ; i++) {
				currentInstance = currentInstance.getChild(namePath[i]);
				if (currentInstance == null)
					throw new IllegalArgumentException("the path cannot be resolved. index="+i+" value='"+namePath[i]+"'");
			}
			//loop through childs
			for (PerfInstance child : currentInstance.getAllChilds().values()) {
				if (child.hasEnded()) {
					times.put(child.getName(), times.getOrDefault(child.getName(), 0L)+child.getFinalDelta());
					count++;
				}
			}
		}
		
		ArrayList<Entry<String, Long>> sortedList = new ArrayList<>();
		for (Entry<String, Long> e : times.entrySet()) {
			e.setValue((long)(e.getValue()/count));
			sortedList.add(e);
		}
		
		Collections.sort(sortedList, new Comparator<Entry<String, Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		
		LinkedHashMap<String, Long> res = new LinkedHashMap<>();
		sortedList.forEach(e -> res.put(e.getKey(), e.getValue()));
		return res;
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
	}
}
