package de.felixperko.fractals.client.rendering.painter;

import java.awt.Color;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.server.data.Chunk;

public class SamplesPainter extends AbstractPainterImpl {
	
	float sampleOffset = 1f/100;
	
	@Override
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y) {
		
		float hue = chunk.getSampleCount(index)*sampleOffset;
		
		float b = chunk.getAvgIterations(index) > 0 ? (chunk.getDiff() == null ? 1 : (float)Math.pow(chunk.getDiff(index),0.15)*0.9f) : 0.5f;
		
		b *= 1 - chunk.getFailRatio(index)*0.5f;
		if (b > 1)
			b = 1;
		imageData.setPixel(y, x, Color.HSBtoRGB((float) (hue), 0.4f, b));
	}
}