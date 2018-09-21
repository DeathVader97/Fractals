package de.felixperko.fractals.renderer.calculators;

import java.util.BitSet;

import de.felixperko.fractals.Tasks.ChunkTask;
import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.renderer.calculators.infrastructure.AbstractCalculator;
import de.felixperko.fractals.renderer.steps.ProcessingStep;
import de.felixperko.fractals.renderer.steps.patternprovider.BasicPatternProvider;
import de.felixperko.fractals.renderer.steps.patternprovider.Pattern;
import de.felixperko.fractals.util.CategoryLogger;
import de.felixperko.fractals.util.Position;

public class MandelbrotCalculator extends AbstractCalculator{
	
	public MandelbrotCalculator(DataDescriptor dataDescriptor, Task task) {
		super(dataDescriptor, task);
	}
	
	@Override
	public void calculate_samples(int[] sampleIndices, int[] currentIterations, int maxIterations, double[] currentpos_real, double[] currentpos_imag, int[] results) {

		int dim_x = descriptor.getDim_sampled_x();
//		int dim_y = descriptor.getDim_sampled_y();
		
		int pow = descriptor.getFractalPower();
		double startReal = descriptor.getFractalBias().getX();
		double startImag = descriptor.getFractalBias().getY();
		
		int globalMaxIterations = descriptor.getMaxIterations();
		double[] xCoords;
		double[] yCoords;
		try {
			xCoords = descriptor.getXcoords();
			yCoords = descriptor.getYcoords();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		mainLoop : 
		for (int i = 0 ; i < sampleIndices.length ; i++) {
			
			int index = sampleIndices[i];
			
			if (results[i] != 0)
				continue;
			
			task.changedIndices.add(i);
			
			int x = index % dim_x;
			int y = index / dim_x;
			
			int j = currentIterations[i];
			double real = (j == 0) ? startReal : xCoords[x];
			double imag = (j == 0) ? startImag : yCoords[y];
			double creal = xCoords[x];
			double cimag = yCoords[y];
//			double real = currentpos_real[i], imag = currentpos_imag[i];
////			int j = 0;
////			double real = 0, imag = 0;
//			double creal = (j == 0) ? startReal : descriptor.xcoords[x];
//			double cimag = (j == 0) ? startImag : descriptor.ycoords[y];
			
			for ( ; j < maxIterations ; j++) {
				run_iterations++;
				double realSq = 0;
				double imagSq = 0;
				for (int k = 1 ; k < pow ; k++){
//					real = Math.abs(real);
//					imag = Math.abs(imag);
					realSq = real*real;
					imagSq = imag*imag;
					imag = 2*real*imag;
					real = realSq - imagSq;
				}
				real += creal;
				imag += cimag;
				
				if (realSq + imagSq > (1 << 16)) {//outside -> done
					results[i] = j;
					currentIterations[i] = j;
					currentpos_real[i] = real;
					currentpos_imag[i] = imag;
					continue mainLoop;
				}
			}
			
			//still not outside
			if (maxIterations < globalMaxIterations) { //not done -> store temp result
				currentIterations[i] = maxIterations;
				currentpos_real[i] = real;
				currentpos_imag[i] = imag;
			} else { //max iterations reached -> declared as in the mandelbrot set
				results[i] = -1;
			}
		}
	}

	@Override
	public void calculate_samples(Chunk chunk, ProcessingStep step) {
		chunk.setGetIndexMask(step.getIndexMask());
		Pattern pattern = step.getPattern();
		BitSet activeIndices = step.getActiveIndices();
		int maxiterations = step.getMaxIterations();
		
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
			
			int sampleCount = chunk.getSampleCount(i);
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
//				int j = chunk.finishedIterations;
				int j = 0;
				double real;
				double imag;
				if (j == 0 || !chunk.getProcessingStepState().isDefaultState()){
					real = startReal;
					imag = startImag;
				} else {
					real = chunk.getCurrentPosX(i);
					imag = chunk.getCurrentPosY(i);
				}
				double creal = xPos;
				double cimag = yPos;
				
//				if (j == 0)
				
				for ( ; j < maxiterations ; j++) {
					run_iterations++;
					double realSq = 0;
					double imagSq = 0;
					for (int l = 1 ; l < pow ; l++){
	//					real = Math.abs(real);
	//					imag = Math.abs(imag);
						realSq = real*real;
						imagSq = imag*imag;
						imag = 2*real*imag;
						real = realSq - imagSq;
					}
					real += creal;
					imag += cimag;
					
					if (realSq + imagSq > (1 << 16)) {//outside -> done
						float iterations = (float) (j < 0 ? j : Math.sqrt( j + 1 -  Math.log( Math.log(real*real+imag*imag)*0.5 / Math.log(2) ) / Math.log(2)  ));
						chunk.addIterationsSum(i, iterations);
						chunk.addIterationsSumSq(i, iterations*iterations);
						if (k == 0) {
							chunk.setCurrentPosX(i, (float) real);
							chunk.setCurrentPosY(i, (float) imag);
						}
						chunk.addSampleCount(i, 1);
						logIfDebug(chunk, i);
						continue sampleLoop;
					}
				}
				
				//still not outside
				if (maxiterations < globalMaxIterations && k == 0) { //not done -> store temp result
					chunk.setCurrentPosX(i, (float) real);
					chunk.setCurrentPosY(i, (float) imag);
				} else { //max iterations reached -> declared as in the mandelbrot set
					chunk.addSampleCount(i, 1);
					chunk.addFailSampleCount(i, 1);
					logIfDebug(chunk, i);
				}
			
			}
		}
		chunk.finishedIterations = maxiterations;
	}
	
	public Position getDelta(double sizeX, double sizeY){
		return new Position(descriptor.getDelta_x()*sizeX/descriptor.getDim_goal_x(), descriptor.getDelta_y()*sizeY/descriptor.getDim_goal_y());
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
//			real = Math.abs(real);
//			imag = Math.abs(imag);
			new_real = (real*real - (imag*imag));
			new_imag = 2*(real*imag);
			real = new_real;
			imag = new_imag;
		}
		real += startPos.getX();
		imag += startPos.getY();
		
		return new Position(real, imag);
	}
}
