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

import de.felixperko.fractals.Tasks.TaskManager;
import de.felixperko.fractals.Tasks.perf.PerfInstance;
import de.felixperko.fractals.state.stateholders.RendererStateHolder;

import static de.felixperko.fractals.WindowHandler.*;

public class FractalRenderer {
	
	BufferedImage disp_img;
	BufferedImage draw_img;
	
	public DataDescriptor dataDescriptor;
	public DataContainer dataContainer;
	
	RendererStateHolder rendererStateHolder = new RendererStateHolder();
	
	protected boolean redraw = false;
	int drawn_depth = 0;
	
	long lastDrawn = 0;
	
	double q = 1;
	
	double cul_spacing_factor = 1;
	protected int disp_x = 0;
	protected int disp_y = 0;
	protected int disp_x2 = 0;
	protected int disp_y2 = 0;
	protected boolean disp_changed = false;
	
	float colorOffset = 0.0f;
	
	int maxIterations = 1000000;
	
	public FractalRenderer() {
	}
	
	public void init() {
		int w = FractalsMain.mainWindow.canvas.getSize().x;
		int h = FractalsMain.mainWindow.canvas.getSize().y;
		dataDescriptor = new DataDescriptor(-2, -2, 2.*w/h, 2, (int)Math.round(w*q), (int)Math.round(h*q), w, h, maxIterations, rendererStateHolder);
		dataDescriptor.calculateCoords();
		dataContainer = new DataContainer(dataDescriptor);
		draw_img = new BufferedImage(dataDescriptor.getDim_sampled_x(), dataDescriptor.getDim_sampled_y(), BufferedImage.TYPE_INT_RGB);
		disp_img = new BufferedImage(dataDescriptor.getDim_goal_x(), dataDescriptor.getDim_goal_y(), BufferedImage.TYPE_INT_RGB);
		disp_x2 = disp_img.getWidth();
		disp_y2 = disp_img.getHeight();
	}
	
	boolean newFinish = true;
	boolean newPartFinish = true;
	double nextGoal = 0.2;
	int currentGoalJob = 0;
	int currentDrawDepth = 0;
	
	public synchronized void render(Graphics g, boolean save) {
		int finishedDepth = checkDrawConditions();
		if (redraw || save)
			redraw(save, finishedDepth, null);
		g.drawImage(disp_img, 0, 0, WindowHandler.w, WindowHandler.h, disp_x, disp_y, disp_x2, disp_y2, null);
	}
	
	protected int checkDrawConditions() {
		int finishedDepth = FractalsMain.taskManager.getFinishedDepth();
		TaskManager tm = FractalsMain.taskManager;
		if (tm.getJobId() != currentGoalJob) {
			currentGoalJob = tm.getJobId();
			nextGoal = 0.2;
			currentDrawDepth = 0;
			newFinish = true;
			newPartFinish = true;
		}
		if (tm.isFinished()) {
			if (newFinish) {
				redraw = true;
				newFinish = false;
				System.out.println("redraw");
			}
		} else {
			newFinish = true;
		}
		if ((tm.last_step_closed_total > 1000 && tm.last_step_closed_relative < nextGoal && currentDrawDepth < finishedDepth)) {
			if (newPartFinish) {
				redraw = true;
				nextGoal = tm.last_step_closed_relative/2;
				newPartFinish = false;
				currentDrawDepth = finishedDepth;
				System.out.println("redraw temp");
			}
		} else {
			newPartFinish = true;
		}
		return finishedDepth;
	}

