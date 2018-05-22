package de.felixperko.fractals.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.state.State;

public class Logger {
	
	public static State<Integer> state = new State<>("loggercontrol", 0);
	static Map<String, List<String>> log = java.util.Collections.synchronizedMap(new HashMap<>());
	
	public static void log(String category, String msg) {
		List<String> list = log.get(category);
		if (list == null) {
			list = new ArrayList<>();
			log.put(category, list);
		}
		list.add(msg);
		System.out.println(logString(category,msg));
		state.setValue(0);
	}
	
	public static List<String> getLog(){
		List<String> res = new ArrayList<>();
		log.forEach((k,v) -> v.forEach(msg -> res.add(logString(k,msg))));
		return res;
	}
	
	public static List<String> getLog(String category){
		return log.get(category);
	}
	
	private static String logString(String cat, String msg) {
		return "["+cat+"] "+msg;
	}
}
