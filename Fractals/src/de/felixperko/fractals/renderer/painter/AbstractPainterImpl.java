package de.felixperko.fractals.renderer.painter;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.data.Chunk;

public abstract class AbstractPainterImpl implements Painter {

	protected float colorOffset = 0;
	protected float colorScale = 1;

	@Override
	public void setColorOffset(float colorOffset) {
		this.colorOffset = colorOffset;
	}

	@Override
	public void setColorScale(float colorScale) {
		this.colorScale = colorScale;
	}

	@Override
	public float getColorScale() {
		return colorScale;
	}

	@Override
	public float getColorOffset() {
		return colorOffset;
	}

}
