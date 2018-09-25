package de.felixperko.fractals.stateholders;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.renderer.AbstractRendererImpl;
import de.felixperko.fractals.renderer.Renderer;
import de.felixperko.fractals.state.DiscreteState;
import de.felixperko.fractals.state.RangeState;
import de.felixperko.fractals.state.StateHolder;
import de.felixperko.fractals.state.StateListener;
import de.felixperko.fractals.util.NumberUtil;

public class RendererStateHolder extends StateHolder{
	
	public static final String NAME_POWER = "fracal power";
	public static final String NAME_BIAS_REAL = "fractal bias real";
	public static final String NAME_BIAS_IMAG = "fractal bias imag";
	
	Renderer renderer;
	
	DiscreteState<Integer> statePower;
	RangeState stateBiasReal;
	RangeState stateBiasImag;
	RangeState stateColorScale;
	RangeState stateColorShift;
	
	public RendererStateHolder(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	protected void stateSetup() {
		configureColorScale();
		configureColorShift();
		configurePower();
		configureBiasReal();
		configureBiasImag();

		addState(stateColorScale);
		addState(stateColorShift);
		addState(statePower);
		addState(stateBiasReal);
		addState(stateBiasImag);
	}
	
	private void configureColorScale() {
		int steps = 20;
		float shiftPerStep = 0.1f;
		stateColorScale = new RangeState("color scale", steps/2) {
			@Override
			public Object getOutput() {
				return 1 + (getValue() - steps/2)*shiftPerStep;
			}
		};
		stateColorScale.setProperties(0, steps, 1);
		stateColorScale.setConfigurable(true);
		stateColorScale.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				float scale = 1 + (newValue - steps/2)*shiftPerStep;
				renderer.setColorScale(scale);
			}
		});
	}

	private void configureColorShift() {
		int steps = 100;
		stateColorShift = new RangeState("color shift", 0) {
			@Override
			public Object getOutput() {
				return getValue()/(float)steps;
			}
		};
		stateColorShift.setProperties(0, steps, 1);
		stateColorShift.setConfigurable(true);
		stateColorShift.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				renderer.setColorOffset(newValue/(float)steps);
			}
		});
	}

	private void configurePower() {
		statePower = new DiscreteState<Integer>(NAME_POWER, 2) {
			@Override
			public Integer getPrevious() {
				if (getValue() <= 2)
					return null;
				return getValue()-1;
			}
			
			@Override
			public Integer getNext() {
				return getValue()+1;
			}
		};
		statePower.setIncrementable(true).setDecrementable(true).addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.mainWindow.getMainRenderer().getDataDescriptor().refreshStateParams();
				FractalsMain.mainWindow.setRedraw(true);
				FractalsMain.mainWindow.getDisplay().asyncExec(() -> {FractalsMain.mainWindow.getMainRenderer().reset();});
			}
		});
	}

	private void configureBiasImag() {
		stateBiasImag = new RangeState(NAME_BIAS_IMAG, 2000){
			@Override
			public Double getOutput() {
				return NumberUtil.getRoundedDouble(getValue()/1000.-2, 3);
			}
		};
		stateBiasImag.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.mainWindow.getMainRenderer().getDataDescriptor().refreshStateParams();
				FractalsMain.mainWindow.setRedraw(true);
				FractalsMain.mainWindow.getDisplay().asyncExec(() -> {FractalsMain.mainWindow.getMainRenderer().reset();});
			}
		});
		stateBiasImag.setProperties(0, 4000, 1);
	}

	private void configureBiasReal() {
		stateBiasReal = new RangeState(NAME_BIAS_REAL, 2000){
			@Override
			public Double getOutput() {
				return NumberUtil.getRoundedDouble(getValue()/1000.-2, 3);
			}
		};
		stateBiasReal.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.mainWindow.getMainRenderer().getDataDescriptor().refreshStateParams();
				FractalsMain.mainWindow.setRedraw(true);
				FractalsMain.mainWindow.getDisplay().asyncExec(() -> {FractalsMain.mainWindow.getMainRenderer().reset();});
			}
		});
		stateBiasReal.setProperties(0, 4000, 1);
	}
}
