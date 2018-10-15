package de.felixperko.fractals.server.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.painter.Painter;
import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.steps.masks.DefaultMask;
import de.felixperko.fractals.server.steps.masks.IndexMask;
import de.felixperko.fractals.server.util.Position;

public class Chunk {
	
	public static AtomicInteger count_active = new AtomicInteger(0);
	
	public static int getIndex(int relX, int relY, int chunkSize) {
		return relX*chunkSize + relY;
	}
	
	final int chunk_size;
	int arr_size;
	public int finishedIterations;

	float[] iterationsSum;
	float[] iterationsSumSq;
	float[] diff;
	float[] currentPosX;
	float[] currentPosY;
	int[] sampleCount;
	int[] failSampleCount;
	
	transient Grid grid; //required to access adjacent chunks in calculateDiff
	transient public ImageData imageData;
	transient public Image image;
//	DataDescriptor dataDescriptor;
	
//	boolean redraw = false;
	
	public boolean imageCalculated = false;
	
	Position startPosition;
	Position delta;
	
	double distanceToMid;
	double priorityMultiplier = 1;
	double stepPriorityOffset = 200;
	
	boolean disposed = false;
	boolean arraysInstantiated = false;
	
	protected final Position gridPos;
	
	protected final Position[] neigbourPositions = new Position[9];
	
	Painter painter;
	
	float colorOffset = 0; //TODO move to painter

	ProcessingStepState processingStepState;
	int drawnStep = -1;

	private boolean readyToDraw = false;
	private boolean readyToCalculate = false;
	
	Map<ChunkAccessType, IndexMask> setIndexMasks = new HashMap<>();
	Map<ChunkAccessType, IndexMask> getIndexMasks = new HashMap<>();
	
	int maxIterations = 10000;
	
	public Chunk(int chunk_size, DataDescriptor dataDescriptor, Grid grid, Position gridPos) {
		this.chunk_size = chunk_size;
//		this.dataDescriptor = dataDescriptor;
		this.processingStepState = new ProcessingStepState(dataDescriptor.getStepProvider());
		this.gridPos = gridPos;
		
		int i = 0;
		long gridX = (long) gridPos.getX();
		long gridY = (long) gridPos.getY();
		for (long x = gridX-1 ; x <= gridX+1 ; x++){
			for (long y = gridY-1 ; y <= gridY+1 ; y++){
				neigbourPositions[i++] = new Position(x, y);
			}
		}
		
		this.startPosition = grid.getSpaceOffset(gridPos);
		this.delta = new Position(dataDescriptor.getDelta_x()*chunk_size/dataDescriptor.dim_sampled_x, dataDescriptor.getDelta_y()*chunk_size/dataDescriptor.dim_sampled_y);
//		System.out.println("Chunk.new : deltaX = "+delta.getX()+" deltaY = "+delta.getY()+" gridPos: "+gridPos);
//		Thread.dumpStack();
		this.grid = grid;
		this.painter = grid.getRenderer().getPainter();
	}
	
	public IndexMask getGetIndexMask(ChunkAccessType accessType) {
		IndexMask mask = getIndexMasks.get(accessType);
		if (mask == null)
			return DefaultMask.instance;
		return mask;
	}
	
	public IndexMask getSetIndexMask(ChunkAccessType accessType) {
		IndexMask mask = setIndexMasks.get(accessType);
		if (mask == null)
			return DefaultMask.instance;
		return mask;
	}

	private int applySetIndexMasks(int i, ChunkAccessType accessType) {
		return getSetIndexMask(accessType).getIndex(i);
	}

