package de.felixperko.fractals.client.threads;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.gui.PhaseProgressionCanvas;
import de.felixperko.fractals.client.gui.RedrawInfo;
import de.felixperko.fractals.client.rendering.chunkprovider.ChunkProvider;
import de.felixperko.fractals.client.rendering.chunkprovider.LocalChunkProvider;
import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.threads.FractalsThread;
import de.felixperko.fractals.server.util.Position;

public class CalcPixelThread extends FractalsThread {
	
	Lock lock = new ReentrantLock();
	
	Queue<Chunk> waitingChunks = new PriorityBlockingQueue<>();
	Set<Chunk> waitingChunkSet = Collections.synchronizedSet(new HashSet<>());
	Set<Chunk> finishedChunks = new HashSet<>();
	
	LocalChunkProvider localChunkProvider;
	
	Renderer renderer;
	
	public CalcPixelThread(String name) {
		super(name, 5);
	}
	
	@Override
	public void run() {
		setPhase(PHASE_IDLE);
		while (true) {
			while (!waitingChunks.isEmpty()) {
				synchronized (this) {
					
//					System.out.println(waitingChunks.size());
					Chunk c = null;
					try {
					
						c = waitingChunks.poll();
						
						if (c != null && !c.isReadyToCalculate()) {
							waitingChunks.add(c);
						}
						else if (c != null){
							setPhase(PHASE_WORKING);
								waitingChunkSet.remove(c);
								c.calculatePixels(renderer);
								c.setReadyToDraw(true);
								c.setRedrawNeeded(true);
		//						finishedChunks.add(c);
								if (localChunkProvider != null)
									localChunkProvider.addChunk(c);
								FractalsMain.mainWindow.canvas.getDisplay().asyncExec(() -> FractalsMain.mainWindow.setRedraw(true));
	//							FractalsMain.mainWindow.canvas.getDisplay().asyncExec(() -> {
	//								Position screenOffset = c.getGrid().getScreenOffset(c.getGridPosition());
	//								Position screenChunkDimensions = c.getGrid().getScreenOffset(new Position(1,1).add(c.getGridPosition())).sub(screenOffset);
	//								FractalsMain.mainWindow.addRedrawInfo(new RedrawInfo(screenOffset, screenChunkDimensions, c.getPriority()));
	//							});
						}
						
					} catch (Exception e) {
						if (c != null && !c.isDisposed())
							e.printStackTrace();
					}
					
				}
			}
			setPhase(PHASE_IDLE);
			try {
				Thread.sleep(1);
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

	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

}
