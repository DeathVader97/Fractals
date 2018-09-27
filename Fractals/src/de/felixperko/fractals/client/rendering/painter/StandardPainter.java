package de.felixperko.fractals.client.rendering.painter;

import java.awt.Color;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.server.data.Chunk;

public class StandardPainter extends AbstractPainterImpl {

	int color = Color.GRAY.getRGB();
	

	@Override
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y) {
		if (chunk.isDisposed())
			return;
		if (chunk.getSampleCount(index) == 0)
			imageData.setPixel(x, y, color);
		else {
			float it = chunk.getAvgIterations(index);
//			sat2 = (float) Math.log10(sat2);
			float hue = (float) Math.log(it);
			
			float b = it > 0 ? (chunk.getDiff() == null ? 1 : (float)Math.pow(chunk.getDiff(index),0.15)*0.9f) : 0;
			
			b *= 1 - chunk.getFailRatio(index);
			if (it < 1)
				b *= it;
			if (b > 1)
				b = 1;
			imageData.setPixel(x, y, Color.HSBtoRGB((float) (colorOffset+hue*colorScale), 0.4f, b));
		}
	}

}
