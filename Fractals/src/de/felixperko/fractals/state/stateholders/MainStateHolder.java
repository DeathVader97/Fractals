package de.felixperko.fractals.state.stateholders;

import de.felixperko.fractals.FractalsMain;
import de.felixperko.fractals.state.DiscreteState;
import de.felixperko.fractals.state.State;
import de.felixperko.fractals.state.StateHolder;
import de.felixperko.fractals.state.StateListener;
import de.felixperko.fractals.state.SwitchState;
import de.felixperko.fractals.util.Position;

public class MainStateHolder extends StateHolder {
	
	DiscreteState<Integer> stateThreadCount;
	State<Position> statecursorPosition;
	State<Position> stateCursorImagePosition;
	SwitchState stateFullscreen;
	DiscreteState<Integer> stateVisualizationSteps;
	
	public MainStateHolder(FractalsMain main) {
		super();
	}
	
	@Override
	protected void stateSetup() {
		
		configureThreadCount();
		configureFullscreen();
		configureCursorPosition();
		configureCursorImagePosition();
		configureVisualizationSteps();
		
		addState(stateThreadCount);
		addState(stateFullscreen);
		addState(statecursorPosition);
		addState(stateCursorImagePosition);
		addState(stateVisualizationSteps);
	}

	private void configureVisualizationSteps() {
		stateVisualizationSteps = new DiscreteState<Integer>("visulization steps", 0) {
			@Override
			public Integer getNext() {
				if (getValue() < 10)
					return getValue()+1;
				else if (getValue() < 100)
					return getValue()+10;
				else
					return getValue()+100;
			}
			@Override
			public Integer getPrevious() {
				if (getValue() <= 0)
					return null;
				else if (getValue() <= 10)
					return getValue()-1;
				else if (getValue() <= 100)
					return getValue()-10;
				else
					return getValue()-100;
			}
		};
		stateVisualizationSteps.setIncrementable(true).setDecrementable(true);
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
}
