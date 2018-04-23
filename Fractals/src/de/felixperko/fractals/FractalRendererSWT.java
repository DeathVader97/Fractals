package de.felixperko.fractals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class FractalRendererSWT extends FractalRenderer {
	
	Display display;
	
	public Image disp_img;
	Rectangle lastBounds = new Rectangle(0,0,0,0);
	public Image draw_img;
	
	public FractalRendererSWT(Display display) {
		super();
		this.display = display;
		draw_img = new Image(display, new Rectangle(0, 0, dataDescriptor.dim_sampled_x, dataDescriptor.dim_sampled_y));
		disp_img = new Image(display, new Rectangle(0, 0, dataDescriptor.dim_goal_x, dataDescriptor.dim_goal_y));
	}
	
	public void render(PaintEvent e, boolean save) {
		try {
			save = FractalsMain.mainWindow.save;
			int finishedDepth = checkDrawConditions();
			System.out.println("render redraw="+redraw+" save="+save);
			Rectangle bounds = FractalsMain.mainWindow.canvas.getBounds();
			if (redraw || save) {
				if (!bounds.equals(lastBounds)) {
					lastBounds = bounds;
					resized();
				}
				redraw(save, finishedDepth);
				System.out.println("draw with "+disp_x+","+disp_y+" - "+disp_x2+","+disp_y2);
				e.gc.drawImage(disp_img, disp_x, disp_y, disp_x2-disp_x, disp_y2-disp_y, 0, 0, bounds.width, bounds.height);
				if (save)
					FractalsMain.mainWindow.save = false;
			}
			if (disp_changed) {
				disp_changed = false;
				int disp_w = disp_x2-disp_x;
				int disp_h = disp_y2-disp_y;
				int minX = (int) (disp_x >= 0 ? 0 : bounds.width*((double)-disp_x)/disp_w);
				int minY = (int) (disp_y >= 0 ? 0 : bounds.width*((double)-disp_y)/disp_h);
				int maxX = (int) (disp_w < disp_img.getBounds().width ? bounds.width : bounds.width*(((double)disp_w)/disp_img.getBounds().width));
				int maxY = (int) (disp_h < disp_img.getBounds().height ? bounds.height : bounds.height*(((double)disp_h)/disp_img.getBounds().height));
				System.out.println(minX+","+minY+" - "+maxX+","+maxY);
				e.gc.drawImage(disp_img, disp_x > 0 ? disp_x : 0, disp_y > 0 ? disp_y : 0, disp_w, disp_h, minX, minY, maxX, maxY);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	protected void redraw(boolean save, int finishedDepth) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		PaletteData palette = new PaletteData(0xFF , 0xFF00 , 0xFF0000);
		ImageData draw_data = draw_img.getImageData();
		draw_data.palette = palette;
		for (int imgx = 0; imgx < draw_img.getBounds().width; imgx++) {
			for (int imgy = 0; imgy < draw_img.getBounds().height; imgy++) {
				int i = imgx+imgy*draw_img.getBounds().width;
				int it = dataContainer.samples[i];
				double real = dataContainer.currentSamplePos_real[i];
				double imag = dataContainer.currentSamplePos_imag[i];
				double absoluteSquared = real*real+imag*imag;
				if (it > 0 || absoluteSquared > 4) {
					float sat = (float)(it+1-Math.log(Math.log(Math.sqrt(absoluteSquared))/Math.log(2)));
					sat /= 1000;
					draw_data.setPixel(imgx, imgy, Color.HSBtoRGB(colorOffset+10*sat, 0.6f,1f));
				} else {
					if (it == -2)
						draw_data.setPixel(imgx, imgy, new Color(0f,0,0).getRGB());
					else
						draw_data.setPixel(imgx, imgy, 0);
				}
			}
		}
		draw_img.dispose();
		draw_img = new Image(display, draw_data);
		lastDrawn = System.nanoTime();
		
		GC gc = new GC(disp_img);
		gc.setAntialias(SWT.ON);
		
		if (redraw || drawn_depth != finishedDepth) {
			try {
				gc.drawImage(draw_img, 0, 0, draw_img.getBounds().width, draw_img.getBounds().height, 0, 0, disp_img.getBounds().width, disp_img.getBounds().height);
				System.out.println("disp_img updated");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(FractalsMain.mainWindow.canvas.getBounds().toString());
			}
		}
		if (save) {
			exportImage();
		}
		gc.dispose();
		
		cul_spacing_factor = 1;
		disp_x = 0;
		disp_y = 0;
		disp_x2 = disp_img.getBounds().width;
		disp_y2 = disp_img.getBounds().height;
		redraw = false;
		drawn_depth = finishedDepth;
	}
	
	private void exportImage() {
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{disp_img.getImageData()};
		loader.save("swtimg.png", SWT.IMAGE_PNG);
		System.out.println("saved image");
	}


	public boolean isRedraw() {
		checkDrawConditions();
		return redraw;
	}
	
	public void resized() {
		Rectangle disp_bounds = FractalsMain.mainWindow.canvas.getBounds();
		Rectangle draw_bounds = new Rectangle(0, 0, (int)Math.round(disp_bounds.width*q), (int)Math.round(disp_bounds.height*q));
		disp_img.dispose();
		disp_img = new Image(display, new Rectangle(0, 0, disp_bounds.width, disp_bounds.height));
		draw_img.dispose();
		draw_img = new Image(display, new Rectangle(0, 0, draw_bounds.width, draw_bounds.height));
		dataDescriptor.dim_sampled_x = draw_bounds.width;
		dataDescriptor.dim_sampled_y = draw_bounds.height;
		dataDescriptor.dim_goal_x = disp_bounds.width;
		dataDescriptor.dim_goal_y = disp_bounds.height;
		reset();
	}
	
	@Override
	public synchronized void setQuality(double quality) {
		if (quality == q)
			return;
		dataDescriptor.spacing /= (double)quality/q;
		this.q = quality;
		dataDescriptor.dim_sampled_x = (int)Math.round(dataDescriptor.dim_goal_x*q);
		dataDescriptor.dim_sampled_y = (int)Math.round(dataDescriptor.dim_goal_y*q);
		Rectangle draw_bounds = new Rectangle(0, 0, (int)Math.round(disp_img.getBounds().width*q), (int)Math.round(disp_img.getBounds().height*q));
		draw_img = new Image(display, new Rectangle(0, 0, draw_bounds.width, draw_bounds.height));
		reset();
	}

}
