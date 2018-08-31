package de.felixperko.fractals.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.felixperko.fractals.renderer.GridRenderer;
import de.felixperko.fractals.util.Position;

public class Grid {
	
	int chunk_size = 16;
	
	GridRenderer renderer;
	
	HashMap<Integer, HashMap<Integer, Position>> positions = new HashMap<>();
	
	public HashMap<Position, Chunk> map = new HashMap<>();
	
	Position offset;
	double shiftX;
	double shiftY;
	
	public Grid(GridRenderer renderer) {
		this.renderer = renderer;
		this.offset = new Position(0,0);
		this.shiftX = 16;
		this.shiftY = 16;
	}
	
	public Chunk getChunk(int gridX, int gridY) {
		Position pos = getPosition(gridX, gridY);
		Chunk c = map.get(pos);
		if (c == null) {
			c = new Chunk(chunk_size, renderer.getDataDescriptor(), this);
			map.put(pos, c);
		}
		return c;
	}
	
	public Position getOffset(Position gridPos) {
		return new Position(offset.getX() + gridPos.getX()*shiftX, offset.getY() + gridPos.getY()*shiftY);
	}
	
	public Position getOffset() {
		return offset;
	}
	
	public void setOffset(Position offset) {
		this.offset = offset;
	}

	public void shiftOffset(Position shift) {
		this.offset.performOperation(Position.add, shift);
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
	
	public Position getGridPosition(int screenX, int screenY) {
		return new Position((screenX-offset.getX())/shiftX, (screenY-offset.getY())/shiftY);
	}

	public void disposeChunk(Position key) {
		Chunk c = map.remove(key);
		c.dispose();
		priorityList.remove(c);
		positions.get((int)key.getX()).remove((int)key.getY());
	}
}
