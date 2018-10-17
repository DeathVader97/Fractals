package de.felixperko.fractals.client.rendering.renderer;

import static de.felixperko.fractals.server.util.performance.PerfInstance.createNewAndBegin;
import static de.felixperko.fractals.server.util.performance.PerfInstance.createNewSubInstanceAndBegin;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.chunkprovider.ChunkProvider;
import de.felixperko.fractals.client.rendering.chunkprovider.LocalChunkProvider;
import de.felixperko.fractals.client.rendering.painter.Painter;
import de.felixperko.fractals.client.rendering.painter.StandardPainter;
import de.felixperko.fractals.client.threads.CalcPixelThread;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.calculators.infrastructure.AbstractCalculator;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.data.Grid;
import de.felixperko.fractals.server.data.Location;
import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.tasks.ArrayListBatchTaskManager;
import de.felixperko.fractals.server.tasks.TaskManager;
import de.felixperko.fractals.server.util.CategoryLogger;
import de.felixperko.fractals.server.util.Position;
import de.felixperko.fractals.server.util.performance.PerfInstance;

public class GridRenderer extends AbstractRendererImpl {
	
	CategoryLogger log = new CategoryLogger("renderer", Color.BLUE);
	
	Painter painter = new StandardPainter();
	
	//TODO move to threadmanger
	
	Canvas canvas;
	
	ArrayListBatchTaskManager taskManager;
	Grid grid;
	
	double maxViewDistance = 5;
	
	double minGridX;
	double minGridY;
	double maxGridX;
	double maxGridY;
	
	double midGridX;
	double midGridY;
	
	boolean initialized = false;
	
	double insidePriorityMultiplier = 1;
	double outsidePriorityMultiplier = 5;
	
	ChunkProvider chunkProvider;
	
	boolean redraw = true;
	
	public GridRenderer() {
		grid = new Grid(this);
		chunkProvider = new LocalChunkProvider();
		CalcPixelThread calcPixelThread = FractalsMain.threadManager.getCalcPixelThread();
		calcPixelThread.setLocalChunkProvider((LocalChunkProvider)chunkProvider);
		calcPixelThread.start();
	}
	
	public void setTaskManager(TaskManager taskManager){
		if (!(taskManager instanceof ArrayListBatchTaskManager))
			throw new IllegalArgumentException();
		this.taskManager = (ArrayListBatchTaskManager) taskManager;
	}
	
	public void setGridMin(Position min) {
		minGridX = min.getX();
		minGridY = min.getY();
		calcMid();
	}
	
	public void setGridMax(Position max) {
		maxGridX = max.getX();
		maxGridY = max.getY();
		calcMid();
	}
	
	public void setGridMinMax(Position min, Position max) {
		System.out.println("old dim: "+minGridX+", "+minGridY+"   "+maxGridX+", "+maxGridY);
		minGridX = min.getX();
		minGridY = min.getY();
		maxGridX = max.getX();
		maxGridY = max.getY();
		System.out.println("new dim: "+minGridX+", "+minGridY+"   "+maxGridX+", "+maxGridY);
		calcMid();
	}
	
	private void calcMid() {
		midGridX = (maxGridX+minGridX)/2;
		midGridY = (maxGridY+minGridY)/2;
	}
	
	public double midGridDistSq(double gridX, double gridY) {
		double dx = Math.abs(gridX - midGridX);
		double dy = Math.abs(gridY - midGridY);
		return dx*dx + dy*dy;
	}
	
