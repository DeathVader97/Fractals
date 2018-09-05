package de.felixperko.fractals.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.Tasks.NewTaskManagerImpl;
import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.perf.PerfInstance;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.Grid;
import de.felixperko.fractals.data.Location;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Position;
import static de.felixperko.fractals.Tasks.perf.PerfInstance.*;

public class GridRenderer extends AbstractRendererImpl {
	
	CategoryLogger log = new CategoryLogger("renderer", Color.BLUE);
	
	Canvas canvas;
	
	NewTaskManagerImpl taskManager;
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
	
	boolean redraw = true;
	
	public GridRenderer() {
		grid = new Grid(this);
	}
	
	public void setTaskManager(TaskManager taskManager){
		if (!(taskManager instanceof NewTaskManagerImpl))
			throw new IllegalArgumentException();
		this.taskManager = (NewTaskManagerImpl) taskManager;
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
		DataDescriptor dataDescriptor = new DataDescriptor(-2, -2, 2.*w/h, 2, w, h, w, h, 1000, rendererStateHolder);
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
		//TODO ZOOM POSITION!
//		Position clickPoint = grid.getSpaceOffset(grid.getGridPosition(mouse_x, mouse_y));
		int w = canvas.getSize().x;
		int h = canvas.getSize().y;
		Position newScreenStart = new Position(mouse_x - w*0.5*spacing_factor, mouse_y - h*0.5*spacing_factor);
//		Position newScreenStart = new Position(w/4, h/4);
//		Position newScreenEnd = new Position(mouse_x + w/4, mouse_y + h/4);
//		Position newGridStart = grid.getGridPosition(newScreenStart).performOperation(Position.add, getGridMin());//TODO ADJUST FOR ZOOM COPY ALIGNMENT
		Position newGridStart = grid.getGridPosition(newScreenStart);//TODO ADJUST FOR ZOOM COPY ALIGNMENT
		Position newSpaceStart = grid.getSpacePosition(newGridStart);
		Position currentSpaceStart = grid.getSpacePosition(new Position(minGridX, minGridY));
		Position spaceShift = newSpaceStart.performOperation(Position.subNew, currentSpaceStart);
		scaleBy(spacing_factor);
		shiftView(spaceShift);
		Position updatedGridStart = grid.spaceToGrid(newSpaceStart);
		Position updatedScreenStart = grid.getScreenOffset(updatedGridStart);
//		grid.setSpaceOffset(newSpaceStart);
//		Position newGridEnd = grid.getGridPosition((int)newScreenEnd.getX(), (int)newScreenEnd.getY());
		//TODO EVERYTHING WRONG HERE! DRAMA!
//		setGridMinMax(new Position(-2,-2), new Position((maxGridX-minGridX)*0.5, (maxGridY-minGridY)*0.5));
//		setGridMinMax(newGridStart, newGridEnd);
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

	@Override
	public void render(PaintEvent e, boolean save) {
//		log.log("render");
		renderGrid(e);
//		renderMinimap(e);
	}

	private void renderGrid(PaintEvent e) {
		for (int gridX = (int) minGridX ; gridX < maxGridX ; gridX++) {
			for (int gridY = (int) minGridY ; gridY < maxGridY ; gridY++) {
				renderPerf = createNewAndBegin("renderChunk");
				
				PerfInstance get = createNewSubInstanceAndBegin("getChunk", renderPerf);
				double shiftX = (gridX-minGridX-1)*grid.getChunkSize();
				double shiftY = (gridY-minGridY-1)*grid.getChunkSize();
				Chunk chunk = grid.getChunk(gridX, gridY);
				get.end();
				
				if (!chunk.imageCalculated) {
					PerfInstance calculate = createNewSubInstanceAndBegin("calculate", renderPerf);
					chunk.calculatePixels();
					calculate.end();
				}
				
				PerfInstance refreshImage = createNewSubInstanceAndBegin("refreshImage", renderPerf);
				chunk.refreshImage(e.display);
				refreshImage.end();
				
//				Position offset = grid.getScreenOffset(new Position(gridX, gridY));
				
				PerfInstance drawImage = createNewSubInstanceAndBegin("drawImage", renderPerf);
				e.gc.drawImage(chunk.image, (int) shiftX, (int) shiftY);
				drawImage.end();
				renderPerf.end();
				renderPerf.printSecondsToLog(3, true, 0.01);
			}
		}
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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void shiftView(Position shift) {
		//set grid offset
		double chunkSize = grid.getChunkSize();
//		Position gridShift = new Position(shift.getX()/grid.getScaleX()/chunkSize, shift.getY()/grid.getScaleY()/chunkSize);
		Position gridShift = grid.spaceToGrid(shift);
//		System.out.println("shift by "+shift+" grid: "+gridShift);
		minGridX += gridShift.getX();
		minGridY += gridShift.getY();
		maxGridX += gridShift.getX();
		maxGridY += gridShift.getY();
		calcMid();
//		Position newMin = getGridMin().performOperation(Position.add, gridShift);
//		Position newMax = getGridMax().performOperation(Position.add, gridShift);
//		setGridMinMax(newMin, newMax);
//		grid.setScreenOffset(grid.getScreenOffset().performOperation(Position.addNew, shift));
		boundsChanged();
	}
	
	public void shiftViewGrid(Position gridShift) {
		minGridX += gridShift.getX();
		minGridY += gridShift.getY();
		maxGridX += gridShift.getX();
		maxGridY += gridShift.getY();
		calcMid();
		boundsChanged();
	}
	
	List<Position> tempDeleteChunks = new ArrayList<>();
	
	private void boundsChanged() {
		//update positions
//		updateRendererPositions();
		
		grid.setScreenOffset(getGridMin());
		
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
		for (int gridX = (int) minGridX ; gridX < maxGridX ; gridX++) {
			for (int gridY = (int) minGridY ; gridY < maxGridY ; gridY++) {
				Chunk c = grid.getChunk(gridX, gridY);
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

	public NewTaskManagerImpl getTaskManager() {
		return taskManager;
	}

	public Grid getGrid() {
		return grid;
	}

}
