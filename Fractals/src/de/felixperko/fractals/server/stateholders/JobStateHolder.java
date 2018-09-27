package de.felixperko.fractals.server.stateholders;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.client.util.NumberUtil;
import de.felixperko.fractals.server.state.DiscreteState;
import de.felixperko.fractals.server.state.RangeState;
import de.felixperko.fractals.server.state.StateHolder;
import de.felixperko.fractals.server.state.StateListener;
import de.felixperko.fractals.server.util.Position;

public class JobStateHolder extends StateHolder{
	
	Renderer renderer;
	
	public DiscreteState<Integer> statePower;
	public RangeState stateBiasReal;
	public RangeState stateBiasImag;
	
	public JobStateHolder(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	protected void stateSetup() {
		configurePower();
		configureBiasReal();
		configureBiasImag();
		
		addState(statePower);
		addState(stateBiasReal);
		addState(stateBiasImag);
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
