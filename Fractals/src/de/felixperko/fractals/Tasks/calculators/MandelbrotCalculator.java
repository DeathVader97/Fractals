package de.felixperko.fractals.Tasks.calculators;

import de.felixperko.fractals.Tasks.Task;
import de.felixperko.fractals.Tasks.calculators.infra.AbstractCalculator;
import de.felixperko.fractals.data.Chunk;
import de.felixperko.fractals.data.DataDescriptor;
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
	public void calculate_samples(Chunk chunk, int maxiterations, Position[] samplepattern) {

		int dim_x = descriptor.getDim_sampled_x();
		
		int pow = descriptor.getFractalPower();
		double startReal = descriptor.getFractalBias().getX();
		double startImag = descriptor.getFractalBias().getY();
		
		int chunk_size = chunk.getChunkSize();
		int xShift = -1;
		int yShift = 0;
		
		double xPos = 0;
		double yPos = 0;
		
		int globalMaxIterations = descriptor.getMaxIterations();
		
		Position delta = new Position(descriptor.getDelta_x()/descriptor.getDim_goal_x(), descriptor.getDelta_y()/descriptor.getDim_goal_y());

		mainLoop : 
		for (int i = 0 ; i < chunk_size*chunk_size ; i++) {
			
			//update position
			xShift++;
			yPos = chunk.getY(yShift);
			if (xShift >= chunk_size) {
				xShift = 0;
				yShift++;
				yPos = chunk.getY(yShift);
			}
			xPos = chunk.getX(xShift);
			
			if (chunk.isDisposed() || chunk.sampleCount == null) {
				continue;
			}
			
			Position prevsampleoffset = null;
			
			sampleLoop:
			for (int k = chunk.sampleCount[i] ; k < samplepattern.length ; k++) {
				
				Position sampleoffset = samplepattern[k];
				xPos += sampleoffset.getX()*delta.getX();
				yPos += sampleoffset.getY()*delta.getY();
				if (prevsampleoffset != null) {
					xPos -= prevsampleoffset.getX()*delta.getX();
					yPos -= prevsampleoffset.getY()*delta.getY();
				}
				prevsampleoffset = sampleoffset;
				
				int j = chunk.finishedIterations;
				double real = (j == 0) ? startReal : chunk.currentPosX[i];
				double imag = (j == 0) ? startImag : chunk.currentPosY[i];
				double creal = xPos;
				double cimag = yPos;
				
				if (j == 0)
					chunk.sampleCount[i]++;
				
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
						chunk.iterationsSum[i] += iterations;
						chunk.iterationsSumSq[i] += iterations*iterations;
						if (k == 0) {
							chunk.currentPosX[i] = (float) real;
							chunk.currentPosY[i] = (float) imag;
						}
						continue sampleLoop;
					}
				}
				
				//still not outside
				if (maxiterations < globalMaxIterations && k == 0) { //not done -> store temp result
					chunk.currentPosX[i] = (float) real;
					chunk.currentPosY[i] = (float) imag;
				} else { //max iterations reached -> declared as in the mandelbrot set
					
				}
			
			}
		}
		chunk.finishedIterations = maxiterations;
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
