package de.felixperko.fractals.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.state.State;

public class Logger {
	
	public static State<Integer> state = new State<>("loggercontrol", 0);
	static Map<String, List<String>> logMap = java.util.Collections.synchronizedMap(new HashMap<>());
	static ArrayList<String> log = new ArrayList<>();
	
	public static void log(String category, String msg) {
		List<String> list = logMap.get(category);
		if (list == null) {
			list = new ArrayList<>();
			logMap.put(category, list);
		}
		list.add(msg);
		String str = logString(category,msg);
		log.add(str);
		System.out.println(str);
		state.setValue(0);
	}
	
	public static List<String> getLog(){
//		List<String> res = new ArrayList<>();
//		logMap.forEach((k,v) -> v.forEach(msg -> res.add(logString(k,msg))));
//		return res;
		return log;
	}
	
	public static List<String> getLog(String category){
		return logMap.get(category);
	}
	
	private static String logString(String cat, String msg) {
		return "["+cat+"] "+msg;
	}
}
