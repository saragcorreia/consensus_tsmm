package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;

public class SpecificModelResult implements IOmicsReconstructionResult, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected Container specificModel;
	protected IOmicsConfiguration configuration;
	
	public SpecificModelResult(Container specificModel, IOmicsConfiguration configuration){
		this.specificModel = specificModel;
		this.configuration = configuration;
	}
	

	@Override
	public IOmicsConfiguration getConfiguration(){
		return configuration;
	}

	@Override
	public Container getSpecificModel() {
		return specificModel;
	}
		
}
