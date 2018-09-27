package de.felixperko.fractals.server.calculators;

import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.tasks.Task;
import de.felixperko.fractals.server.util.Position;

public class BurningShipCalculator extends AbstractMandelbrotCalculator{
	
	public BurningShipCalculator(DataDescriptor dataDescriptor, Task task) {
		super(dataDescriptor, task);
	}

	@Override
	protected void innerLoop() {
		real = Math.abs(real);
		imag = Math.abs(imag);
		realSq = real*real;
		imagSq = imag*imag;
		imag = 2*real*imag;
		real = realSq - imagSq;
	}

	@Override
	protected void innerLoopPosition() {
		real = Math.abs(real);
		imag = Math.abs(imag);
		new_real = (real*real - (imag*imag));
		new_imag = 2*(real*imag);
		real = new_real;
		imag = new_imag;
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
			real = Math.abs(real);
			imag = Math.abs(imag);
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

