package de.felixperko.fractals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.felixperko.fractals.Tasks.perf.PerfInstance;
import de.felixperko.fractals.Tasks.threading.IterationPositionThread;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.NumberUtil;
import de.felixperko.fractals.util.Position;

public class FractalRendererSWT extends FractalRenderer {
	
	Display display;
	
	public Image disp_img;
	Rectangle lastBounds = new Rectangle(0,0,0,0);
//	public Image draw_img;
	
	State<Integer> stateVisulizationSteps;
	
	IterationPositionThread ipt;
	
	org.eclipse.swt.graphics.Color black;
	
	public FractalRendererSWT(Display display) {
		super();
		this.display = display;
		this.black = new org.eclipse.swt.graphics.Color(display, new RGB(0,0,0));
//		draw_img = new Image(display, new Rectangle(0, 0, dataDescriptor.dim_sampled_x, dataDescriptor.dim_sampled_y));
	}
	
	@Override
	public void init() {
		super.init();
		
	}
	
	public void prepare() {
		disp_img = new Image(display, new Rectangle(0, 0, dataDescriptor.getDim_goal_x(), dataDescriptor.getDim_goal_y()));
		stateVisulizationSteps = FractalsMain.mainStateHolder.getState("visulization steps", Integer.class);
	}
	
	public void startIterationPositionThread() {
		ipt = FractalsMain.threadManager.getIterationWorkerThread();
	}
	
//	public boolean allowRedraw = true;
	
