package de.felixperko.fractals.client.stateholders;

import de.felixperko.fractals.client.rendering.renderer.Renderer;
import de.felixperko.fractals.server.state.RangeState;
import de.felixperko.fractals.server.state.StateHolder;
import de.felixperko.fractals.server.state.StateListener;

public class RendererStateHolder extends StateHolder {
	
	Renderer renderer;
	
	public RangeState stateColorScale;
	public RangeState stateColorShift;
	
	public RendererStateHolder(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	protected void stateSetup() {
		configureColorScale();
		configureColorShift();

		addState(stateColorScale);
		addState(stateColorShift);
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
