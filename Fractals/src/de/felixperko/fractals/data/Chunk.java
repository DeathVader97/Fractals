package de.felixperko.fractals.data;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import de.felixperko.fractals.renderer.painter.FailRatioPainter;
import de.felixperko.fractals.renderer.painter.Painter;
import de.felixperko.fractals.renderer.painter.SamplesPainter;
import de.felixperko.fractals.renderer.painter.StandardPainter;
import de.felixperko.fractals.util.Position;

public class Chunk {
	
	public static AtomicInteger count_active = new AtomicInteger(0);
	
	final int chunk_size;
	int arr_size;
	public int finishedIterations;
	
	public float[] iterationsSum;
	public float[] iterationsSumSq;
	public float[] diff;
	public float[] currentPosX;
	public float[] currentPosY;
	public int[] sampleCount;
	public int[] failSampleCount;
	
	Grid grid;
	DataDescriptor dataDescriptor;
	
	ImageData imageData;
	public Image image;
	
	boolean redraw = false;
	
	public boolean imageCalculated = false;
	
	Position startPosition;
	Position delta;
	
	double distanceToMid;
	double priorityMultiplier = 1;
	double stepPriorityOffset = 50;
	
	boolean disposed = false;
	boolean arraysInstantiated = false;
	
	Position gridPos;
	
	Painter painter = new FailRatioPainter();
	
	float colorOffset = 0; //TODO move to painter

	PatternState patternState;
	int drawnPatternState = -1;

	private boolean readyToDraw = false;
	
	public Chunk(int chunk_size, DataDescriptor dataDescriptor, Grid grid, Position gridPos) {
		this.chunk_size = chunk_size;
		this.dataDescriptor = dataDescriptor;
		this.patternState = new PatternState(dataDescriptor.getPatternProvider());
		this.gridPos = gridPos;
		this.startPosition = grid.getSpaceOffset(gridPos);
		this.delta = new Position(dataDescriptor.getDelta_x()*chunk_size/dataDescriptor.dim_sampled_x, dataDescriptor.getDelta_y()*chunk_size/dataDescriptor.dim_sampled_y);
		this.grid = grid;
	}
	
	public void instantiateArrays() {
		arr_size = chunk_size*chunk_size;
		iterationsSum = new float[arr_size];
		iterationsSumSq = new float[arr_size];
		currentPosX = new float[arr_size];
		currentPosY = new float[arr_size];
		diff = new float[arr_size];
		sampleCount = new int[arr_size];
		failSampleCount = new int[arr_size];
		arraysInstantiated = true;
		count_active.incrementAndGet();
	}
	
	public boolean arraysInstantiated() {
		return iterationsSum != null;
	}
	
	public int getIndex(int relX, int relY) {
		return relX*chunk_size + relY;
	}
	
	public void calculatePixels() {
		prepareArrays();
		prepareImageData();
		fillPixels();
		setRedrawFlags();
	}
	
	private void prepareArrays() {
		if (arraysInstantiated)
			return;
		instantiateArrays();
	}

