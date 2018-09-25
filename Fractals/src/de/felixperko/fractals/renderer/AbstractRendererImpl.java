package de.felixperko.fractals.renderer;

import de.felixperko.fractals.data.DataContainer;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.stateholders.RendererStateHolder;

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
