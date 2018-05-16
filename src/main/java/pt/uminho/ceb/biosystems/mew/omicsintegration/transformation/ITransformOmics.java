package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;

public interface ITransformOmics {

	public IOmicsContainer transform (IOmicsContainer omicsContainer);
}
