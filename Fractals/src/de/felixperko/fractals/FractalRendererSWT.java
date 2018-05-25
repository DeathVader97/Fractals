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

import de.felixperko.fractals.Tasks.IterationPositionThread;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.util.NumberUtil;
import de.felixperko.fractals.util.Position;

public class FractalRendererSWT extends FractalRenderer {
	
	Display display;
	
	public Image disp_img;
	Rectangle lastBounds = new Rectangle(0,0,0,0);
//	public Image draw_img;
	
	State<Integer> stateVisulizationSteps;
	
	IterationPositionThread ipt = FractalsMain.threadManager.getIterationWorkerThread();
	
	org.eclipse.swt.graphics.Color black;
	
	public FractalRendererSWT(Display display) {
		super();
		this.display = display;
		this.black = new org.eclipse.swt.graphics.Color(display, new RGB(0,0,0));
//		draw_img = new Image(display, new Rectangle(0, 0, dataDescriptor.dim_sampled_x, dataDescriptor.dim_sampled_y));
		disp_img = new Image(display, new Rectangle(0, 0, dataDescriptor.getDim_goal_x(), dataDescriptor.getDim_goal_y()));
		stateVisulizationSteps = FractalsMain.mainStateHolder.getState("visulization steps", Integer.class);
	}
	
//	public boolean allowRedraw = true;
	
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
			int visSteps = stateVisulizationSteps.getValue();
			disp_changed = true; //TODO Testcode
			if (disp_changed) {
				disp_changed = false;

				int imgW = disp_img.getBounds().width;
				int imgH = disp_img.getBounds().height;
				
				int sourceMaxX = disp_x2 <= imgW ? disp_x2 : imgW;
				int sourceMaxY = disp_y2 <= imgH ? disp_y2 : imgH;
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
				
//				int disp_w = disp_x2-disp_x;
//				int disp_h = disp_y2-disp_y;
//				int imgW = disp_img.getBounds().width;
//				int imgH = disp_img.getBounds().height;
//				int minDispX = disp_x >= 0 ? disp_x : 0;
//				int minDispY = disp_y >= 0 ? disp_y : 0;
////				int adjDispW = (disp_x2 >= imgW) ? ((int)((disp_x2*((double)imgW/disp_w)))-minDispX) : (disp_x2-disp_x);
//				int adjDispW = disp_w;
//				if (disp_x2 >= imgW) {
//					adjDispW = (disp_w - (disp_x2-imgW));
//					disp_w -= (disp_x2-imgW);
//				}
//				int adjDispH = (disp_y2 >= imgH) ? ((int)((disp_y2*((double)imgH/disp_h)))-minDispY) : (disp_y2-disp_y);
//				int minDrawX = (int) (disp_x >= 0 ? 0 : bounds.width*((double)-disp_x)/disp_w);
//				int minDrawY = (int) (disp_y >= 0 ? 0 : bounds.width*((double)-disp_y)/disp_h);
////				int maxDrawX = (int) (adjDispW >= disp_w ? bounds.width : bounds.width*(((double)adjDispW)/imgW));
//				int maxDrawW = (int) (adjDispW >= disp_w ? bounds.width : bounds.width - (adjDispW - disp_w));
//				int maxDrawH = (int) (adjDispH < imgH ? bounds.height : bounds.height*(((double)adjDispH)/imgH));
//				System.out.println(minDrawX+","+minDrawY+" - "+maxDrawW+","+maxDrawH);
//				e.gc.drawImage(disp_img, minDispX, minDispY, adjDispW, adjDispH, minDrawX, minDrawY, maxDrawW, maxDrawH);
				
				int calculatedIterations = ipt.getIterations();
				if (calculatedIterations < visSteps)
					visSteps = calculatedIterations;
				if (visSteps > 0) {
//					allowRedraw = false;
					ArrayList<Position> positions = ipt.getPositions();
//					Position c = ((Position) FractalsMain.mainStateHolder.getState("cursor image position", Position.class).getOutput());
					Position p = positions.get(0);
					Position pScreen = null;
					Position p2 = null;
					Position p2Screen = null;
//					e.gc.setAntialias(SWT.ON);
					long t1 = System.nanoTime();
					for (int i = 1 ; i < visSteps ; i++) {
						org.eclipse.swt.graphics.Color color = new org.eclipse.swt.graphics.Color(display, new RGB((i*360f/visSteps)*0.6666f+0.0f*360, 0.5f, 1f));
//						p2 = p.complexSquared();
//						p2.setX(p2.getX()+p.getX());
//						p2.setY(p2.getY()+p.getY());
						p2 = positions.get(i);
						p2Screen = p2.planeToScreen(dataDescriptor);
						pScreen = p.planeToScreen(dataDescriptor);
	//					if (i != 0) {
//						System.out.println((((float)i*10)/visSteps));
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
//					allowRedraw = true;
				}
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
//		int width = ;
//		int height = ;
//		ImageData draw_data = draw_img.getImageData();
//		draw_img.dispose();
//		draw_img = new Image(display, draw_data);
		lastDrawn = System.nanoTime();
		
//		GC gc = new GC(disp_img);
//		gc.setAntialias(SWT.ON);
		
		if (redraw || drawn_depth != finishedDepth) {
			ImageData data = disp_img.getImageData();
			while (true){
				try {
					putOnImageData(disp_img.getBounds().width, disp_img.getBounds().height, data);
					break;
				} catch (ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}
			}
			disp_img.dispose();
			disp_img = new Image(display, data);
//			try {
//				gc.drawImage(draw_img, 0, 0, draw_img.getBounds().width, draw_img.getBounds().height, 0, 0, disp_img.getBounds().width, disp_img.getBounds().height);
				System.out.println("disp_img updated");
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println(FractalsMain.mainWindow.canvas.getBounds().toString());
//			}
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

	private void putOnImageData(int width, int height, ImageData draw_data) {
		int qualityScaling = dataDescriptor.getDim_sampled_x()/dataDescriptor.getDim_goal_x();
		SampledDataContainer sdc = new SampledDataContainer(dataContainer, qualityScaling);
		PaletteData palette = new PaletteData(0xFF , 0xFF00 , 0xFF0000);
		draw_data.palette = palette;
		int firstFinished = -1;
		for (int imgx = 0; imgx < width; imgx++) {
			for (int imgy = 0; imgy < height; imgy++) {
				int i = imgx+imgy*width;
				int v = dataContainer.samples[i];
				if (v > 0 && (firstFinished == -1 || v < firstFinished))
					firstFinished = v;
			}
		}
		for (int imgx = 0; imgx < width; imgx++) {
			for (int imgy = 0; imgy < height; imgy++) {
				double it = sdc.samples[imgx][imgy];
				double absoluteSquared = sdc.absSq[imgx][imgy];
				if (it > 0) {
					float sat = (float)(it+3-Math.log(Math.log(absoluteSquared)*0.5/Math.log(2))/Math.log(2));
//					float sat = (float)(it);
					
					float sat2 = (float) Math.log10(sat);

					float b = (float)Math.pow(sdc.fluctuance[imgx][imgy],0.2);
					if (b > 1)
						b = 1f;
					else if (b < 0.5 || b == Float.NaN)
						b = 0.5f;
					
					float notFinishedFraction = sdc.notFinishedFraction[imgx][imgy];
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
		Rectangle disp_bounds = FractalsMain.mainWindow.canvas.getBounds();
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
		dataDescriptor.setSpacing(dataDescriptor.getSpacing() / (double)quality/q);
		this.q = quality;
		dataDescriptor.setDim_sampled_x((int)Math.round(dataDescriptor.getDim_goal_x()*q));
		dataDescriptor.setDim_sampled_y((int)Math.round(dataDescriptor.getDim_goal_y()*q));
//		Rectangle draw_bounds = new Rectangle(0, 0, (int)Math.round(disp_img.getBounds().width*q), (int)Math.round(disp_img.getBounds().height*q));
//		draw_img = new Image(display, new Rectangle(0, 0, draw_bounds.width, draw_bounds.height));
		reset();
	}

	public void setRedraw(boolean b) {
		redraw = true;
	}

}
