package de.felixperko.fractals;

public class Task {
	
	final static int STATE_NOT_ASSIGNED = 0,
			STATE_ASSINGED = 1,
			STATE_FINISHED = 2;
	
	int start, end;
	int state = 0;
	
	public Task(int start, int end) {
		this.start = start;
		this.end = end;
	}
}
