package de.felixperko.fractals.renderer;

import java.awt.Graphics;

import org.eclipse.swt.events.PaintEvent;

import de.felixperko.fractals.data.DataContainer;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.data.Location;
import de.felixperko.fractals.state.StateHolder;
import de.felixperko.fractals.state.stateholders.RendererStateHolder;
import de.felixperko.fractals.util.Position;

public interface Renderer {

	public void init();

	public void render(Graphics g, boolean save);

	public DataDescriptor getDataDescriptor();
	public void setDataDescriptor(DataDescriptor dataDescriptor);

//	public DataContainer getDataContainer();
//	public void setDataContainer(DataContainer dataContainer);

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

}