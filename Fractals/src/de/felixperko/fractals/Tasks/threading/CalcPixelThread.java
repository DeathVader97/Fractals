package de.felixperko.fractals.Tasks.threading;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.data.Chunk;

public class CalcPixelThread extends FractalsThread {
	
	Lock lock = new ReentrantLock();
	
	Queue<Chunk> waitingChunks = new LinkedList<>();
	Set<Chunk> waitingChunkSet = new HashSet<>();
	Set<Chunk> finishedChunks = new HashSet<>();
	
	public CalcPixelThread(String name) {
		super(name, 5);
	}
	
	@Override
	public void run() {
		while (true) {
			while (!waitingChunks.isEmpty()) {
				synchronized (this) {
					
					Chunk c = waitingChunks.poll();
					if (c == null)
						continue;
					if (!c.isReadyToCalculate()) {
						waitingChunks.add(c);
						continue;
					}
					
					try {
						waitingChunkSet.remove(c);
						c.calculatePixels();
						c.setReadyToDraw(true);
						c.setRedrawNeeded(true);
//						finishedChunks.add(c);
						FractalsMain.mainWindow.canvas.getDisplay().asyncExec(() -> FractalsMain.mainWindow.setRedraw(true));
					} catch (Exception e) {
						if (c != null && !c.isDisposed())
							e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
	public void addChunk(Chunk c) {
		if (!waitingChunkSet.contains(c)) {
			finishedChunks.remove(c);
			waitingChunks.add(c);
			waitingChunkSet.add(c);
			interrupt();
		}
	}
	
	public boolean isFinished(Chunk c, boolean reset) {
		boolean finished = finishedChunks.contains(c);
		if (finished && reset)
			finishedChunks.remove(c);
		return finished;
	}

}
