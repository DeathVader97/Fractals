package de.felixperko.fractals.client.rendering.renderer;

import java.awt.Graphics;

import org.eclipse.swt.events.PaintEvent;

import de.felixperko.fractals.client.rendering.chunkprovider.ChunkProvider;
import de.felixperko.fractals.client.rendering.painter.Painter;
import de.felixperko.fractals.client.stateholders.RendererStateHolder;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.data.Location;
import de.felixperko.fractals.server.state.StateHolder;
import de.felixperko.fractals.server.stateholders.JobStateHolder;
import de.felixperko.fractals.server.util.Position;

public interface Renderer {

	public void init();

	public void render(Graphics g, boolean save);

	public DataDescriptor getDataDescriptor();
	public void setDataDescriptor(DataDescriptor dataDescriptor);

	public void setMaxIterations(int maxIterations);

	public void setQuality(double quality);

	public void updateLocation(int mouse_x, int mouse_y, double spacing_factor);
	
	public void setLocation(Location location);
	public Location getLocation(String name);
	
	public StateHolder getRendererStateHolder();
	public void setRendererStateHolder(RendererStateHolder rendererStateHolder);

	public void startIterationPositionThread();

	public boolean isRedrawAndReset();

	public void render(PaintEvent e, boolean save);

	public void resized();

	public void addColorOffset(float additionalOffset);

	public void reset();

	public void shiftView(Position shift);
	
	public Painter getPainter();
	public void setPainter(Painter painter);
	
	public float getColorOffset();
	public void setColorOffset(float colorOffset);
	
	public float getColorScale();
	public void setColorScale(float scale);
	
	public void setChunkProvider(ChunkProvider chunkProvider);
}