	/**
	 * create ImageData object if null
	 */
	private void prepareImageData() {
		if (imageData == null)
			imageData = new ImageData(chunk_size, chunk_size, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
	}
	
	/**
	 * fill the pixels according to finished iterations
	 */
	private void fillPixels() {
		int i = 0;
		for (int x = 0 ; x < chunk_size ; x++) {
			for (int y = 0 ; y < chunk_size ; y++) {
				painter.paint(imageData, this, i, x, y);
				i++;
			}
		}
	}
	
	public float getFailRatio(int i) {
		int samples = sampleCount[i];
		if (samples == 0)
			return 0;
		return failSampleCount[i]/(float)samples;
	}

	public float getAvgIterations(int i) {
		float sucessfulIterations = sampleCount[i]-failSampleCount[i];
		if (sucessfulIterations == 0)
			return -1;
		return iterationsSum[i]/sucessfulIterations;
	}

	private float getAvgIterations(int x, int y) {
		return getAvgIterations(getIndex(x,y));
	}

	private void setRedrawFlags() {
		imageCalculated = true;
		redraw = true;
	}
	
	public void refreshImage(Device device) {
		
		if (image != null) {
			image.dispose();
		}
		image = new Image(device, imageData);
		drawnPatternState = patternState.id;
	}
	
	/**
	 * returns the approximate size of the contained data
	 * @return
	 */
	public static int getByteSize(int arr_size) {
		
		int array_count = 6;
		int int_arr_count = 1;
		int float_arr_count = 5;
		int references_count = 2;
		
		int int_bytes = 4;
		int float_bytes = 4;
		int references_bytes = 8;
		
		int sizes_size = int_bytes*2;
		return sizes_size + references_bytes*references_count + array_count*16 + arr_size*(int_arr_count*int_bytes + float_arr_count*float_bytes);
	}

	public void dispose() {
		count_active.decrementAndGet();
		disposed = true;
		arraysInstantiated = false;
		if (image != null)
			image.dispose();
		imageData = null;
		
		arr_size = 0;
		iterationsSum = null;
		iterationsSumSq = null;
		currentPosX = null;
		currentPosY = null;
		diff = null;
		sampleCount = null;
		failSampleCount = null;
	}

	public boolean isDisposed() {
		return disposed;
	}

	public Position getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Position startPosition) {
		this.startPosition = startPosition;
	}

	public Position getDelta() {
		return delta;
	}

	public void setDelta(Position delta) {
		this.delta = delta;
	}
	
	public double getX(int iX) {
		return startPosition.getX() + (iX)*delta.getX()/chunk_size;
	}
	
	public double getY(int iY) {
		return startPosition.getY() + (iY)*delta.getY()/chunk_size;
	}
	
	public int getChunkSize() {
		return chunk_size;
	}
	
	public void setDistanceToMid(double distanceToMid) {
		this.distanceToMid = distanceToMid;
	}
	
	public double getDistanceToMid() {
		return distanceToMid;
	}
	
	public double getPriorityMultiplier() {
		return priorityMultiplier;
	}

	public void setPriorityMultiplier(double priorityMultiplier) {
		this.priorityMultiplier = priorityMultiplier;
	}

	public double getStepPriorityOffset() {
		return stepPriorityOffset;
	}

	public void setStepPriorityOffset(double stepPriorityMultiplier) {
		this.stepPriorityOffset = stepPriorityMultiplier;
	}

	public double getPriority() {
		return priorityMultiplier*distanceToMid + stepPriorityOffset*(patternState.id+1);
	}

	public Position getGridPosition() {
		return gridPos;
	}

