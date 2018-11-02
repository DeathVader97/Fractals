package de.felixperko.fractals.client.rendering.renderer;

import com.sun.scenario.effect.impl.Renderer.RendererState;

import de.felixperko.fractals.client.stateholders.RendererStateHolder;
import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.stateholders.JobStateHolder;

public abstract class AbstractRendererImpl implements Renderer {
	
	DataDescriptor dataDescriptor;
	RendererStateHolder rendererStateHolder = new RendererStateHolder(this);
	
	public AbstractRendererImpl() {
	}

	@Override
	public DataDescriptor getDataDescriptor() {
		return dataDescriptor;
	}

	@Override
	public void setDataDescriptor(DataDescriptor dataDescriptor) {
		this.dataDescriptor = dataDescriptor;
	}

	public RendererStateHolder getRendererStateHolder() {
		return rendererStateHolder;
	}

	public void setRendererStateHolder(RendererStateHolder rendererStateHolder) {
		this.rendererStateHolder = rendererStateHolder;
	}

}
