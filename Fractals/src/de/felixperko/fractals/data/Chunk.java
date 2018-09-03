package de.felixperko.fractals.data;

import java.awt.Color;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import de.felixperko.fractals.util.Position;

public class Chunk {
	
	int color = 0;
	{
		double r = Math.random();
		if (r < 1./3)
			color = Color.RED.getRGB();
		else if (r < 2./3)
			color = Color.GREEN.getRGB();
		else
			color = Color.BLUE.getRGB();
	}
	
	final int chunk_size;
	int arr_size;
	public int finishedIterations;
	
	public float[] iterationsSum;
	public float[] iterationsSumSq;
	public float[] diff;
	public float[] currentPosX;
	public float[] currentPosY;
	public int[] sampleCount;
	
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
	double stepPriorityMultiplier = 1;
	
	boolean disposed = false;
	boolean arraysInstantiated = false;
	
	Position gridPos;
	
	public Chunk(int chunk_size, DataDescriptor dataDescriptor, Grid grid, Position gridPos) {
		this.chunk_size = chunk_size;
		this.dataDescriptor = dataDescriptor;
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
		arraysInstantiated = true;
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
	
	float colorOffset = 0;
	
	/**
	 * fill the pixels according to finished iterations
	 */
	private void fillPixels() {
		int i = 0;
		for (int x = 0 ; x < chunk_size ; x++) {
			for (int y = 0 ; y < chunk_size ; y++) {
				if (sampleCount[i] == 0)
					imageData.setPixel(y, x, color);
				else {
					float sat2 = getAvgIterations(i);
					sat2 = (float) Math.log10(sat2);
					float b = sat2 > 0 ? 1 : 0;
					imageData.setPixel(y, x, Color.HSBtoRGB((float) (colorOffset+sat2), 0.4f, b));
				}
				i++;
			}
		}
	}
	
	private float getAvgIterations(int i) {
		return iterationsSum[i]/sampleCount[i];
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

	public double getStepPriorityMultiplier() {
		return stepPriorityMultiplier;
	}

	public void setStepPriorityMultiplier(double stepPriorityMultiplier) {
		this.stepPriorityMultiplier = stepPriorityMultiplier;
	}

	public double getPriority() {
		return priorityMultiplier*stepPriorityMultiplier*distanceToMid;
	}

	public Position getGridPosition() {
		return gridPos;
	}
}
