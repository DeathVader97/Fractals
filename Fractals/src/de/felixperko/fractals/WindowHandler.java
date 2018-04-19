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
	
	private int iterations = 1000;
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
		
//		jframe.setSize(w, h);
//		jframe.setVisible(true);

		jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jframe.setUndecorated(true);
		jframe.setVisible(true);
		w = jframe.getWidth();
		h = jframe.getHeight();
		
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.addMouseListener(new MouseControls());
		jframe.addMouseWheelListener(new MouseWheelControls(this));
		jframe.addKeyListener(new KeyListenerControls(this));
		panel = new JPanel();
		jframe.add(panel);
//		generateImage();
	}

//	private void generateImage() {
//		if (temp_img == null)
//			temp_img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//		long startTime = System.nanoTime();
//		long printProgressTimer = startTime;
//		long iterationCount = 0;
//		int[] neededIterations = new int[getIterations()/100 + 1];
//		int printCounter = 0;
//		for (int imgx = 0; imgx < w; imgx++) {
//			for (int imgy = 0; imgy < h; imgy++) {
////				img.setRGB(imgx, imgy, new Color((int)(255*imgx/w),(int)(255*imgy/h),0).getRGB());
//				double sat = 0;
//				for (int subx = 0 ; subx < quality ; subx++) {
//					for (int suby = 0 ; suby < quality ; suby++) {
//						double creal = getReal(imgx+subx/quality);
//						double cimag = getImag(imgy+suby/quality);
//						double real = 0;
//						double imag = 0;
////						System.out.println(real+", "+imag);
//						for (int i = 0 ; i < getIterations() ; i++) {
//							double real2 = real*real - (imag*imag) - creal;
//							double imag2 = real*imag + (imag*real) - cimag;
//							real = real2;
//							imag = imag2;
//							iterationCount++;
//							if (real*real + imag*imag > 4) {
//								sat += (((double)i/getIterations()))/(quality*quality);
//								neededIterations[i/100]++;
//								break;
//							}
//							if (i == getIterations()-1)
//								neededIterations[neededIterations.length-1]++;
//						}
//					}
//				}
//				Color color = Color.getHSBColor((float)-Math.pow(sat,0.1), 1, (float)Math.pow(sat, 0.2));
//				temp_img.setRGB(imgx, imgy, color.getRGB());
//			}
//			long percentage = Math.round((double)imgx*100/w);
//			if (System.nanoTime()-printProgressTimer > 1000000000) {
////				System.out.print(percentage+"%...");
//				System.out.println(percentage+";"+iterationCount);
////				if (++printCounter % 20 == 0)
////					System.out.println();
//				printProgressTimer = System.nanoTime();
//			}
////			if (percentage > nextPointPercentage) {
////				nextPointPercentage += 10;
////				System.out.print(".");
////			}
//				
//		}
//		System.out.println();
//		System.out.println(Math.round((System.nanoTime()-startTime)/1000000)/1000.+" s");
//		System.out.println();
//		for (int i = 0 ; i < neededIterations.length ; i++)
//			System.out.println(neededIterations[i]);
//		BufferedImage t = img;
//		this.img = temp_img;
//		temp_img = t;
//	}

	public void render() {
		
		Graphics g = panel.getGraphics();
//		tick();
//		if (img != null) {
//			g.drawImage(img, 0, 0, null);
//		}
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
//		if (changed) {
//			changed = false;
//			generateImage();
//		}
	}

	public void clickedMouse(int x, int y) {
		double real = getReal(x);
		double imag = getImag(y);
		midx = real;
		midy = imag;
//		iterations *= 1.1;
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
}
