package de.felixperko.fractals;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyListenerControls implements KeyListener {
	
	WindowHandler windowHandler;
	
	public KeyListenerControls(WindowHandler windowHandler) {
		this.windowHandler = windowHandler;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
		case '+':
			windowHandler.setQuality(windowHandler.quality + 1);
			break;
		case '-':
			if (windowHandler.quality > 1)
				windowHandler.setQuality(windowHandler.quality - 1);
			break;
		}
		
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ENTER:
			windowHandler.setRedraw();
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
