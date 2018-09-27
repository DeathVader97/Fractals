package de.felixperko.fractals.client.rendering.painter;

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
