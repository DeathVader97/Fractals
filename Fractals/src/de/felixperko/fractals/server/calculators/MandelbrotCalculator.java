package de.felixperko.fractals.server.calculators;

import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.tasks.Task;

public class MandelbrotCalculator extends AbstractMandelbrotCalculator{
	
	public MandelbrotCalculator(DataDescriptor dataDescriptor, Task task) {
		super(dataDescriptor, task);
	}

	@Override
	protected void innerLoop() {
		realSq = real*real;
		imagSq = imag*imag;
		imag = 2*real*imag;
		real = realSq - imagSq;
	}

	@Override
	protected void innerLoopPosition() {
		new_real = (real*real - (imag*imag));
		new_imag = 2*(real*imag);
		real = new_real;
		imag = new_imag;
	}
}
