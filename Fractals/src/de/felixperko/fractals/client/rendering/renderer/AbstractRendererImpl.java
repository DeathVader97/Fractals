package de.felixperko.fractals.client.rendering.renderer;

import de.felixperko.fractals.server.data.DataDescriptor;
import de.felixperko.fractals.server.stateholders.JobStateHolder;

public abstract class AbstractRendererImpl implements Renderer {
	
	DataDescriptor dataDescriptor;
	JobStateHolder rendererStateHolder = new JobStateHolder(this);
	
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

	public JobStateHolder getRendererStateHolder() {
		return rendererStateHolder;
	}

	public void setRendererStateHolder(JobStateHolder rendererStateHolder) {
		this.rendererStateHolder = rendererStateHolder;
	}

}
