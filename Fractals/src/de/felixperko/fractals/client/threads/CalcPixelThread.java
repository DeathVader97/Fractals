package de.felixperko.fractals.client.threads;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.gui.PhaseProgressionCanvas;
import de.felixperko.fractals.client.rendering.chunkprovider.ChunkProvider;
import de.felixperko.fractals.client.rendering.chunkprovider.LocalChunkProvider;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.threads.FractalsThread;

public class CalcPixelThread extends FractalsThread {
	
	Lock lock = new ReentrantLock();
	
	Queue<Chunk> waitingChunks = new LinkedList<>();
	Set<Chunk> waitingChunkSet = new HashSet<>();
	Set<Chunk> finishedChunks = new HashSet<>();
	
	LocalChunkProvider localChunkProvider;
	
	public CalcPixelThread(String name) {
		super(name, 5);
	}
	
	@Override
	public void run() {
		setPhase(PHASE_WAITING);
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
					setPhase(PHASE_WORKING);
					try {
						waitingChunkSet.remove(c);
						c.calculatePixels();
						c.setReadyToDraw(true);
						c.setRedrawNeeded(true);
//						finishedChunks.add(c);
						if (localChunkProvider != null)
							localChunkProvider.addChunk(c);
						FractalsMain.mainWindow.canvas.getDisplay().asyncExec(() -> FractalsMain.mainWindow.setRedraw(true));
					} catch (Exception e) {
						if (c != null && !c.isDisposed())
							e.printStackTrace();
					}
				}
			}
			setPhase(PHASE_WAITING);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	
	public void addChunk(Chunk c) {
//		if (!waitingChunkSet.contains(c)) {
//			finishedChunks.remove(c);
			waitingChunks.add(c);
			waitingChunkSet.add(c);
//			interrupt();
//		}
	}
	
	public boolean isFinished(Chunk c, boolean reset) {
		boolean finished = finishedChunks.contains(c);
		if (finished && reset)
			finishedChunks.remove(c);
		return finished;
	}

	public void setLocalChunkProvider(LocalChunkProvider chunkProvider) {
		this.localChunkProvider = chunkProvider;
	}
	
	public synchronized void reset() {
		waitingChunks.clear();
		waitingChunkSet.clear();
		finishedChunks.clear();
	}

}
