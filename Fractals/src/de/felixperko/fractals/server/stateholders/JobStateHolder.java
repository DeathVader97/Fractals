package de.felixperko.fractals.server.stateholders;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.gui.SelectionState;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.calculators.BurningShipCalculator;
import de.felixperko.fractals.server.calculators.MandelbrotCalculator;
import de.felixperko.fractals.server.calculators.TestCalculator;
import de.felixperko.fractals.server.calculators.infrastructure.SampleCalculator;
import de.felixperko.fractals.server.state.DiscreteState;
import de.felixperko.fractals.server.state.RangeState;
import de.felixperko.fractals.server.state.StateChangeListener;
import de.felixperko.fractals.server.state.StateHolder;
import de.felixperko.fractals.server.state.StateListener;
import de.felixperko.fractals.server.util.Position;

public class JobStateHolder extends StateHolder{
	
	Renderer renderer;
	
	public DiscreteState<Integer> statePower;
	public RangeState stateBiasReal;
	public RangeState stateBiasImag;
	public SelectionState<Class<? extends SampleCalculator>> stateCalculator;
	
	public JobStateHolder(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	protected void stateSetup() {
		configureCalculator();
		configurePower();
		configureBiasReal();
		configureBiasImag();

		addState(stateCalculator);
		addState(statePower);
		addState(stateBiasReal);
		addState(stateBiasImag);
	}
	
	private void configureCalculator() {
		stateCalculator = new SelectionState<Class<? extends SampleCalculator>>("calculator", MandelbrotCalculator.class) {
			@Override
			public String getName(Class<? extends SampleCalculator> obj) {
				return obj.getSimpleName();
			}
			@Override
			public String getValueString() {
				return getValue().getSimpleName();
			}
		};
		stateCalculator.addStateListener(new StateChangeListener<Class<? extends SampleCalculator>>(stateCalculator){
			@Override
			public void valueChanged(Class<? extends SampleCalculator> oldValue,
					Class<? extends SampleCalculator> newValue) {
				FractalsMain.mainWindow.getMainRenderer().getDataDescriptor().refreshStateParams();
				FractalsMain.mainWindow.setRedraw(true);
				FractalsMain.mainWindow.getDisplay().asyncExec(() -> {FractalsMain.mainWindow.getMainRenderer().reset();});
			}
		});
		List<Class<? extends SampleCalculator>> options = new ArrayList<>();
		options.add(MandelbrotCalculator.class);
		options.add(BurningShipCalculator.class);
		options.add(TestCalculator.class);
		stateCalculator.setOptions(options);
	}

	private void configurePower() {
		statePower = new DiscreteState<Integer>("fracal power", 2) {
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
		stateBiasImag = new RangeState("fractal bias imag", 2000){
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
		stateBiasReal = new RangeState("fractal bias real", 2000){
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

	public Position getBias() {
		return new Position((double) stateBiasReal.getOutput(), (double) stateBiasImag.getOutput());
	}
}
