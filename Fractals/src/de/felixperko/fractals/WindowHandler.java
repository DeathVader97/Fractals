package de.felixperko.fractals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class WindowHandler {
	
	static int w = 1280, h = 720;
	
	int iterations;
	double midx = 2, midy = 0;
	double range = 3;
	
	int prev_iterations = iterations;
	
	boolean changed = true;
	
	JFrame jframe;
	BufferedImage img;
	BufferedImage temp_img;
	
	public WindowHandler() {
		jframe = new JFrame("Fractals");
		jframe.setVisible(true);
		jframe.setSize(w,h);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.addMouseListener(new MouseControls());
		generateImage();
	}

	private void generateImage() {
		if (temp_img == null)
			temp_img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int imgx = 0; imgx < w; imgx++) {
			for (int imgy = 0; imgy < h; imgy++) {
//				img.setRGB(imgx, imgy, new Color((int)(255*imgx/w),(int)(255*imgy/h),0).getRGB());
				double sat = 0;
				for (int subx = 0 ; subx < 2 ; subx++) {
					for (int suby = 0 ; suby < 2 ; suby++) {
						double creal = getReal(imgx+subx*0.5);
						double cimag = getImag(imgy+suby*0.5);
						double real = 0;
						double imag = 0;
//						System.out.println(real+", "+imag);
						for (int i = 0 ; i < iterations ; i++) {
							if (real*real + imag*imag > 10) {
								sat += (1 - ((double)i/iterations))*0.25;
								break;
							}
							double real2 = real*real - (imag*imag) - creal;
							double imag2 = real*imag + (imag*real) - cimag;
							real = real2;
							imag = imag2;
						}
					}
				}
				Color color = Color.getHSBColor(1-(float) sat, 1, (float)sat);
				temp_img.setRGB(imgx, imgy, color.getRGB());
			}
		}
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
	}
	
	public void tick() {
		iterations = 2000;
		if (prev_iterations != iterations) {
			prev_iterations = iterations;
			changed = true;
		}
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
		return (((double)(h-imgx)/h)*range-range/2)+midx;
	}
	
	public double getImag(double imgy) {
		return (((double)(h-imgy)/h)*range-range/2)+midy;
	}
}
