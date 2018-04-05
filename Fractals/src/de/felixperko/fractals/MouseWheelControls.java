package de.felixperko.fractals;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseWheelControls implements MouseWheelListener {
	
	WindowHandler windowHandler;

	public MouseWheelControls(WindowHandler windowHandler) {
		this.windowHandler = windowHandler;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		windowHandler.setIterations(windowHandler.iterations + e.getWheelRotation()*100);
	}

}
