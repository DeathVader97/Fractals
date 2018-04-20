package de.felixperko.fractals;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.felixperko.fractals.Controls.KeyListenerControls;
import de.felixperko.fractals.Controls.MouseControls;
import de.felixperko.fractals.Controls.MouseWheelControls;
import de.felixperko.fractals.Tasks.WorkerThread;

public class WindowHandler {
	
	static int w = 1280, h = 720;
	static int target_fps = 60;
	
	private int iterations = 1000000;
	double midx = 0, midy = 0;
	double range = 3;
	
	int prev_iterations = getIterations();
	
	boolean changed = true;
	
	JFrame jframe;
	JPanel panel;
	BufferedImage img;
	BufferedImage temp_img;

	public double quality = 1;
	
	public FractalRenderer mainRenderer;
	
	public boolean save = false;
	
	public WindowHandler() {
		
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		w = screenSize.width;
//		h = screenSize.height;
		
		jframe = new JFrame("Fractals");
		
		jframe.setSize(w, h);
		jframe.setVisible(true);

//		jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		jframe.setUndecorated(true);
//		jframe.setVisible(true);
//		w = jframe.getWidth();
//		h = jframe.getHeight();
		
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.addMouseListener(new MouseControls());
		jframe.addMouseWheelListener(new MouseWheelControls(this));
		jframe.addKeyListener(new KeyListenerControls(this));
		jframe.setFocusTraversalKeysEnabled(false);
		panel = new JPanel();
		jframe.add(panel);
	}

	public void render() {
		
		Graphics g = panel.getGraphics();
		
		if (mainRenderer != null) {
			updateScheduledProperties();
			mainRenderer.render(g, save);
			save = false;
		}
		
		int y = 15;
		for (WorkerThread thread : FractalsMain.threadManager.getThreads()) {
			g.drawString(thread.getPhase(), 10, y);
			y += 15;
		}
		
		g.dispose();
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void updateScheduledProperties() {
		if (iterations != mainRenderer.getMaxIterations() && System.nanoTime()-lastMaxIterationsChange > 100000000) {
			System.out.println("set interations: "+iterations);
			mainRenderer.setMaxIterations(iterations);
		}
	}

	public void clickedMouse(int x, int y) {
		double real = getReal(x);
		double imag = getImag(y);
		midx = real;
		midy = imag;
		range *= 0.5;
		changed = true;
		mainRenderer.updateLocation(x, y, 0.5);
	}
	
	public double getReal(double imgx) {
		return (((double)(h-imgx)/h)*range)+midx;
	}
	
	public double getImag(double imgy) {
		return (((double)(h-imgy)/h - 0.5)*range)+midy;
	}

	public void setQuality(double quality) {
		System.out.println("set quality: "+quality);
		this.quality = quality;
		mainRenderer.setQuality(quality);
	}

	public int getIterations() {
		return iterations;
	}
	
	long lastMaxIterationsChange = 0;
	
	public void setIterations(int iterations) {
		this.iterations = iterations;
		lastMaxIterationsChange = System.nanoTime();
	}
	
	private void applyRendererIterations() {
		mainRenderer.setMaxIterations(iterations);
	}

	public void setRedraw() {
		System.out.println("set redraw...");
		changed = true;
	}

	public FractalRenderer getMainRenderer() {
		return mainRenderer;
	}

	public void setMainRenderer(FractalRenderer mainRenderer) {
		this.mainRenderer = mainRenderer;
	}

	public void loopColor(float additionalOffset) {
		mainRenderer.addColorOffset(additionalOffset);
	}

	public void jumpToSavedLocation(boolean backwards) {
		mainRenderer.setLocation(backwards ? FractalsMain.locationHolder.getPreviousLocation() : FractalsMain.locationHolder.getNextLocation());
	}

	public void saveLocation() {
		FractalsMain.locationHolder.addLocation(mainRenderer.getLocation());
	}
}