	public double viewGridDist(double gridX, double gridY) {
		double dx = 0;
		double dy = 0;
		if ((gridX+1) < minGridX)
			dx = minGridX - (gridX+1);
		else if (gridX > maxGridX)
			dx = gridX - maxGridX;
		if ((gridY+1) < minGridY)
			dy = minGridY - (gridY+1);
		else if (gridY > maxGridY)
			dy = gridY - maxGridY;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	@Override
	public void init() {
		this.canvas = FractalsMain.mainWindow.canvas;
		int w = canvas.getSize().x;
		int h = canvas.getSize().y;
		DataDescriptor dataDescriptor = new DataDescriptor(-2, -2, 2.*w/h, 2, w, h, w, h, 10000, rendererStateHolder, grid.getChunkSize());
		setDataDescriptor(dataDescriptor);
		grid.init();
		updateRendererPositions();
		initialized = true;
	}

	@Override
	public void render(Graphics g, boolean save) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxIterations(int maxIterations) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setQuality(double quality) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLocation(int mouse_x, int mouse_y, double spacing_factor) {
		//disable position debugging
		AbstractCalculator.resetDebug();
		
		//calculate new position
		int w = canvas.getSize().x;
		int h = canvas.getSize().y;
		double factor = (spacing_factor < 1) ? 2./3*spacing_factor : 2./3;
		double factor_mouse = (spacing_factor < 1) ? 1 : 0;
		Position newScreenStart = new Position(mouse_x*factor_mouse - w*factor, mouse_y*factor_mouse - h*factor);
		Position newGridStart = grid.getGridPosition(newScreenStart);//TODO ADJUST FOR ZOOM COPY ALIGNMENT
		Position newSpaceStart = grid.getSpacePosition(newGridStart);
		
		//calculate shift
		Position currentSpaceStart = grid.getSpacePosition(new Position(minGridX, minGridY));
		Position spaceShift = newSpaceStart.subNew(currentSpaceStart);
		
		//execute scale + shift
		scaleBy(spacing_factor);
		shiftView(spaceShift);
		
		//recalculate bounds
		boundsChanged();
	}

	@Override
	public void setLocation(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public Location getLocation(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startIterationPositionThread() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRedrawAndReset() {
		boolean wasRedraw = redraw;
		redraw = false;
		return wasRedraw;
	}

	public void setRedraw() {
		redraw = true;
	}
	
	PerfInstance renderPerf;
	
	long time_renderStart;
	long time_maxRenderTime = (long)((1/60.)/NumberUtil.NS_TO_S);
	
	@Override
	public void render(PaintEvent e, boolean save) {
		time_renderStart = System.nanoTime();
//		log.log("render");
		renderGrid(e);
//		renderMinimap(e);
	}

	private void renderGrid(PaintEvent e) {
//		PerformanceMonitor
		for (long gridX = (long) minGridX ; gridX < maxGridX ; gridX++) {
			for (long gridY = (long) minGridY ; gridY < maxGridY ; gridY++) {
				renderPerf = createNewAndBegin("renderChunk");
				
				PerfInstance get = createNewSubInstanceAndBegin("getChunk", renderPerf);
				double shiftX = (gridX-minGridX-1)*grid.getChunkSize();
				double shiftY = (gridY-minGridY-1)*grid.getChunkSize();
				Chunk chunk = chunkProvider.getChunk(new Position(gridX, gridY));
				if (chunk == null)
					continue;
				get.end();
				
				ProcessingStep step = chunk.getProcessingStep();
				if (step == null) {
					continue;
				}
				
//				if (!chunk.imageCalculated) {
//					PerfInstance calculate = createNewSubInstanceAndBegin("calculate", renderPerf);
//					chunk.calculatePixels();
//					chunk.refreshImage(e.display);
//					calculate.end();
//				}
				
				if (chunk.isReadyToDraw() && chunk.imageData != null && (chunk.refreshNeeded() || !chunk.imageCalculated)) {
					if (isTimeLeft()) {
						PerfInstance refreshImage = createNewSubInstanceAndBegin("refreshImage", renderPerf);
						chunk.refreshImage(e.display);
						refreshImage.end();
					} else {
						FractalsMain.mainWindow.setRedraw(true);
					}
				}
					
//					Position offset = grid.getScreenOffset(new Position(gridX, gridY));
				
				if (!chunk.isDisposed() && chunk.image != null && step.isDrawable()){
					PerfInstance drawImage = createNewSubInstanceAndBegin("drawImage", renderPerf);
					e.gc.drawImage(chunk.image, (int) shiftX, (int) shiftY);
					drawImage.end();
				}
				renderPerf.end();
				renderPerf.printSecondsToLog(3, true, 0.1);
			}
		}
	}

	private boolean isTimeLeft() {
		return System.nanoTime() - time_renderStart < time_maxRenderTime;
	}

	private void renderMinimap(PaintEvent e) {
		Point size = canvas.getSize();
		if (size.x < 200 || size.y < 200)
			return;
		e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
		e.gc.setAlpha(255);
		e.gc.drawRectangle(size.x-150, size.y-150, 100, 100);
		e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
		e.gc.setAlpha(255);
		e.gc.drawRectangle(size.x-150, size.y-150, 100, 100);
	}

	@Override
	public void resized() {
		if (initialized){
			updateRendererPositions();
			boundsChanged();
		}
	}

	@Override
	public void addColorOffset(float additionalOffset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		taskManager.clearTasks();
		grid.reset();
		chunkProvider.reset();
		boundsChanged();
	}
	
	@Override
	public void shiftView(Position shift) {
		Position gridShift = shift.div(grid.getScale()).div(grid.getChunkSize());
		shiftViewGrid(gridShift);
	}
	
	public void shiftViewGrid(Position gridShift) {
//		System.out.println("GridRenderer.shiftViewGrid(): "+gridShift);
		minGridX += gridShift.getX();
		minGridY += gridShift.getY();
		maxGridX += gridShift.getX();
		maxGridY += gridShift.getY();
		calcMid();
		boundsChanged();
	}
	
	List<Position> tempDeleteChunks = new ArrayList<>();
	
	public void boundsChanged() {
		//update positions
//		updateRendererPositions();
		
		grid.setScreenOffset(getGridMin().add(new Position(1,1)));
		
		//update distances and queue out of bounds removals
		for (Entry<Position, Chunk> e : grid.map.entrySet()) {
			double gridX = e.getKey().getX();
			double gridY = e.getKey().getY();
			double dist = viewGridDist(gridX, gridY);
			if (dist > maxViewDistance) {
				tempDeleteChunks.add(e.getKey());
			} else {
				double midDist = midGridDistSq(gridX, gridY);
				Chunk c = e.getValue();
				c.setDistanceToMid(midDist);
				c.setPriorityMultiplier(dist > 0 ? outsidePriorityMultiplier : insidePriorityMultiplier);
			}
		}
		
		//remove out of bounds chunks
		for (Position removePos : tempDeleteChunks) {
			grid.disposeChunk(removePos);
		}
		tempDeleteChunks.clear();
		
		//update priorities for chunks
		taskManager.setUpdatePriorities();
		
		//redraw the canvas
		FractalsMain.mainWindow.setRedraw(true);
		
		//generate chunks
		if (Double.isInfinite(maxGridX) || Double.isInfinite(maxGridY))
			throw new IllegalStateException();
		for (long gridX = (long) minGridX ; gridX < maxGridX ; gridX++) {
			int loop = 0;
			for (long gridY = (long) minGridY ; gridY < maxGridY ; gridY++) {
				Chunk c = grid.getChunk(gridX, gridY);
//				if (loop++ > 100)
//					System.out.println("GridRenderer.boundsChanged() : loop(y)= "+loop);
//				if (!c.imageCalculated)
//					c.calculatePixels();
			}
		}
	}
	
	private void updateRendererPositions() {
		Point p = canvas.getSize();
		setGridMinMax(new Position(-1,-1), grid.getGridPosition(p.x, p.y));
//		for (int gridX = (int) minGridX ; gridX < maxGridX ; gridX++) {
//			for (int gridY = (int) minGridY ; gridY < maxGridY ; gridY++) {
//				grid.getChunk(gridX, gridY);
//			}
//		}
	}

	private void scaleBy(double scaleBy) {
		getDataDescriptor().scaleBy(scaleBy);
		taskManager.clearTasks();
		grid.scaleBy(scaleBy);
		Position diff = getGridMax().sub(getGridMin());
		setGridMin(getGridMin().mult(1/scaleBy));
		setGridMax(getGridMin().add(diff));
		FractalsMain.mainWindow.setRedraw(true);
//		scaleGridMax(scaleBy);
	}

	public Position getGridMin() {
		return new Position(minGridX, minGridY);
	}

	public Position getGridMax() {
		return new Position(maxGridX, maxGridY);
	}

	public void scaleGridMax(double scaleBy) {
		Position min = getGridMin();
		setGridMax(getGridMax().performOperation(Position.sub, min).scaleBy(scaleBy, false).performOperation(Position.add, min));
	}

	public ArrayListBatchTaskManager getTaskManager() {
		return taskManager;
	}

	public Grid getGrid() {
		return grid;
	}
	
	@Override
	public Painter getPainter() {
		return painter;
	}

	@Override
	public float getColorOffset() {
		return painter.getColorOffset();
	}

	@Override
	public void setColorOffset(float colorOffset) {
		painter.setColorOffset(colorOffset);
		redrawChunks();
	}

	@Override
	public float getColorScale() {
		return painter.getColorScale();
	}

	@Override
	public void setColorScale(float colorScale) {
		painter.setColorScale(colorScale);
		redrawChunks();
	}
	
	protected void redrawChunks() {
		for (Chunk c : grid.map.values())
			c.imageCalculated = false;
		FractalsMain.mainWindow.setRedraw(true);
	}

	@Override
	public void setChunkProvider(ChunkProvider chunkProvider) {
		if (chunkProvider != null)
			chunkProvider.reset();
		this.chunkProvider = chunkProvider;
	}

}
