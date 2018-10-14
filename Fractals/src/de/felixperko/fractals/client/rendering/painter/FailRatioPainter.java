package de.felixperko.fractals.client.rendering.painter;

import java.awt.Color;

import org.eclipse.swt.graphics.ImageData;

import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.ChunkAccessType;

public class FailRatioPainter extends AbstractPainterImpl {
	
	float sampleOffset = 1f/100;

	@Override
	public void paint(ImageData imageData, Chunk chunk, int index, int x, int y) {
		
		float failRatio = chunk.getFailRatio(index, ChunkAccessType.RENDERING);
		float hue = failRatio;
		
		float b = 1;
		
		b *= 1 - failRatio* 0.5f;
		imageData.setPixel(y, x, Color.HSBtoRGB((float) (hue), 0.4f, b));
	}

}
