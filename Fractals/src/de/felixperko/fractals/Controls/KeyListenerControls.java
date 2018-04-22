package de.felixperko.fractals.Controls;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

import de.felixperko.fractals.WindowHandler;
import de.felixperko.fractals.gui.MainWindow;

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

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println(e.toString());
		if (e.character == 'l') {
			System.out.println("jump");
			mainWindow.jumpToSavedLocation(false);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
