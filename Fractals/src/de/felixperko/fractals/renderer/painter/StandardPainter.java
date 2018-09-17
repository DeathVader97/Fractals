package de.felixperko.fractals.renderer.painter;

import java.awt.Color;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.data.Chunk;

public class StandardPainter implements Painter {

	int color = Color.GRAY.getRGB();
	
	float colorOffset = 0;

	@Override
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y) {
		if (chunk.getSampleCount(index) == 0)
			imageData.setPixel(y, x, color);
		else {
			float it = chunk.getAvgIterations(index);
//			sat2 = (float) Math.log10(sat2);
			float hue = (float) Math.log(it);
			
			float b = it > 0 ? (chunk.getDiff() == null ? 1 : (float)Math.pow(chunk.getDiff(index),0.15)*0.9f) : 0;
			
			b *= 1 - chunk.getFailRatio(index);
			if (b > 1)
				b = 1;
			imageData.setPixel(y, x, Color.HSBtoRGB((float) (colorOffset+hue), 0.4f, b));
		}
	}

}
