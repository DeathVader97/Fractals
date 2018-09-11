package de.felixperko.fractals.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.felixperko.fractals.renderer.GridRenderer;
import de.felixperko.fractals.renderer.painter.Painter;
import de.felixperko.fractals.renderer.painter.StandardPainter;
import de.felixperko.fractals.util.Position;

public class Grid {
	
	int chunk_size = 128;
	
	GridRenderer renderer;
	
	HashMap<Integer, HashMap<Integer, Position>> positions = new HashMap<>();
	
	public HashMap<Position, Chunk> map = new HashMap<>();
	
	Position screenOffset;
	double screenShiftX;
	double screenShiftY;
	
	Position spaceOffset;
	double spaceShiftX;
	double spaceShiftY;
	
	public Grid(GridRenderer renderer) {
		this.renderer = renderer;
		
		this.screenOffset = new Position(0,0);
		this.screenShiftX = chunk_size;
		this.screenShiftY = chunk_size;
	}
	
	public void init(){
		updateSpaceDimensions();
	}

	public Chunk getChunk(int gridX, int gridY) {
		Position pos = getPosition(gridX, gridY);
		Chunk c = map.get(pos);
		if (c == null) {
			c = new Chunk(chunk_size, renderer.getDataDescriptor(), this, pos);
			map.put(pos, c);
			renderer.getTaskManager().addChunk(c);
		}
		return c;
	}
	
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
//		renderer.getDataDescriptor().setStart_x(newSpaceStart.getX());
//		renderer.getDataDescriptor().setStart_y(newSpaceStart.getY());
//		updateSpaceDimensions();
	}

	public void shiftScreenOffset(Position shift) {
		this.screenOffset.performOperation(Position.add, shift);
	}
	
	public void updateSpaceDimensions() {
		double scaleX = getScaleX();
		double scaleY = getScaleY();
		this.spaceOffset = new Position(renderer.getDataDescriptor().getStart_x(), renderer.getDataDescriptor().getStart_y());
//		this.spaceOffset = new Position(screenOffset.getX()*scaleX, screenOffset.getY()*scaleY);
		this.spaceShiftX = screenShiftX*scaleX;
		this.spaceShiftY = screenShiftY*scaleY;
	}
	
	public double getScaleX(){
		return renderer.getDataDescriptor().getDelta_x()/renderer.getDataDescriptor().getDim_goal_x();
	}
	
	public double getScaleY(){
		return renderer.getDataDescriptor().getDelta_x()/renderer.getDataDescriptor().getDim_goal_x();
	}

	private Position getPosition(int gridX, int gridY) {
		Position pos = null;
		HashMap<Integer, Position> yMap = positions.get(gridX);
		if (yMap == null) {
			yMap = new HashMap<Integer, Position>();
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
		renderer.getTaskManager().priorityList.remove(c);
		positions.get((int)key.getX()).remove((int)key.getY());
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

	public GridRenderer getRenderer() {
		return renderer;
	}

	public Position getSpacePosition(Position gridPosition) {
		return new Position(gridPosition.getX()*chunk_size*getScaleX() + spaceOffset.getX(),
							gridPosition.getY()*chunk_size*getScaleY() + spaceOffset.getY());
	}

	public Position spaceToGrid(Position spacePosition) {
//		return new Position((spacePosition.getX()+spaceOffset.getX())/getScaleX()/chunk_size,
//							(spacePosition.getY()+spaceOffset.getY())/getScaleY()/chunk_size);
		return new Position((spacePosition.getX())/getScaleX()/chunk_size,
							(spacePosition.getY())/getScaleY()/chunk_size);
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
	
	
}
