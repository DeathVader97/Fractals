package de.felixperko.fractals.state;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.util.NumberUtil;
import de.felixperko.fractals.util.Position;

public class MainStateHolder extends StateHolder {
	
	FractalsMain main;
	
	DiscreteState<Integer> stateThreadCount;
	DiscreteState<Integer> statePower;
	RangeState stateBiasReal;
	RangeState stateBiasImag;
	State<Position> statecursorPosition;
	State<Position> stateCursorImagePosition;
	SwitchState stateFullscreen;
	DiscreteState<Integer> stateVisualizationSteps;
	
	
	public MainStateHolder(FractalsMain main) {
		this.main = main;
		stateSetup();
	}

	private void stateSetup() {
		
		configureThreadCount();
		configureFullscreen();
		configurePower();
		configureCursorPosition();
		configureCursorImagePosition();
		configureBiasReal();
		configureBiasImag();
		configureVisualizationSteps();
		
		addState(stateThreadCount);
		addState(stateFullscreen);
		addState(statePower);
		addState(statecursorPosition);
		addState(stateCursorImagePosition);
		addState(stateBiasReal);
		addState(stateBiasImag);
		addState(stateVisualizationSteps);
	}

	private void configureVisualizationSteps() {
		stateVisualizationSteps = new DiscreteState<Integer>("visulization steps", 0) {
			@Override
			public Integer getNext() {
				return getValue()+1;
			}
			@Override
			public Integer getPrevious() {
				if (getValue() <= 0)
					return null;
				return getValue()+1;
			}
		};
		stateVisualizationSteps.setIncrementable(true).setDecrementable(true);
	}

	private void configureBiasImag() {
		stateBiasImag = new RangeState("bias imag", 2000){
			@Override
			public Double getOutput() {
				return NumberUtil.getRoundedDouble(getValue()/1000.-2, 3);
			}
		};
		stateBiasImag.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.mainWindow.getMainRenderer().reset();
			}
		});
		stateBiasImag.setProperties(0, 4000, 1);
	}

	private void configureBiasReal() {
		stateBiasReal = new RangeState("bias real", 2000){
			@Override
			public Double getOutput() {
				return NumberUtil.getRoundedDouble(getValue()/1000.-2, 3);
			}
		};
		stateBiasReal.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.mainWindow.getMainRenderer().reset();
			}
		});
		stateBiasReal.setProperties(0, 4000, 1);
	}

	private void configureCursorImagePosition() {
		stateCursorImagePosition = new State<Position>("cursor image position", new Position()){
			@Override
			public String getValueString() {
				Position p = getValue();
				return p.getX()+", "+p.getY();
			}
		}.setVisible(false);
	}

	private void configureCursorPosition() {
		statecursorPosition = new State<Position>("cursor position", new Position()){
			@Override
			public String getValueString() {
				Position p = getValue();
				return (int)Math.round(p.getX())+", "+(int)Math.round(p.getY());
			}
		};
	}

	private void configureFullscreen() {
		stateFullscreen = new SwitchState("Fullscreen", false);
		stateFullscreen.addStateListener(new StateListener<Boolean>() {
			@Override
			public void valueChanged(Boolean oldValue, Boolean newValue) {
				FractalsMain.mainWindow.shell.setFullScreen(newValue);
			}
		});
	}

	private void configureThreadCount() {
		stateThreadCount = new DiscreteState<Integer>("Thread Count", Runtime.getRuntime().availableProcessors()) {
			@Override
			public Integer getPrevious() {
				if (getValue() <= 1)
					return null;
				return getValue()-1;
			}
			
			@Override
			public Integer getNext() {
				return getValue()+1;
			}
		};
		stateThreadCount.setIncrementable(true).setDecrementable(true);
		stateThreadCount.addStateListener(new StateListener<Integer>() {
			@Override
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.threadManager.setThreadCount(newValue);
			};
		});
	}

	private void configurePower() {
		statePower = new DiscreteState<Integer>("Mandelbrot Power", 2) {
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
				FractalsMain.mainWindow.getMainRenderer().reset();
			}
		});
	}
}
