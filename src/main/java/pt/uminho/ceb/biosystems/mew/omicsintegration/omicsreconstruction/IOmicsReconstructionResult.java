package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;

public interface IOmicsReconstructionResult {

	public Container getSpecificModel();
	
	public IOmicsConfiguration getConfiguration();
	
}
