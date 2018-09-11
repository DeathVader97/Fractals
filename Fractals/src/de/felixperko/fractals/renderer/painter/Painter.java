package de.felixperko.fractals.renderer.painter;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.data.Chunk;

public interface Painter {
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y);
}
