package de.felixperko.fractals.Controls;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import de.felixperko.fractals.WindowHandler;

public class MouseWheelControls implements MouseWheelListener {
	
	WindowHandler windowHandler;

	public MouseWheelControls(WindowHandler windowHandler) {
		this.windowHandler = windowHandler;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int newMaxIterations = windowHandler.getIterations() - e.getWheelRotation()*100;
		if (newMaxIterations > 0)
			windowHandler.setIterations(newMaxIterations);
	}

}