	public void render(PaintEvent e, boolean save) {
		PerfInstance renderPerf = PerfInstance.createNewAndBegin("render");
		try {
			PerfInstance checkConditionsPerf = PerfInstance.createNewSubInstanceAndBegin("check_conditions", renderPerf);
			
			save = FractalsMain.mainWindow.save;
			int finishedDepth = checkDrawConditions();
			
			checkConditionsPerf.end();
			
			System.out.println("render redraw="+redraw+" save="+save);
			Rectangle bounds = FractalsMain.mainWindow.canvas.getBounds();
			if (redraw || save) {
				if (!bounds.equals(lastBounds)) {
					PerfInstance resizeResetPerf = PerfInstance.createNewSubInstanceAndBegin("resize_reset", renderPerf);
					lastBounds = bounds;
					resized();
					resizeResetPerf.end();
				}
				redraw(save, finishedDepth, renderPerf);
				System.out.println("draw with "+disp_x+","+disp_y+" - "+disp_x2+","+disp_y2);
				PerfInstance drawImagePerf = PerfInstance.createNewSubInstanceAndBegin("draw_image", renderPerf);
				e.gc.drawImage(disp_img, disp_x, disp_y, disp_x2-disp_x, disp_y2-disp_y, 0, 0, bounds.width, bounds.height);
				drawImagePerf.end();
				if (save)
					FractalsMain.mainWindow.save = false;
			}
			int visSteps = stateVisulizationSteps.getValue();
			disp_changed = true; //TODO implement in "buffered completion scheduling"
			if (disp_changed) {
				PerfInstance drawPathPerf = PerfInstance.createNewSubInstanceAndBegin("draw_path", renderPerf);
				disp_changed = false;

				int imgW = disp_img.getBounds().width;
				int imgH = disp_img.getBounds().height;
				
				int sourceX = disp_x;
				int sourceY = disp_y;
				int sourceW = disp_x2-disp_x;
				int sourceH = disp_y2-disp_y;
				int destX = 0;
				int destY = 0;
				int destW = imgW;
				int destH = imgH;
				
				float scaleX = ((float)destW)/sourceW;
				float scaleY = ((float)destH)/sourceH;
				
				if (sourceX < 0) {
					destX += -sourceX*scaleX;
					sourceX = 0;
				}
				if (sourceY < 0) {
					destY += -sourceY*scaleY;
					sourceY = 0;
				}
				if (disp_x2 > imgW) {
					int d = disp_x2 - imgW;
					destW -= d*scaleX;
					sourceW -= d;
				}
				if (disp_y2 > imgH) {
					int d = disp_y2 - imgH;
					destH -= d*scaleY;
					sourceH -= d;
				}
				if (sourceX+sourceW > imgW) {
					sourceW = imgW-sourceX;
				}
				if (sourceY+sourceH > imgH) {
					sourceH = imgH-sourceY;
				}
				
				try {
					e.gc.setForeground(black);
					e.gc.fillRectangle(0, 0, e.width, e.height);
					e.gc.drawImage(disp_img, sourceX, sourceY, sourceW, sourceH, destX, destY, destW, destH);
				} catch (IllegalArgumentException ex) {
					System.err.println("Illegal argument at scaling");
					System.err.println(imgW+","+imgH+" -> "+e.width+","+e.height);
					System.err.println(sourceX+","+sourceY+" "+sourceW+","+sourceH+" -> "+destX+","+destY+" "+destW+","+destH);
					ex.printStackTrace();
				}
				
				System.out.println(visSteps+", "+ipt.getIterations());
				int calculatedIterations = ipt.getIterations();
				if (calculatedIterations < visSteps)
					visSteps = calculatedIterations;
				if (visSteps > 0) {
					ArrayList<Position> positions = ipt.getPositions();
					Position p = positions.get(0);
					Position pScreen = null;
					Position p2 = null;
					Position p2Screen = null;
					e.gc.setAntialias(SWT.ON);
					long t1 = System.nanoTime();
					for (int i = 1 ; i < visSteps ; i++) {
						org.eclipse.swt.graphics.Color color = new org.eclipse.swt.graphics.Color(display, new RGB((i*360f/visSteps)*0.6666f+0.0f*360, 0.5f, 1f));
						p2 = positions.get(i);
						p2Screen = p2.planeToScreen(dataDescriptor);
						pScreen = p.planeToScreen(dataDescriptor);
						e.gc.setForeground(color);
						e.gc.setAlpha((int)(Math.pow(i, -0.5)*(255-16)+16));
						e.gc.drawLine((int)(pScreen.getX()), (int)(pScreen.getY()), (int)(p2Screen.getX()), (int)(p2Screen.getY()));
						e.gc.setAlpha(255);
						e.gc.drawOval((int)(pScreen.getX()-2), (int)(pScreen.getY()-2), 4, 4);
						
						p = p2;
						pScreen = p2Screen;
						color.dispose();
					}
					long t2 = System.nanoTime();
					System.out.println("drawing "+visSteps+" took "+(int)((t2-t1)*NumberUtil.NS_TO_MS)+"ms.");
				}
				drawPathPerf.end();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		renderPerf.end();
		renderPerf.printChildSecondsToLog(3);
	}
	
	@Override
	protected void redraw(boolean save, int finishedDepth, PerfInstance parentPerfInstance) {
		lastDrawn = System.nanoTime();
		
		if (redraw || drawn_depth != finishedDepth) {
			ImageData data = disp_img.getImageData();
			while (true){
				try {
					putOnImageData(disp_img.getBounds().width, disp_img.getBounds().height, data, parentPerfInstance);
					break;
				} catch (ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
			disp_img.dispose();
			disp_img = new Image(display, data);
			CategoryLogger.INFO.log("renderer", "drew image");
		}
		if (save) {
			exportImage();
		}
//		gc.dispose();
		
		cul_spacing_factor = 1;
		disp_x = 0;
		disp_y = 0;
		disp_x2 = disp_img.getBounds().width;
		disp_y2 = disp_img.getBounds().height;
		redraw = false;
		drawn_depth = finishedDepth;
	}

	private void putOnImageData(int width, int height, ImageData draw_data, PerfInstance parentPerfInstance) {
		int qualityScaling = dataDescriptor.getDim_sampled_x()/dataDescriptor.getDim_goal_x();
		SampledDataContainer sdc = new SampledDataContainer(dataContainer, qualityScaling, parentPerfInstance);
		PaletteData palette = new PaletteData(0xFF , 0xFF00 , 0xFF0000);
		draw_data.palette = palette;
//		int firstFinished = -1;
//		for (int imgx = 0; imgx < width; imgx++) {
//			for (int imgy = 0; imgy < height; imgy++) {
//				int i = imgx+imgy*width;
//				int v = dataContainer.samples[i];
//				if (v > 0 && (firstFinished == -1 || v < firstFinished))
//					firstFinished = v;
//			}
//		}
		PerfInstance setPixelPerf = PerfInstance.createNewSubInstanceAndBegin("set_pixel", parentPerfInstance);
		for (int imgx = 0; imgx < width; imgx++) {
			for (int imgy = 0; imgy < height; imgy++) {
				double it = sdc.samples[imgx][imgy];
//				double absoluteSquared = sdc.absSq[imgx][imgy];
				if (it > 0) {
//					float sat = (float)(it+1-Math.log(Math.log(absoluteSquared)*0.5)/Math.log(2));
					float sat = (float)(it);
					
					float sat2 = (float) Math.log10(sat);

					float b = sdc.fluctuance == null ? 0 : (float)Math.pow(sdc.fluctuance[imgx][imgy],0.15)*0.9f;
//					float notFinishedFraction = sdc.notFinishedFraction[imgx][imgy];
//					if (notFinishedFraction > 0)
//						b = 1;
//					float b = 1;
//					b *= 360 - sat%360;
//					if (b > 1)
//						b = 1f;
//					else if (b < 0.2f || b == Float.NaN)
//						b = 0.2f;
					
//					b *= (1-notFinishedFraction);
//					b = 1;
					
//					sat2 %= 0.5;
					
					draw_data.setPixel(imgx, imgy, Color.HSBtoRGB((float) (colorOffset+sat2), 0.4f, b));
				} else {
					if (it == -2)
						draw_data.setPixel(imgx, imgy, new Color(0f,0,0).getRGB());
					else
						draw_data.setPixel(imgx, imgy, 0);
				}
			}
		}
		setPixelPerf.end();
	}
	
	private void exportImage() {
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{disp_img.getImageData()};
		try {
			File f = new File("swtimg.png");
			int c = 1;
			while (f.exists())
				f = new File("swtimg"+(c++)+".png");
			OutputStream os = new FileOutputStream(f);
			loader.save(os, SWT.IMAGE_PNG);
			System.out.println("saved image");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	public boolean isRedraw() {
		checkDrawConditions();
		return redraw;
	}
	
	public void resized() {
		if (dataDescriptor == null) {
			CategoryLogger.WARNING.log("rendererSWT", "attempted to resize while DataDescriptor is null");
			return;
		}
		Rectangle disp_bounds = FractalsMain.mainWindow.canvas.getBounds();
		CategoryLogger.INFO.log("resizing "+dataDescriptor.getDim_goal_x()+","+dataDescriptor.getDim_goal_y()+" to "+disp_bounds.width+","+disp_bounds.height);
		Rectangle calc_bounds = new Rectangle(0, 0, (int)Math.round(disp_bounds.width*q), (int)Math.round(disp_bounds.height*q));
		disp_img.dispose();
		disp_img = new Image(display, new Rectangle(0, 0, disp_bounds.width, disp_bounds.height));
//		draw_img.dispose();
//		draw_img = new Image(display, new Rectangle(0, 0, draw_bounds.width, draw_bounds.height));
		dataDescriptor.setDim_sampled_x(calc_bounds.width);
		dataDescriptor.setDim_sampled_y(calc_bounds.height);
		double oldDeltaX = dataDescriptor.getDelta_x();
		dataDescriptor.setGoalDimensions(disp_bounds.width, disp_bounds.height);
		double changeInDeltaX = dataDescriptor.getDelta_x()-oldDeltaX;
		dataDescriptor.setStart_x(dataDescriptor.getStart_x() - changeInDeltaX/2);
		dataDescriptor.setEnd_x(dataDescriptor.getStart_x() + dataDescriptor.getDelta_x());
		reset();
	}
	
	@Override
	public synchronized void setQuality(double quality) {
		if (quality == q)
			return;
		this.q = quality;
		double end_x = dataDescriptor.getEnd_x();
		double end_y = dataDescriptor.getEnd_y();
		dataDescriptor.setDim_sampled_x((int)Math.round(dataDescriptor.getDim_goal_x()*q));
		dataDescriptor.setDim_sampled_y((int)Math.round(dataDescriptor.getDim_goal_y()*q));
		dataDescriptor.setEnd_x(end_x);
		dataDescriptor.setEnd_y(end_y);
//		Rectangle draw_bounds = new Rectangle(0, 0, (int)Math.round(disp_img.getBounds().width*q), (int)Math.round(disp_img.getBounds().height*q));
//		draw_img = new Image(display, new Rectangle(0, 0, draw_bounds.width, draw_bounds.height));
		reset();
	}

	public void setRedraw(boolean b) {
		redraw = true;
	}

}
