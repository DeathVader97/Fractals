package de.felixperko.fractals.client.gui;

import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.tasks.WorkerPhase;
import de.felixperko.fractals.server.tasks.WorkerPhaseChange;
import de.felixperko.fractals.server.threads.FractalsThread;
import de.felixperko.fractals.server.threads.PerformanceThread;

public class PhaseProgressionCanvas extends Canvas {
	
	List<WorkerPhaseChange> phaseChanges;
	double timeframe = 1;
	
	PerformanceThread thread;

	public PhaseProgressionCanvas(Composite parent, int style, PerformanceThread thread) {
		super(parent, style);
		this.thread = thread;
		thread.setPhaseProgressionCanvas(this);
		addPaintListener(new ProcessPaintListener(this));
		setSize(100, 20);
	}
	
	public Point computeSize (int widthHint, int heightHint, boolean changed) {
		  Point initialSize = super.computeSize (widthHint, heightHint, changed);
		  initialSize.x = 100;
		  initialSize.y = 20;
		  return initialSize;
	}
	
	public List<WorkerPhaseChange> getPhaseChanges() {
		return phaseChanges;
	}

	public void setPhaseChanges(List<WorkerPhaseChange> phaseChanges) {
		this.phaseChanges = phaseChanges;
		setRedraw(true);
	}
	
	public double getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(double timeframe) {
		this.timeframe = timeframe;
		setRedraw(true);
	}
	
	class ProcessPaintListener implements PaintListener{
		
		PhaseProgressionCanvas canvas;
		
		public ProcessPaintListener(PhaseProgressionCanvas canvas) {
			this.canvas = canvas;
		}
		
		@Override
		public void paintControl(PaintEvent e) {
			GC gc = e.gc;
			int w = canvas.getBounds().width;
			int h = canvas.getBounds().height;
			List<WorkerPhaseChange> changes = canvas.getPhaseChanges();
			double time_left = canvas.getTimeframe();
			
			if (changes != null) {
				int count = changes.size();
				if (count <= 1) {
					WorkerPhase phase = count == 1 ? changes.get(0).getPhase() : FractalsThread.DEFAULT_PHASE;
					drawRect(gc, 0, w, h, phase.getSwtColor());
				}
				else {
					
					double x = w;
					WorkerPhaseChange prev_change = changes.get(changes.size()-1);
					
					double deltaT = (System.nanoTime() - prev_change.getTime())*NumberUtil.NS_TO_S;
					if (deltaT > time_left) { //cut off
						deltaT = time_left;
					}
					int deltaX = (int) Math.round((deltaT/time_left)*x);
					if (deltaX > 0) {
						x -= deltaX;
						time_left -= deltaT;
						drawRect(gc, (int)x, deltaX, h, prev_change.getSwtColor());
					}
					
					for (int i = changes.size()-2 ; i >= 0 ; i--) {
						WorkerPhaseChange change = changes.get(i);
						
						//get time
						deltaT = (prev_change.getTime() - change.getTime())*NumberUtil.NS_TO_S;
						if (deltaT > time_left) { //cut off
							deltaT = time_left;
							if (deltaT == 0) //no time left -> stop
								break;
						}
						
						deltaX = (int) Math.round((deltaT/time_left)*x);
						if (deltaX == 0) {
							prev_change = change;
							continue;
						}
						
						x -= deltaX;
						time_left -= deltaT;

						drawRect(gc, (int)x, deltaX, h, change.getSwtColor());
						prev_change = change;
					}
					
					if (time_left > 0) {
						drawRect(gc, 0, deltaX, h, FractalsThread.DEFAULT_PHASE.getSwtColor());
					}
				}
			}
		}

		private void drawRect(GC gc, int x, int deltaX, int h, Color color) {
			gc.setBackground(color);
			gc.fillRectangle(x, 0, deltaX, h);
//			System.out.println(x+"-"+(x+deltaX)+": "+color.toString());
		}
	}
	
	@Override
	public void dispose() {
		thread.removePhaseProgressionCanvas(this);
		super.dispose();
	}

}