	public void calculateDiff() {
//		int i = 0;
//		for (int x = 0 ; x < chunk_size ; x++) {
//			for (int y = 0 ; y < chunk_size ; y++) {
//				double diff = 0;
//				int c = 0;
//				double avgIterations = getAvgIterations(i);
//				for (int x2 = x-1 ; x2 <= x+1 ; x2++) {
//					for (int y2 = y-1 ; y2 <= y+1 ; y2++) {
//						if (x2 >= 0 && y2 >= 0 && x2 < chunk_size && y2 < chunk_size && (x2 != x || y2 != y)) {
//							int i2 = x2*chunk_size + y2;
//							diff += Math.abs(getAvgIterations(i2) - avgIterations);
//							c++;
//						}
//					}
//				}
//				this.diff[i] = (float) (diff/c);
//				i++;
//			}
//		}
		
		
		int rad = 0;
		int boxBlurIterations = 1;
		int radDim = rad*2+1;
				
		double[] pass1 = new double[arr_size];
		double[] pass_buffer = new double[radDim];
		int pass_buffer_offset = 0;
		
		int iterationCount = 0;
		
		int kernelMid = rad;
		int index = 0;
		for (int x = 0 ; x < chunk_size ; x++) {
			for (int y = 0 ; y < chunk_size ; y++) {
//				double neighbour_sum = 0;
//				int c = 0;
//				if (x > 0) {
//					c++;
//					neighbour_sum += replaceNaN(adjSamples[x-1][y]);
//				}
//				if (y > 0) {
//					c++;
//					neighbour_sum += replaceNaN(adjSamples[x][y-1]);
//				}
//				if (x < samples.length - 1) {
//					c++;
//					neighbour_sum += replaceNaN(adjSamples[x+1][y]);
//				}
//				if (y < samples[0].length - 1) {
//					c++;
//					neighbour_sum += replaceNaN(adjSamples[x][y+1]);
//				}
//				double value = replaceNaN(adjSamples[x][y]);
//				double neighbour_avg = neighbour_sum/c;
//				diff[x][y] = Math.abs(value - neighbour_avg);
//				iterationCount += c+1;

				double value = replaceNaN(getAvgIterations(x,y));
				double delta = 0;
				int c = 0;
				if (x > 0) {
					c++;
					delta += Math.abs(value - replaceNaN(getAvgIterations(x-1, y)));
				}
				if (y > 0) {
					c++;
					delta += Math.abs(value - replaceNaN(getAvgIterations(x, y-1)));
				}
				if (x < chunk_size - 1) {
					c++;
					delta += Math.abs(value - replaceNaN(getAvgIterations(x+1, y)));
				}
				if (y < chunk_size - 1) {
					c++;
					delta += Math.abs(value - replaceNaN(getAvgIterations(x, y+1)));
				}
				diff[index] = (float) (delta/c);
				index++;
				iterationCount += c+1;
			}
		}
		
		double[] buff = new double[radDim];
		double addedValue = 0;
		int buff_index = 0;
		int buff_size = 0;
		
		for (int i = 0 ; i < boxBlurIterations ; i++) {
			//vertical box blur
			for (int x = 0 ; x < chunk_size ; x++) {
				//read first values
				for (int y = 0 ; y < rad ; y++) {
					double value = diff[getIndex(x, y)];
					addedValue += value;
					buff[buff_index] = value;
					buff_index++;
					buff_size++;
				}
				//actual loop
				for (int y = 0 ; y < chunk_size ; y++) {
					if (y+rad >= radDim) {
						addedValue -= buff[buff_index];
						buff_size--;
					}
					if (y+rad <= chunk_size-1) {
						double value = diff[getIndex(x, y+rad)];
						buff_size++;
						addedValue += value;
						buff[buff_index] = value;
						buff_index = (buff_index+1) % radDim;
					}
					pass1[getIndex(x, y)] = addedValue/radDim;
				}
				buff_index = 0;
				buff_size = 0;
				addedValue = 0;
			}
			//horizontal box blur
			for (int y = 0 ; y < chunk_size ; y++) {
				//read first values
				for (int x = 0 ; x < rad ; x++) {
					double value = pass1[getIndex(x, y)];
					addedValue += value;
					buff[buff_index] = value;
					buff_index++;
					buff_size++;
				}
				//actual loop
				for (int x = 0 ; x < chunk_size ; x++) {
					if (x+rad >= radDim) {
						addedValue -= buff[buff_index];
						buff_size--;
					}
					if (x+rad <= chunk_size-1) {
						double value = pass1[getIndex(x+rad, y)];
						buff_size++;
						addedValue += value;
						buff[buff_index] = value;
						buff_index = (buff_index+1) % radDim;
					}
//					if (i == boxBlurIterations-1)
//						fluctuance[x][y] = addedValue/buff_size;
//					else
						diff[getIndex(x, y)] = (float) (addedValue/buff_size);
				}
				buff_index = 0;
				buff_size = 0;
				addedValue = 0;
			}
		}
	}
	
	private double replaceNaN(double d) {
		if (Double.isNaN(d))
			return 0;
		return d;
	}
	
	public PatternState getPatternState() {
		return patternState;
	}

	public boolean refreshNeeded() {
		return drawnPatternState < patternState.id;
	}
	
	public boolean isReadyToDraw() {
		return readyToDraw;
	}
	
	public void setReadyToDraw(boolean readyToDraw) {
		this.readyToDraw = readyToDraw;
	}
}
