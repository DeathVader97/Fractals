package de.felixperko.fractals;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class FractalRenderer {
	
	BufferedImage disp_img;
	BufferedImage draw_img;
	
	public DataDescriptor dataDescriptor;
	public DataContainer dataContainer;
	
	boolean redraw = true;
	int drawn_depth = 0;
	
	long lastDrawn = 0;
	
	double q = 1;
	
	int disp_x = 0;
	int disp_y = 0;
	int disp_x2 = 0;
	int disp_y2 = 0;
	
	boolean allow_zooming = true;
	
	public FractalRenderer() {
		dataDescriptor = new DataDescriptor(-2, -2, 4./(WindowHandler.h*q), (int)Math.round(WindowHandler.w*q), (int)Math.round(WindowHandler.h*q), WindowHandler.w, WindowHandler.h, 1000);
		dataDescriptor.calculateCoords();
		dataContainer = new DataContainer(dataDescriptor);
		draw_img = new BufferedImage(dataDescriptor.dim_sampled_x, dataDescriptor.dim_sampled_y, BufferedImage.TYPE_INT_RGB);
		disp_img = new BufferedImage(dataDescriptor.dim_goal_x, dataDescriptor.dim_goal_y, BufferedImage.TYPE_INT_RGB);
		disp_x2 = disp_img.getWidth();
		disp_y2 = disp_img.getHeight();
	}
	
	public synchronized void render(Graphics g, boolean save) {
		
//		if (System.nanoTime()-lastDrawn > 10000000) {
//			redraw = true;
//		}
		int finishedDepth = FractalsMain.taskManager.getFinishedDepth();
		if ((save || redraw) || drawn_depth != finishedDepth) {
			for (int imgx = 0; imgx < draw_img.getWidth(); imgx++) {
				for (int imgy = 0; imgy < draw_img.getHeight(); imgy++) {
					int i = imgx+imgy*draw_img.getWidth();
					int it = dataContainer.samples[i];
					
//					double sat = (double)it / dataDescriptor.getMaxIterations();
//					Color color = Color.getHSBColor((float)-Math.pow(sat,0.1), 1, (float)Math.pow(sat, 0.2));
//					draw_img.setRGB(imgx, imgy, color.getRGB());
					
					if (it > 0 || (it == 0 && dataContainer.currentSampleIterations[i] < finishedDepth)) {
						double real = dataContainer.currentSamplePos_real[imgx+imgy*draw_img.getWidth()];
						double imag = dataContainer.currentSamplePos_imag[imgx+imgy*draw_img.getWidth()];
						float sat = (float)(it+1-Math.log(Math.log(Math.sqrt(real*real+imag*imag))/Math.log(2)));
						sat /= dataDescriptor.maxIterations;
						sat = (float)Math.pow(sat, 0.25);
						draw_img.setRGB(imgx, imgy, Color.HSBtoRGB(1f+5*sat, 0.6f,1f));
					} else {
						draw_img.setRGB(imgx, imgy, 0);
					}
				}
			}
			lastDrawn = System.nanoTime();
			Graphics2D g2 = (Graphics2D) disp_img.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			if (redraw || drawn_depth != finishedDepth)
				g2.drawImage(draw_img, 0, 0, WindowHandler.w, WindowHandler.h, 0, 0, draw_img.getWidth(), draw_img.getHeight(), null);
			if (save && FractalsMain.taskManager.isFinished()) {
				exportImage();
			}
			disp_x = 0;
			disp_y = 0;
			disp_x2 = disp_img.getWidth();
			disp_y2 = disp_img.getHeight();
			allow_zooming = true;
			redraw = false;
			drawn_depth = finishedDepth;
		}
		g.drawImage(disp_img, 0, 0, WindowHandler.w, WindowHandler.h, disp_x, disp_y, disp_x2, disp_y2, null);
	}

	private void exportImage() {
		try {
			int counter = 0;
			File f;
			while ((f = new File("img"+counter+".png")).exists()) {
				counter++;
			}
			ImageIO.write(draw_img, "png", f);
			System.out.println("exported image to "+f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DataDescriptor getDataDescriptor() {
		return dataDescriptor;
	}

	public void setDataDescriptor(DataDescriptor dataDescriptor) {
		this.dataDescriptor = dataDescriptor;
	}

	public DataContainer getDataContainer() {
		return dataContainer;
	}

	public void setDataContainer(DataContainer dataContainer) {
		this.dataContainer = dataContainer;
	}

	public int getMaxIterations() {
		return dataDescriptor.maxIterations;
	}
	
	public void setMaxIterations(int maxIterations) {
		dataDescriptor.maxIterations = maxIterations;
		reset();
	}
	
	public synchronized void setQuality(double quality) {
		if (quality == q)
			return;
		dataDescriptor.spacing /= (double)quality/q;
		this.q = quality;
		dataDescriptor.dim_sampled_x = (int)Math.round(dataDescriptor.dim_goal_x*q);
		dataDescriptor.dim_sampled_y = (int)Math.round(dataDescriptor.dim_goal_y*q);
		draw_img = new BufferedImage(dataDescriptor.dim_sampled_x, dataDescriptor.dim_sampled_y, BufferedImage.TYPE_INT_RGB);
		reset();
	}
	
	public void updateLocation(int mouse_x, int mouse_y, double spacing_factor) {
		if (!allow_zooming)
			return;
		try {
			allow_zooming = false;
			dataDescriptor.spacing *= spacing_factor;
			dataDescriptor.start_x = dataDescriptor.getXcoords()[(int)Math.round(mouse_x*q)] - dataDescriptor.spacing*dataDescriptor.dim_sampled_x/2.;
			dataDescriptor.start_y = dataDescriptor.getYcoords()[(int)Math.round(mouse_y*q)] - dataDescriptor.spacing*dataDescriptor.dim_sampled_y/2.;
			
			double rangeX = (disp_x2-disp_x)*spacing_factor;
			double rangeY = (disp_y2-disp_y)*spacing_factor;
			disp_x = (int) (mouse_x - rangeX/2);
			disp_y = (int) (mouse_y - rangeY/2);
			disp_x2 = (int) (mouse_x + rangeX/2);
			disp_y2 = (int) (mouse_y + rangeY/2);
			
			reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		dataDescriptor.start_x = mid_x - dataDescriptor.spacing*dataDescriptor.dim_sampled_x;
//		dataDescriptor.start_y = mid_y - dataDescriptor.spacing*dataDescriptor.dim_sampled_y;
	}
	
	public void reset() {
		dataDescriptor.calculateCoords();
		dataContainer = new DataContainer(dataDescriptor);
		FractalsMain.taskManager.setDataContainer(dataContainer);
		FractalsMain.taskManager.clearTasks();
		FractalsMain.taskManager.generateTasks();
		redraw = true;
	}
}
