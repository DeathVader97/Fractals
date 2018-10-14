package de.felixperko.fractals.client.rendering.chunkprovider;

import java.util.HashMap;
import java.util.Map;

import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.util.Position;

public class LocalChunkProvider implements ChunkProvider{
	
	Map<Position, Chunk> chunks = new HashMap<>();
	
	@Override
	public Chunk getChunk(Position gridPos) {
		return chunks.get(gridPos);
	}

	@Override
	public void reset() {
		chunks.clear();
	}

	@Override
	public void addChunk(Chunk chunk) {
		chunks.put(chunk.getGridPosition(), chunk);
	}

}
