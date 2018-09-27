package de.felixperko.fractals.server.stateholders;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.state.DiscreteState;
import de.felixperko.fractals.server.state.StateHolder;
import de.felixperko.fractals.server.state.StateListener;

public class MainStateHolder extends StateHolder {
	
	public DiscreteState<Integer> stateThreadCount;
	
	@Override
	protected void stateSetup() {
		
		configureThreadCount();
		
		addState(stateThreadCount);
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
}
