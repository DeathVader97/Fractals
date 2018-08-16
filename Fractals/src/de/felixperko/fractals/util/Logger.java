package de.felixperko.fractals.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.felixperko.fractals.state.State;

public class Logger {
	
	public static State<Integer> state = new State<>("loggercontrol", 0);
	static Map<String, List<Message>> logMap = java.util.Collections.synchronizedMap(new HashMap<>());
	static ArrayList<Message> log = new ArrayList<>();

	public static void log(Message message) {
		if (message == null)
			throw new IllegalArgumentException("The message can't be null");
		List<Message> list = logMap.get(message.getCategory());
		if (list == null) {
			list = new ArrayList<>();
			logMap.put(message.getCategory().category, list);
		}
		list.add(message);
		String str = message.getLogString();
		log.add(message);
		System.out.println(str);
		state.setValue(0);
	}
	
	public static List<Message> getLog(){
//		List<String> res = new ArrayList<>();
//		logMap.forEach((k,v) -> v.forEach(msg -> res.add(logString(k,msg))));
//		return res;
		return log;
	}
	
	public static List<Message> getLog(String category){
		return logMap.get(category);
	}
}
