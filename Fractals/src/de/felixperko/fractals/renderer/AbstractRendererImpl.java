package de.felixperko.fractals.renderer;

import de.felixperko.fractals.data.DataContainer;
import de.felixperko.fractals.data.DataDescriptor;
import de.felixperko.fractals.state.stateholders.RendererStateHolder;

public abstract class AbstractRendererImpl implements Renderer {
	
//	DataContainer dataContainer;
	DataDescriptor dataDescriptor;
	RendererStateHolder rendererStateHolder = new RendererStateHolder();
	
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

//	@Override
//	public DataContainer getDataContainer() {
//		return dataContainer;
//	}
//
//	@Override
//	public void setDataContainer(DataContainer dataContainer) {
//		this.dataContainer = dataContainer;
//	}

	public RendererStateHolder getRendererStateHolder() {
		return rendererStateHolder;
	}

	public void setRendererStateHolder(RendererStateHolder rendererStateHolder) {
		this.rendererStateHolder = rendererStateHolder;
	}

}
