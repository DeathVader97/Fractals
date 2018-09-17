package de.felixperko.fractals.Controls;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

import de.felixperko.fractals.gui.MainWindow;
import de.felixperko.fractals.util.Position;

public class KeyListenerControls implements KeyListener {
	
	MainWindow mainWindow;
	
	boolean setSave = true;
	boolean jumpToLocation = true;
	boolean saveLocation = true;
	
	public KeyListenerControls(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

//	@Override
//	public void keyTyped(KeyEvent e) {
//		switch (e.getKeyChar()) {
//		case '+':
//			mainWindow.setQuality(mainWindow.getQuality()*2);
//			break;
//		case '-':
//			mainWindow.setQuality(mainWindow.getQuality()*0.5);
//			break;
//		case 'c':
//			mainWindow.loopColor(0.01f);
//		}
//		
////		switch (e.getKeyCode()) {
////		case KeyEvent.VK_ENTER:
////			mainWindow.setRedraw();
////			break;
////		}
//	}
//
//	@Override
//	public void keyPressed(KeyEvent e) {
//		if (setSave && e.getKeyChar() == 's') {
//			mainWindow.save = true;
//			setSave = false;
//		}
//		if (jumpToLocation && e.getKeyCode() == KeyEvent.VK_TAB) {
//			mainWindow.jumpToSavedLocation(e.isShiftDown());
//			jumpToLocation = false;
//		}
//		if (saveLocation && e.getKeyChar() == 'l') {
//			mainWindow.saveLocation();
//			saveLocation = false;
//		}
//	}
//
//	@Override
//	public void keyReleased(KeyEvent e) {
//		if (e.getKeyChar() == 's')
//			setSave = true;
//		if (e.getKeyCode() == KeyEvent.VK_TAB)
//			jumpToLocation = true;
//		if (e.getKeyChar() == 'l')
//			saveLocation = true;
//	}

	double shiftSensitivity = 500;
	public static Position shift = new Position(0,0);
	public static Position nullPos = new Position(0,0);
	
	boolean left,right,up,down;
	
	@Override
	public void keyPressed(KeyEvent e) {
//		System.out.println(e.toString());
		if (e.character == 'l') {
			System.out.println("jump");
			mainWindow.jumpToSavedLocation(false);
		}
		
		else if (e.keyCode == SWT.ARROW_LEFT && !left) {
			shift.addX(-shiftSensitivity);
			left = true;
		} else if (e.keyCode == SWT.ARROW_RIGHT && !right) {
			shift.addX(shiftSensitivity);
			right = true;
		} else if (e.keyCode == SWT.ARROW_UP && !up) {
			shift.addY(-shiftSensitivity);
			up = true;
		} else if (e.keyCode == SWT.ARROW_DOWN && !down) {
			shift.addY(shiftSensitivity);
			down = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.keyCode == SWT.ARROW_LEFT && left) {
			shift.addX(shiftSensitivity);
			left = false;
		} else if (e.keyCode == SWT.ARROW_RIGHT && right) {
			shift.addX(-shiftSensitivity);
			right = false;
		} else if (e.keyCode == SWT.ARROW_UP && up) {
			shift.addY(shiftSensitivity);
			up = false;
		} else if (e.keyCode == SWT.ARROW_DOWN && down) {
			shift.addY(-shiftSensitivity);
			down = false;
		}
	}
}