	protected void redraw(boolean save, int finishedDepth, PerfInstance parentPerfInstance) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int imgx = 0; imgx < draw_img.getWidth(); imgx++) {
			for (int imgy = 0; imgy < draw_img.getHeight(); imgy++) {
				int i = imgx+imgy*draw_img.getWidth();
				int it = dataContainer.samples[i];
				double real = dataContainer.currentSamplePos_real[i];
				double imag = dataContainer.currentSamplePos_imag[i];
				double absoluteSquared = real*real+imag*imag;
				if (it > 0 || absoluteSquared > 4) {
					float sat = (float)(it+1-Math.log(Math.log(Math.sqrt(absoluteSquared))/Math.log(2)));
					sat /= 1000;
					draw_img.setRGB(imgx, imgy, Color.HSBtoRGB(colorOffset+10*sat, 0.6f,1f));
				} else {
					if (it == -2)
						draw_img.setRGB(imgx, imgy, new Color(0f,0,0).getRGB());
					else
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
		if (save) {
			exportImage();
		}
		
		cul_spacing_factor = 1;
		disp_x = 0;
		disp_y = 0;
		disp_x2 = disp_img.getWidth();
		disp_y2 = disp_img.getHeight();
		redraw = false;
		drawn_depth = finishedDepth;
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
		return dataDescriptor.getMaxIterations();
	}
	
	public void setMaxIterations(int maxIterations) {
		dataDescriptor.setMaxIterations(maxIterations);
		reset();
	}
	
	public synchronized void setQuality(double quality) {
		if (quality == q)
			return;
		dataDescriptor.scaleBy((double)quality/q);
		this.q = quality;
		dataDescriptor.setDim_sampled_x((int)Math.round(dataDescriptor.getDim_goal_x()*q));
		dataDescriptor.setDim_sampled_y((int)Math.round(dataDescriptor.getDim_goal_y()*q));
		draw_img = new BufferedImage(dataDescriptor.getDim_sampled_x(), dataDescriptor.getDim_sampled_y(), BufferedImage.TYPE_INT_RGB);
		reset();
	}
	
	public void updateLocation(int mouse_x, int mouse_y, double spacing_factor) {
		try {
			double relX = spacing_factor < 1 ? mouse_x/(double)dataDescriptor.getDim_goal_x() : 0.5;
			double relY = spacing_factor < 1 ? mouse_y/(double)dataDescriptor.getDim_goal_y() : 0.5;
			double midX = relX*(disp_x2)-disp_x;
			double midY = relY*(disp_y2)-disp_y;
//			dataDescriptor.start_x = dataDescriptor.getXcoords()[(int)Math.round(midX*q)] - dataDescriptor.delta_x/2.;
//			dataDescriptor.start_y = dataDescriptor.getYcoords()[(int)Math.round(midY*q)] - dataDescriptor.delta_y/2.;
			dataDescriptor.scaleBy(spacing_factor);
			
			double midCoordsX = dataDescriptor.getXcoords()[(int)(midX*q)];
			double midCoordsY = dataDescriptor.getYcoords()[(int)(midY*q)];
			dataDescriptor.setStart_x(midCoordsX - dataDescriptor.getDelta_x()/2);
			dataDescriptor.setStart_y(midCoordsY - dataDescriptor.getDelta_y()/2);
			
			cul_spacing_factor *= spacing_factor;
			double rangeX = (disp_x2-disp_x)*spacing_factor;
			double rangeY = (disp_y2-disp_y)*spacing_factor;
			disp_x = (int) (midX - rangeX/2);
			disp_y = (int) (midY - rangeY/2);
			disp_x2 = (int) (midX + rangeX/2);
			disp_y2 = (int) (midY + rangeY/2);
			disp_changed = true;
			System.out.println("! changed to "+disp_x+","+disp_y+" - "+disp_x2+","+disp_y2);
			reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		dataDescriptor.refreshStateParams();
		dataDescriptor.calculateCoords();
		dataContainer = new DataContainer(dataDescriptor);
		FractalsMain.taskManager.setDataContainer(dataContainer);
		FractalsMain.taskManager.clearTasks();
		FractalsMain.taskManager.generateTasks();
	}

	public void addColorOffset(float additionalOffset) {
		colorOffset += additionalOffset;
		colorOffset = colorOffset%1;
		redraw = true;
	}

	public void setLocation(Location location) {
		double desiredRatio = ((double)dataDescriptor.getDim_sampled_x())/dataDescriptor.getDim_sampled_y();
		double deltaX = (location.getX2()-location.getX1());
		double deltaY = (location.getY2()-location.getY1());
		double adjustedDeltaY = deltaX/desiredRatio;
		System.out.println("deltaY: "+deltaY+" adjusted: "+adjustedDeltaY);
		double shiftY = 0.5*(adjustedDeltaY-deltaY);
		dataDescriptor.setStart_x(location.getX1());
		dataDescriptor.setStart_y(location.getY1()-shiftY);
		dataDescriptor.setEnd_x(location.getX2());
		dataDescriptor.setEnd_y(location.getY2()+shiftY);
		dataDescriptor.setSpacing(dataDescriptor.getDelta_x()/dataDescriptor.getDim_sampled_x());
		disp_x = 0;
		disp_y = 0;
		disp_x2 = disp_img.getWidth();
		disp_y2 = disp_img.getHeight();
		reset();
	}

	public Location getLocation(String name) {
		return new Location(dataDescriptor, name);
	}

	public RendererStateHolder getRendererStateHolder() {
		return rendererStateHolder;
	}

	public void setRendererStateHolder(RendererStateHolder rendererStateHolder) {
		this.rendererStateHolder = rendererStateHolder;
	}
}
