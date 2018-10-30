package de.felixperko.fractals.client.stateholders;

import java.util.ArrayList;
import java.util.List;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.client.gui.SelectionState;
import de.felixperko.fractals.client.rendering.painter.FailRatioPainter;
import de.felixperko.fractals.client.rendering.painter.Painter;
import de.felixperko.fractals.client.rendering.painter.SamplesPainter;
import de.felixperko.fractals.client.rendering.painter.StandardPainter;
import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.server.calculators.MandelbrotCalculator;
import de.felixperko.fractals.server.calculators.infrastructure.SampleCalculator;
import de.felixperko.fractals.server.state.RangeState;
import de.felixperko.fractals.server.state.StateChangeListener;
import de.felixperko.fractals.server.state.StateHolder;
import de.felixperko.fractals.server.state.StateListener;

public class RendererStateHolder extends StateHolder {
	
	Renderer renderer;
	
	public RangeState stateColorScale;
	public RangeState stateColorShift;
	public SelectionState<Class<? extends Painter>> statePainter;
	
	public RendererStateHolder(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	protected void stateSetup() {
		configurePainter();
		configureColorScale();
		configureColorShift();

		addState(statePainter);
		addState(stateColorScale);
		addState(stateColorShift);
	}

	private void configurePainter() {
		statePainter = new SelectionState<Class<? extends Painter>>("painter", StandardPainter.class) {
			@Override
			public String getName(Class<? extends Painter> obj) {
				return obj.getSimpleName();
			}
			@Override
			public String getValueString() {
				return getValue().getSimpleName();
			}
		};
		statePainter.addStateListener(new StateChangeListener<Class<? extends Painter>>(statePainter){
			@Override
			public void valueChanged(Class<? extends Painter> oldValue, Class<? extends Painter> newValue) {
				try {
					renderer.setPainter(newValue.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});
		List<Class<? extends Painter>> options = new ArrayList<>();
		options.add(StandardPainter.class);
		options.add(SamplesPainter.class);
		options.add(FailRatioPainter.class);
		statePainter.addOptions(options);
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

}
