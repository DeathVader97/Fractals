package de.felixperko.fractals.server.data;

import java.util.HashMap;

import de.felixperko.fractals.client.rendering.renderer.GridRenderer;
import de.felixperko.fractals.server.tasks.TaskManager;
import de.felixperko.fractals.server.util.Position;

public class Grid {
	
	int chunk_size = 128;
	
	TaskManager taskManager;
	
	HashMap<Long, HashMap<Long, Position>> positions = new HashMap<>();
	
	public HashMap<Position, Chunk> map = new HashMap<>();
	
	Position screenOffset;
	double screenShiftX;
	double screenShiftY;
	
	Position spaceOffset;
	double spaceShiftX;
	double spaceShiftY;
	
	public Grid() {
		
		this.screenOffset = new Position(0,0);
		this.screenShiftX = chunk_size;
		this.screenShiftY = chunk_size;
	}
	
	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
		updateSpaceDimensions();
	}

	public Chunk getChunk(long gridX, long gridY) {
		Position pos = getPosition(gridX, gridY);
		Chunk c = map.get(pos);
		if (c == null) {
			c = new Chunk(chunk_size, taskManager.getDataDescriptor(), this, pos);
			map.put(pos, c);
			taskManager.addChunk(c);
		}
		return c;
	}

	public Chunk getChunk(Position gridPos) {
		return getChunk((long)gridPos.getX(), (long)gridPos.getY());
	}
	
	private Chunk getChunkOrNull(long gridX, long gridY) {
		HashMap<Long, Position> xmap = positions.get(gridX);
		if (xmap == null)
			return null;
		Position pos = xmap.get(gridY);
		if (pos == null)
			return null;
		return map.get(pos);
	}

	public Chunk getChunkOrNull(Position gridPos) {
		return getChunkOrNull((long)gridPos.getX(), (long)gridPos.getY());
	}

//	public Position getGridPosOrNull(int gridX, int gridY) {
//		HashMap<Integer, Position> map = positions.get(gridX);
//		if (map == null)
//			return null;
//		return map.get(gridY);
//	}

	public Position getScreenOffset(Position gridPos) {
		return new Position(screenOffset.getX() + gridPos.getX()*screenShiftX,
							screenOffset.getY() + gridPos.getY()*screenShiftY);
	}
	
	public Position getSpaceOffset(Position gridPos) {
		return new Position(spaceOffset.getX() + gridPos.getX()*spaceShiftX,
							spaceOffset.getY() + gridPos.getY()*spaceShiftY);
	}
	
	public Position getScreenOffset() {
		return screenOffset;
	}
	
	public void setScreenOffset(Position offset) {
		this.screenOffset = offset;
//		Position newGridStart = getGridPosition((int)offset.getX(), (int)offset.getY());
//		Position newSpaceStart = getSpacePosition(newGridStart);
//		taskManager.getDataDescriptor().setStart_x(newSpaceStart.getX());
//		taskManager.getDataDescriptor().setStart_y(newSpaceStart.getY());
//		updateSpaceDimensions();
	}

	public void shiftScreenOffset(Position shift) {
		this.screenOffset.performOperation(Position.add, shift);
		this.spaceOffset = this.screenOffset;
	}
	
	public void updateSpaceDimensions() {
		double scaleX = getScaleX();
		double scaleY = getScaleY();
		this.spaceOffset = new Position(taskManager.getDataDescriptor().getStart_x(), taskManager.getDataDescriptor().getStart_y());
//		this.spaceOffset = new Position(screenOffset.getX()*scaleX, screenOffset.getY()*scaleY);
		this.spaceShiftX = screenShiftX*scaleX;
		this.spaceShiftY = screenShiftY*scaleY;
		System.out.println("Grid.updateSpaceDimensions() : spaceShift: "+spaceShiftX+","+spaceShiftY);
	}
	
	public double getScaleX(){
		return taskManager.getDataDescriptor().getDelta_x()/taskManager.getDataDescriptor().getDim_goal_x();
	}
	
	public double getScaleY(){
		return taskManager.getDataDescriptor().getDelta_y()/taskManager.getDataDescriptor().getDim_goal_y();
	}

	public Position getPosition(long gridX, long gridY) {
		Position pos = null;
		HashMap<Long, Position> yMap = positions.get(gridX);
		if (yMap == null) {
			yMap = new HashMap<Long, Position>();
			positions.put(gridX, yMap);
		} else {
			pos = yMap.get(gridY);
		}
		if (pos == null) {
			pos = new Position(gridX, gridY);
			yMap.put(gridY, pos);
		}
		return pos;
	}

	public void disposeChunk(Position key) {
		Chunk c = map.remove(key);
		c.dispose();
		taskManager.removeChunkTask(c);
		positions.get((long)key.getX()).remove((long)key.getY());
	}

	public int getChunkSize() {
		return chunk_size;
	}
	
	public void scaleBy(double scaleBy){
		updateSpaceDimensions();
		reset();
	}

	public void reset() {
		positions.clear();
		for (Chunk c : map.values())
			c.dispose();
		map.clear();
		
//		spaceShiftX *= 0.5;
//		spaceShiftY *= 0.5;
	}

	public Position getSpacePosition(Position gridPosition) {
		return new Position(gridPosition.getX()*chunk_size*getScaleX() + spaceOffset.getX(),
							gridPosition.getY()*chunk_size*getScaleY() + spaceOffset.getY());
	}

	public Position spaceToGrid(Position spacePosition) {
		return new Position((spacePosition.getX()+spaceOffset.getX())/getScaleX()/chunk_size,
							(spacePosition.getY()+spaceOffset.getY())/getScaleY()/chunk_size);
//		return new Position((spacePosition.getX())/getScaleX()/chunk_size,
//							(spacePosition.getY())/getScaleY()/chunk_size);
	}

	public void setSpaceOffset(Position newSpaceOffset) {
		this.spaceOffset = newSpaceOffset;
		updateSpaceDimensions();
	}
	
	public Position getGridPosition(int screenX, int screenY) {
		return new Position((screenX)/screenShiftX+screenOffset.getX(),
							(screenY)/screenShiftY+screenOffset.getY());
	}

	public Position getGridPosition(Position screenPos) {
		return new Position((screenPos.getX())/screenShiftX+screenOffset.getX(),
							(screenPos.getY())/screenShiftY+screenOffset.getY());
	}

	public Position getScale() {
		return new Position(getScaleX(), getScaleY());
	}

	public int getIndexFromGridPos(Position cursorGridPos) {
		double x = cursorGridPos.getX();
		double y = cursorGridPos.getY();
		
		if (x < 0)
			x = 1- (Math.abs(x)%1);
		else
			x %= 1;
		if (y < 0)
			y = 1- (Math.abs(y)%1);
		else
			y %= 1;
		
		int xindex = (int) (x*chunk_size);
		int yi = (int) (y);
		return xindex*chunk_size + yi;
	}

	public void setChunkSize(int chunkSize) {
		this.chunk_size = chunkSize;
	}
}
