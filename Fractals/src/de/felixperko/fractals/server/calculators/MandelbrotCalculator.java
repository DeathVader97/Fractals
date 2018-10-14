package de.felixperko.fractals.server.calculators;

import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.tasks.Task;

public class MandelbrotCalculator extends AbstractMandelbrotCalculator{
	
	public MandelbrotCalculator(DataDescriptor dataDescriptor, Task task) {
		super(dataDescriptor, task);
	}

	@Override
	protected void innerLoop() {
		double real_t = real*real_start - imag*imag_start;
		imag = real*imag_start + real_start*imag;
		real = real_t;
//		realSq = real*real;
//		imagSq = imag*imag;
	}

	@Override
	protected void innerLoopPosition() {
		new_real = (real*real - (imag*imag));
		new_imag = 2*(real*imag);
		real = new_real;
		imag = new_imag;
	}
}
