package de.felixperko.fractals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import de.felixperko.fractals.Controls.KeyListenerControls;
import de.felixperko.fractals.Controls.MouseControls;
import de.felixperko.fractals.Controls.MouseWheelControls;

public class WindowHandler {
	
	static int w = 1280, h = 720;
	
	private int iterations = 100;
	double midx = 0, midy = 0;
	double range = 3;
	
	int prev_iterations = getIterations();
	
	boolean changed = true;
	
	JFrame jframe;
	BufferedImage img;
	BufferedImage temp_img;

	public int quality = 1;
	
	public WindowHandler() {
		jframe = new JFrame("Fractals");
		jframe.setVisible(true);
		jframe.setSize(w,h);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.addMouseListener(new MouseControls());
		jframe.addMouseWheelListener(new MouseWheelControls(this));
		jframe.addKeyListener(new KeyListenerControls(this));
		generateImage();
	}

	private void generateImage() {
		if (temp_img == null)
			temp_img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		long startTime = System.nanoTime();
		long printProgressTimer = startTime;
		long iterationCount = 0;
		int[] neededIterations = new int[getIterations()/100 + 1];
		int printCounter = 0;
		for (int imgx = 0; imgx < w; imgx++) {
			for (int imgy = 0; imgy < h; imgy++) {
//				img.setRGB(imgx, imgy, new Color((int)(255*imgx/w),(int)(255*imgy/h),0).getRGB());
				double sat = 0;
				for (int subx = 0 ; subx < quality ; subx++) {
					for (int suby = 0 ; suby < quality ; suby++) {
						double creal = getReal(imgx+subx/quality);
						double cimag = getImag(imgy+suby/quality);
						double real = 0;
						double imag = 0;
//						System.out.println(real+", "+imag);
						for (int i = 0 ; i < getIterations() ; i++) {
							double real2 = real*real - (imag*imag) - creal;
							double imag2 = real*imag + (imag*real) - cimag;
							real = real2;
							imag = imag2;
							iterationCount++;
							if (real*real + imag*imag > 4) {
								sat += (((double)i/getIterations()))/(quality*quality);
								neededIterations[i/100]++;
								break;
							}
							if (i == getIterations()-1)
								neededIterations[neededIterations.length-1]++;
						}
					}
				}
				Color color = Color.getHSBColor((float)-sat, 1, (float)Math.pow(sat, 0.1));
				temp_img.setRGB(imgx, imgy, color.getRGB());
			}
			long percentage = Math.round((double)imgx*100/w);
			if (System.nanoTime()-printProgressTimer > 1000000000) {
//				System.out.print(percentage+"%...");
				System.out.println(percentage+";"+iterationCount);
//				if (++printCounter % 20 == 0)
//					System.out.println();
				printProgressTimer = System.nanoTime();
			}
//			if (percentage > nextPointPercentage) {
//				nextPointPercentage += 10;
//				System.out.print(".");
//			}
				
		}
		System.out.println();
		System.out.println(Math.round((System.nanoTime()-startTime)/1000000)/1000.+" s");
		System.out.println();
		for (int i = 0 ; i < neededIterations.length ; i++)
			System.out.println(neededIterations[i]);
		BufferedImage t = img;
		this.img = temp_img;
		temp_img = t;
	}

	public void render() {
		Graphics g = jframe.getGraphics();
		tick();
		if (img != null) {
			g.drawImage(img, 0, 0, null);
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void tick() {
		if (changed) {
			changed = false;
			generateImage();
		}
	}

	public void clickedMouse(int x, int y) {
		double real = getReal(x);
		double imag = getImag(y);
		midx = real;
		midy = imag;
//		iterations *= 1.1;
		range *= 0.5;
		changed = true;
	}
	
	public double getReal(double imgx) {
		return (((double)(h-imgx)/h)*range+range/2)+midx;
	}
	
	public double getImag(double imgy) {
		return (((double)(h-imgy)/h)*range-range/2)+midy;
	}

	public void setQuality(int quality) {
		System.out.println("set quality: "+quality);
		this.quality = quality;
	}

	public void setRedraw() {
		System.out.println("set redraw...");
		changed = true;
	}

	public int getIterations() {
		return iterations;
	}
	
	public void setIterations(int iterations) {
		if (iterations != this.iterations)
			System.out.println("set interations: "+iterations);
		this.iterations = iterations;
	}
}