	private int applyGetIndexMasks(int i, ChunkAccessType accessType) {
		return getGetIndexMask(accessType).getIndex(i);
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
		FractalsMain.clientStateHolder.stateActiveChunkCount.incrementValue();
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

	private void setRedrawFlags() {
		imageCalculated = true;
//		redraw = true;
	}
	
	public float getFailRatio(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		int samples = sampleCount[i];
		if (samples == 0)
			return 0;
		return failSampleCount[i]/(float)samples;
	}
	
	public float getFailRatio(int samples, int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		if (samples == 0)
			return 0;
		return failSampleCount[i]/(float)samples;
	}
	
	public float getVariance(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		int samples = sampleCount[i];
		float sumSqAvg = iterationsSumSq[i]/samples;
		float sumAvg = iterationsSum[i]/samples;
		return sumSqAvg - (sumAvg * sumAvg);
	}
	
	public float getStandardDeviation(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return (float) Math.sqrt(getVariance(i, accessType));
	}
	
	public float getAvgIterations(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		float sucessfulIterations = sampleCount[i]-failSampleCount[i];
		if (sucessfulIterations == 0)
			return -1;
		return iterationsSum[i]/sucessfulIterations;
	}

	private float getAvgIterations(int x, int y, ChunkAccessType accessType) {
		return getAvgIterations(getIndex(x,y), accessType);
	}
	
	private float getAvgIterationsGlobal(int x, int y, ChunkAccessType accessType) {
		Chunk c = getGlobalChunk(x,y);
		if (c == null || !c.arraysInstantiated)
			return -2;
		int s = getChunkSize();
		if (x < 0)
			x += s;
		else if (x >= s)
			x -= s;
		if (y < 0)
			y += s;
		else if (y >= s)
			y -= s;
		return c.getAvgIterations(x, y, accessType);
	}

	private Chunk getGlobalChunk(int x, int y) {
		int s = getChunkSize();
		if (x < 0) {
			if (y < 0) {
				return grid.getChunkOrNull(neigbourPositions[0]);
			} else if (y >= s){
				return grid.getChunkOrNull(neigbourPositions[2]);
			} else {
				return grid.getChunkOrNull(neigbourPositions[1]);
			}
		}
		else if (x >= s) {
			if (y < 0) {
				return grid.getChunkOrNull(neigbourPositions[6]);
			} else if (y >= s){
				return grid.getChunkOrNull(neigbourPositions[8]);
			} else {
				return grid.getChunkOrNull(neigbourPositions[7]);
			}
		} else {
			if (y < 0) {
				return grid.getChunkOrNull(neigbourPositions[3]);
			} else if (y >= s){
				return grid.getChunkOrNull(neigbourPositions[5]);
			} else {
				return this;
			}
		}
	}
	
	public void refreshImage(Device device) {
		
		if (image != null) {
			image.dispose();
		}
		image = new Image(device, imageData);
		drawnStep = processingStepState.stateNumber;
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
		FractalsMain.clientStateHolder.stateActiveChunkCount.decrementValue();
		disposed = true;
		arraysInstantiated = false;
		if (image != null)
			image.dispose();
		imageData = null;
		
//		arr_size = 0;
//		iterationsSum = null;
//		iterationsSumSq = null;
//		currentPosX = null;
//		currentPosY = null;
//		diff = null;
//		sampleCount = null;
//		failSampleCount = null;
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
		return grid.getSpacePosition(gridPos.addNew(new Position(iX/(double)chunk_size, 0))).getX();
//		return startPosition.getX() + (iX)*delta.getX()/chunk_size;
	}
	
	public double getY(int iY) {
		return grid.getSpacePosition(gridPos.addNew(new Position(0, iY/(double)chunk_size))).getY();
//		return startPosition.getY() + (iY)*delta.getY()/chunk_size;
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
		return priorityMultiplier*distanceToMid + stepPriorityOffset*(processingStepState.stateNumber+1);
	}

	public Position getGridPosition() {
		return gridPos;
	}

	public void calculateDiff() {
//		int rad = 0;
//		int boxBlurIterations = 1;
//		int radDim = rad*2+1;
		
		float[] otherValues = new float[4];
		ProcessingStep step = processingStepState.getProcessingStep();
		float factor = step.getDiffScale();
		int neighbourOffset = step.getNeigbourOffset();
				
//		double[] pass1 = new double[arr_size];
		int index = 0;
		for (int x = 0 ; x < chunk_size ; x++) {
			for (int y = 0 ; y < chunk_size ; y++) {

				double value = replaceNaN(getAvgIterations(x, y, ChunkAccessType.RENDERING));
				double delta = 0;
				otherValues[0] = getAvgIterationsGlobal(x-neighbourOffset, y, ChunkAccessType.RENDERING);
				otherValues[1] = getAvgIterationsGlobal(x, y-neighbourOffset, ChunkAccessType.RENDERING);
				otherValues[2] = getAvgIterationsGlobal(x+neighbourOffset, y, ChunkAccessType.RENDERING);
				otherValues[3] = getAvgIterationsGlobal(x, y+neighbourOffset, ChunkAccessType.RENDERING);
				int c = 0;
				for (int i = 0 ; i < otherValues.length ; i++) {
					float v = otherValues[i];
					if (!Float.isNaN(c) && v > 0) {
						delta += Math.abs(value - v);
						c++;
					}
				}
				float result = (c > 0) ? (float) (delta/c)*factor : 1;
				if (c > 0) {
					diff[index] = result;
				}
				index++;
			}
		}
		
//		double[] buff = new double[radDim];
//		double addedValue = 0;
//		int buff_index = 0;
//		int buff_size = 0;
		
//		for (int i = 0 ; i < boxBlurIterations ; i++) {
//			//vertical box blur
//			for (int x = 0 ; x < chunk_size ; x++) {
//				//read first values
//				for (int y = 0 ; y < rad ; y++) {
//					double value = diff[getIndex(x, y)];
//					addedValue += value;
//					buff[buff_index] = value;
//					buff_index++;
//					buff_size++;
//				}
//				//actual loop
//				for (int y = 0 ; y < chunk_size ; y++) {
//					if (y+rad >= radDim) {
//						addedValue -= buff[buff_index];
//						buff_size--;
//					}
//					if (y+rad <= chunk_size-1) {
//						double value = diff[getIndex(x, y+rad)];
//						buff_size++;
//						addedValue += value;
//						buff[buff_index] = value;
//						buff_index = (buff_index+1) % radDim;
//					}
//					pass1[getIndex(x, y)] = addedValue/radDim;
//				}
//				buff_index = 0;
//				buff_size = 0;
//				addedValue = 0;
//			}
//			//horizontal box blur
//			for (int y = 0 ; y < chunk_size ; y++) {
//				//read first values
//				for (int x = 0 ; x < rad ; x++) {
//					double value = pass1[getIndex(x, y)];
//					addedValue += value;
//					buff[buff_index] = value;
//					buff_index++;
//					buff_size++;
//				}
//				//actual loop
//				for (int x = 0 ; x < chunk_size ; x++) {
//					if (x+rad >= radDim) {
//						addedValue -= buff[buff_index];
//						buff_size--;
//					}
//					if (x+rad <= chunk_size-1) {
//						double value = pass1[getIndex(x+rad, y)];
//						buff_size++;
//						addedValue += value;
//						buff[buff_index] = value;
//						buff_index = (buff_index+1) % radDim;
//					}
////					if (i == boxBlurIterations-1)
////						fluctuance[x][y] = addedValue/buff_size;
////					else
//						diff[getIndex(x, y)] = (float) (addedValue/buff_size);
//				}
//				buff_index = 0;
//				buff_size = 0;
//				addedValue = 0;
//			}
//		}
	}
	
	private double replaceNaN(double d) {
		if (Double.isNaN(d))
			return 0;
		return d;
	}
	
	public ProcessingStepState getProcessingStepState() {
		return processingStepState;
	}

	public boolean refreshNeeded() {
		return drawnStep < processingStepState.stateNumber;
	}
	
	public boolean isReadyToDraw() {
		return readyToDraw;
	}
	
	public void setReadyToDraw(boolean readyToDraw) {
		this.readyToDraw = readyToDraw;
	}
	
	public boolean isReadyToCalculate() {
		return readyToCalculate;
	}

	public void setReadyToCalculate(boolean readyToCalculate) {
		this.readyToCalculate = readyToCalculate;
	}

	public float getIterationsSum(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return iterationsSum[i];
	}

	public void setIterationsSum(int i, float value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.iterationsSum[i] = value;
	}
	
	public void addIterationsSum(int i, float add, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.iterationsSum[i] += add;
	}

	public float getIterationsSumSq(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return iterationsSumSq[i];
	}

	public void setIterationsSumSq(int i, float value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.iterationsSumSq[i] = value;
	}
	
	public void addIterationsSumSq(int i, float add, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.iterationsSumSq[i] += add;
	}

	public float[] getDiff() {
		return diff;
	}

	public float getDiff(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return diff[i];
	}

	public void setDiff(int i, float value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.diff[i] = value;
	}

	public float getCurrentPosX(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return currentPosX[i];
	}

	public void setCurrentPosX(int i, float value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.currentPosX[i] = value;
	}

	public float getCurrentPosY(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return currentPosY[i];
	}

	public void setCurrentPosY(int i, float value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.currentPosY[i] = value;
	}

	public int getSampleCount(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return sampleCount[i];
	}

	public void setSampleCount(int i, int value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.sampleCount[i] = value;
	}

	public void addSampleCount(int i, int add, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.sampleCount[i] += add;
	}

	public int getFailSampleCount(int i, ChunkAccessType accessType) {
		i = applyGetIndexMasks(i, accessType);
		return failSampleCount[i];
	}

	public void setFailSampleCount(int i, int value, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.failSampleCount[i] = value;
	}

	public void addFailSampleCount(int i, int add, ChunkAccessType accessType) {
		i = applySetIndexMasks(i, accessType);
		this.failSampleCount[i] += add;
	}

	public int[] getSampleCount() {
		return sampleCount;
	}

	public void setSetIndexMask(IndexMask setIndexMask, ChunkAccessType accessType) {
		this.setIndexMasks.put(accessType, setIndexMask);
	}

	public void setGetIndexMask(IndexMask getIndexMask, ChunkAccessType accessType) {
		this.getIndexMasks.put(accessType, getIndexMask);
	}

	public Position[] getNeighbourPositions() {
		return neigbourPositions;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setRedrawNeeded(boolean b) {
		drawnStep = -1;
	}

	public ProcessingStep getProcessingStep() {
		if (processingStepState == null)
			return null;
		int num = processingStepState.getStateNumber();
		if (num == -1 || num > processingStepState.getStepProvider().getMaxState())
			return null;
		return processingStepState.getProcessingStep();
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
}
