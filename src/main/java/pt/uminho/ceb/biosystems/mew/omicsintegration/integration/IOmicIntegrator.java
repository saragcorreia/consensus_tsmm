package pt.uminho.ceb.biosystems.mew.omicsintegration.integration;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.OmicDataConversionException;

public interface IOmicIntegrator<P extends IOmicsContainer,T extends IOmicsDataMap> {
	
	public T convert(P omics) throws OmicDataConversionException;
}
