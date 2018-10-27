package de.felixperko.fractals.server.calculators;

import java.util.BitSet;

import de.felixperko.fractals.server.calculators.infrastructure.AbstractCalculator;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.data.ChunkAccessType;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.steps.ProcessingStep;
import de.felixperko.fractals.server.steps.patternprovider.Pattern;
import de.felixperko.fractals.server.tasks.Task;
import de.felixperko.fractals.server.util.Position;

public abstract class AbstractMandelbrotCalculator extends AbstractCalculator{

	protected double real;
	protected double real_start; //to allow higher powers
	protected double imag;
	protected double imag_start;
	protected double realSq = 0;
	protected double imagSq = 0;
	double new_real;
	double new_imag;
	
	public AbstractMandelbrotCalculator(DataDescriptor dataDescriptor, Task task) {
		super(dataDescriptor, task);
	}
	
	@Override
	public void calculate_samples(Chunk chunk, ProcessingStep step) {
		chunk.setGetIndexMask(step.getIndexMask(), ChunkAccessType.CALCULATION);
		Pattern pattern = step.getPattern();
		BitSet activeIndices = step.getActiveIndices();
		int maxiterations = Integer.min(step.getMaxIterations(), chunk.getMaxIterations());
		
		Position[] patternPositions = pattern.getPositions();
		
		int dim_x = descriptor.getDim_sampled_x();
		
		int pow = descriptor.getFractalPower();
		double startReal = descriptor.getFractalBias().getX();
		double startImag = descriptor.getFractalBias().getY();
		
		int chunk_size = chunk.getChunkSize();
		int xShift = 0;
		int yShift = -1;
		
		double xPos = 0;
		double yPos = 0;
		
		int globalMaxIterations = descriptor.getMaxIterations();
		
		Position delta = new Position(descriptor.getDelta_x()/descriptor.getDim_goal_x(), descriptor.getDelta_y()/descriptor.getDim_goal_y());
		
		boolean trackinghelper = false;
		
		int newSummedCount = pattern.getSummedCount();

		for (int i = 0 ; i < chunk_size*chunk_size ; i++) {
			
			//update position
			yShift++;
			yPos = chunk.getY(yShift);
			if (yShift >= chunk_size) {
				yShift = 0;
				xShift++;
				yPos = chunk.getY(yShift);
			}
			xPos = chunk.getX(xShift);
			
			if (!activeIndices.get(i))
				continue;
			
			if (chunk.isDisposed() || chunk.getSampleCount() == null) {
				continue;
			}
			
//			int sampleCount = chunk.getSampleCount(i);
//			if (sampleCount > 0 && sampleCount == chunk.failSampleCount[i] && chunk.sampleCount[i] > 0.1*newSummedCount) {
			int requiredSamples = getRequiredSampleCount(chunk, i);
//			if (isDebug(chunk, i))
//				System.out.println("state="+chunk.getPatternState().getId()+" required samples="+requiredSamples);
			
			boolean debug = isDebug(chunk, i);
			if (debug){
				trackinghelper = true;
			}
			
			Position prevsampleoffset = null;
			
			sampleLoop:
			for (int k = 0 ; k < requiredSamples ; k++) {
				
				Position sampleoffset = patternPositions[k];
				xPos += sampleoffset.getX()*delta.getX();
				yPos += sampleoffset.getY()*delta.getY();
				if (prevsampleoffset != null) {
					xPos -= prevsampleoffset.getX()*delta.getX();
					yPos -= prevsampleoffset.getY()*delta.getY();
				}
				prevsampleoffset = sampleoffset;
				
				//TODO fix continuing
				int j = chunk.getFinishedIterations();
//				int j = 0;
				if (j == 0 || !chunk.getProcessingStepState().isDefaultState()){
					real = startReal;
					imag = startImag;
				} else {
					real = chunk.getCurrentPosX(i, ChunkAccessType.CALCULATION);
					imag = chunk.getCurrentPosY(i, ChunkAccessType.CALCULATION);
				}
				Position pos = getPosition(xPos, yPos);
				double creal = pos.getX();
				double cimag = pos.getY();
				
//				if (j == 0)
				
				for ( ; j < maxiterations ; j++) {
					run_iterations++;
					realSq = 0;
					imagSq = 0;
					real_start = real;
					imag_start = imag;
					prepareLoop();
					for (int l = 1 ; l < pow ; l++){
						innerLoop();
					}
					real += creal;
					imag += cimag;
					
					if (real*real + imag*imag > (1 << 16)) {//outside -> done
						float iterations = (float) (j < 0 ? j : Math.sqrt( j + 1 -  Math.log( Math.log(real*real+imag*imag)*0.5 / Math.log(2) ) / Math.log(pow)  ));
						chunk.addIterationsSum(i, iterations, ChunkAccessType.CALCULATION);
						chunk.addIterationsSumSq(i, iterations*iterations, ChunkAccessType.CALCULATION);
						if (k == 0) {
							chunk.setCurrentPosX(i, (float) real, ChunkAccessType.CALCULATION);
							chunk.setCurrentPosY(i, (float) imag, ChunkAccessType.CALCULATION);
						}
						chunk.addSampleCount(i, 1, ChunkAccessType.CALCULATION);
						logIfDebug(chunk, i);
						continue sampleLoop;
					}
				}
				
//				//still not outside
				if (maxiterations < globalMaxIterations && k == 0) { //not done -> store temp result
					chunk.setCurrentPosX(i, (float) real, ChunkAccessType.CALCULATION);
					chunk.setCurrentPosY(i, (float) imag, ChunkAccessType.CALCULATION);
				}
				else { //max iterations reached -> declared as in the mandelbrot set
					chunk.addSampleCount(i, 1, ChunkAccessType.CALCULATION);
					chunk.addFailSampleCount(i, 1, ChunkAccessType.CALCULATION);
					logIfDebug(chunk, i);
				}
			
			}
		}
		chunk.setGetIndexMask(step.getIndexMask(), ChunkAccessType.RENDERING);
	}
	
	private Position getPosition(double xPos, double yPos) {
		return new Position(xPos, yPos);
//		Position pos = new Position(xPos, yPos);
//		return pos.div(pos.lengthSq());
	}

	/**
	 * can be overridden to prepare values for the loop
	 */
	protected void prepareLoop() {
	}

	@Override
	public Position getIterationForPosition(Position startPos, Position currentPos){
		
		int pow = descriptor.getFractalPower();
		
		double real = currentPos != null ? currentPos.getX() : descriptor.getFractalBias().getX();
		double imag = currentPos != null ? currentPos.getY() : descriptor.getFractalBias().getY();
		
		run_iterations++;
		
		double new_real = 1;
		double new_imag = 1;
		
		for (int k = 1 ; k < pow ; k++){
			innerLoopPosition();
		}
		real += startPos.getX();
		imag += startPos.getY();
		
		return new Position(real, imag);
	}

	protected abstract void innerLoopPosition();
}
