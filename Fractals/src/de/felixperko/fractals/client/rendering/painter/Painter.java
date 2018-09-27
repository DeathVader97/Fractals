package de.felixperko.fractals.client.rendering.painter;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.server.data.Chunk;

public interface Painter {
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y);

	public float getColorScale();
	public void setColorScale(float colorScale);

	public float getColorOffset();
	public void setColorOffset(float colorOffset);
}
