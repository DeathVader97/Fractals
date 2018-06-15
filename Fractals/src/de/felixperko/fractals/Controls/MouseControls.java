package de.felixperko.fractals.Controls;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import de.felixperko.fractals.FractalsMain;

public class MouseControls implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent arg0) {
		Point p = arg0.getPoint();
		FractalsMain.getWindowHandler().clickedMouse(p.x, p.y);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}
