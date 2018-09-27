package de.felixperko.fractals.server.calculators;

import java.util.Random;

import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.tasks.Task;
import de.felixperko.fractals.server.util.Position;

public class TestCalculator extends AbstractMandelbrotCalculator{
	
	public TestCalculator(DataDescriptor dataDescriptor, Task task) {
		super(dataDescriptor, task);
	}
	
	Random r = new Random();

	@Override
	protected void innerLoop() {
		if (r.nextBoolean()){
			real = Math.abs(real);
			imag = Math.abs(imag);
		}
		realSq = real*real;
		imagSq = imag*imag;
		imag = 2*real*imag;
		real = realSq - imagSq;
	}

	@Override
	protected void innerLoopPosition() {
		if (r.nextBoolean()){
			real = Math.abs(real);
			imag = Math.abs(imag);
		}
		new_real = (real*real - (imag*imag));
		new_imag = 2*(real*imag);
		real = new_real;
		imag = new_imag;
	}

	@Override
	public Position getIterationForPosition(Position startPos, Position currentPos) {
		// TODO Auto-generated method stub
		return null;
	}
}