package de.felixperko.fractals.renderer.painter;

import java.awt.Color;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.data.Chunk;

public class FailRatioPainter implements Painter {
	
	float sampleOffset = 1f/100;

	@Override
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y) {
		
		float failRatio = chunk.getFailRatio(index);
		float hue = failRatio;
		
		float b = 1;
		
		b *= 1 - failRatio;
		imageData.setPixel(y, x, Color.HSBtoRGB((float) (hue), 0.4f, b));
	}

}
