package de.felixperko.fractals.state;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.util.NumberUtil;
import de.felixperko.fractals.util.Position;

public class MainStateHolder extends StateHolder {
	
	FractalsMain main;
	
	DiscreteState<Integer> stateThreadCount;
	DiscreteState<Integer> statePower;
	RangeState biasReal;
	RangeState biasImag;
	State<Position> cursorPosition;
	State<Position> cursorImagePosition;
	
	
	public MainStateHolder(FractalsMain main) {
		this.main = main;
		stateSetup();
	}

	private void stateSetup() {
		
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
		addState(statePower);
		
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
		addState(stateThreadCount);
		
		SwitchState switchState = new SwitchState("Fullscreen", false);
		switchState.addStateListener(new StateListener<Boolean>() {
			@Override
			public void valueChanged(Boolean oldValue, Boolean newValue) {
				FractalsMain.mainWindow.shell.setFullScreen(newValue);
			}
		});
		addState(switchState);
		
		cursorPosition = new State<Position>("cursor position", new Position()){
			@Override
			public String getValueString() {
				Position p = getValue();
				return (int)Math.round(p.getX())+", "+(int)Math.round(p.getY());
			}
		};
		addState(cursorPosition);
		
		cursorImagePosition = new State<Position>("cursor image position", new Position()){
			@Override
			public String getValueString() {
				Position p = getValue();
				return p.getX()+", "+p.getY();
			}
		}.setVisible(false);
		addState(cursorImagePosition);
		
		biasReal = new RangeState("bias real", 2000){
			@Override
			protected Double getOutput() {
				return NumberUtil.getRoundedDouble(getValue()/1000.-2, 3);
			}
		};
		biasReal.setProperties(0, 4000, 1);
		addState(biasReal);
		
		biasImag = new RangeState("bias imag", 2000){
			@Override
			protected Double getOutput() {
				return NumberUtil.getRoundedDouble(getValue()/1000.-2, 3);
			}
		};
		biasImag.setProperties(0, 4000, 1);
		addState(biasImag);
//		cursorPosition.addOnDrawEvent();
		
//		DiscreteState<Integer> testState = new DiscreteState<Integer>("Teststate", 10) {
//			@Override
//			public Integer getNext() {
//				Integer v = getValue()+10;
//				if (v > 100)
//					return null;
//				return v;
//			}
//			@Override
//			public Integer getPrevious() {
//				Integer v = getValue()-10;
//				if (v < 0)
//					return null;
//				return v;
//			}
//		};
//		testState.setIncrementable(true).setDecrementable(true);
//		addState(testState);
	}
}
