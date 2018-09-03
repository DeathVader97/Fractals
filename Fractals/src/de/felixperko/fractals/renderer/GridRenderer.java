package de.felixperko.fractals.renderer;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map.Entry;

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
import de.felixperko.fractals.util.Position;

public class GridRenderer extends AbstractRendererImpl {
	
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
		minGridX = min.getX();
		minGridY = min.getY();
		maxGridX = max.getX();
		maxGridY = max.getY();
		calcMid();
	}
	
	private void calcMid() {
		midGridX = (maxGridX-minGridX)/2;
		midGridY = (maxGridY-minGridY)/2;
	}
	
	public double midDistSq(double gridX, double gridY) {
		double dx = Math.abs(gridX - midGridX);
		double dy = Math.abs(gridY - midGridY);
		return dx*dx + dy*dy;
	}
	
	public double viewDist(double gridX, double gridY) {
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
		Position newScreenStart = new Position(mouse_x*0.5, mouse_y*0.5);
		Position newGridStart = grid.getGridPosition((int)newScreenStart.getX(), (int)newScreenStart.getY());//TODO ADJUST FOR ZOOM COPY ALIGNMENT
		Position newSpaceStart = grid.getSpacePosition(newGridStart);
		grid.scaleBy(spacing_factor);
		grid.setScreenOffset(newSpaceStart);
		//TODO EVERYTHING WRONG HERE! DRAMA!
		setGridMinMax(new Position(-2,-2), new Position((maxGridX-minGridX)*0.5, (maxGridY-minGridY)*0.5));
		grid.reset();
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

	@Override
	public void render(PaintEvent e, boolean save) {
//		PerfInstance renderPerf = PerfInstance.createNewAndBegin("render");
		for (int gridX = (int) minGridX ; gridX < maxGridX ; gridX++) {
			for (int gridY = (int) minGridY ; gridY < maxGridY ; gridY++) {
				Chunk chunk = grid.getChunk(gridX, gridY);
				if (!chunk.imageCalculated) {
					chunk.calculatePixels();
				}
				chunk.refreshImage(e.display);
				Position offset = grid.getScreenOffset(new Position(gridX, gridY));
				e.gc.drawImage(chunk.image, (int) offset.getX(), (int) offset.getY());
			}
		}
//		renderPerf.end();
//		renderPerf.printChildSecondsToLog(3);
	}

	@Override
	public void resized() {
		if (initialized)
			updateRendererPositions();
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
	public void shift(Position shift) {
		//set grid offset
		grid.setScreenOffset(grid.getScreenOffset().performOperation(Position.addNew, shift));
		boundsChanged();
	}
	
	List<Position> tempDeleteChunks = new ArrayList<>();
	
	private void boundsChanged() {
		//update positions
		updateRendererPositions();
		
		//update distances and queue out of bounds removals
		for (Entry<Position, Chunk> e : grid.map.entrySet()) {
			double gridX = e.getKey().getX();
			double gridY = e.getKey().getY();
			double dist = viewDist(gridX, gridY);
			if (dist > maxViewDistance) {
				tempDeleteChunks.add(e.getKey());
			} else {
				double midDist = midDistSq(gridX, gridY);
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
		canvas.redraw();
	}
	
	private void updateRendererPositions() {
		Point p = canvas.getSize();
		setGridMinMax(new Position(-2,-2), grid.getGridPosition(p.x, p.y));
//		for (int gridX = (int) minGridX ; gridX < maxGridX ; gridX++) {
//			for (int gridY = (int) minGridY ; gridY < maxGridY ; gridY++) {
//				grid.getChunk(gridX, gridY);
//			}
//		}
	}

	public void scaleBy(double scaleBy) {
		getDataDescriptor().scaleBy(scaleBy);
		scaleGridMax(scaleBy);
	}

	public Position getGridMin() {
		return new Position(minGridX, minGridX);
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

}
