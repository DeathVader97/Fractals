package de.felixperko.fractals.state;

import de.felixperko.fractals.FractalsMain;

public class MainStateHolder extends StateHolder {
	
	FractalsMain main;
	
	DiscreteState<Integer> stateThreadCount;
	
	public MainStateHolder(FractalsMain main) {
		this.main = main;
		stateSetup();
	}

	private void stateSetup() {
		
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
			public void valueChanged(Integer oldValue, Integer newValue) {
				FractalsMain.threadManager.setThreadCount(newValue);
			};
		});
		addState(stateThreadCount);
		
		SwitchState switchState = new SwitchState("test switch", true);
		addState(switchState);
		
		DiscreteState<Integer> testState = new DiscreteState<Integer>("Teststate", 10) {
			@Override
			public Integer getNext() {
				Integer v = getValue()+10;
				if (v > 100)
					return null;
				return v;
			}
			@Override
			public Integer getPrevious() {
				Integer v = getValue()-10;
				if (v < 0)
					return null;
				return v;
			}
		};
		testState.setIncrementable(true);
		testState.setDecrementable(true);
		addState(testState);
	}
}
