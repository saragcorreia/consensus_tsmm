package pt.uminho.ceb.biosystems.mew.omicsintegration.configuration;


import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.IGenericConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.OmicsMethods;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public interface IOmicsConfiguration extends IGenericConfiguration{
	
	public SolverType getSolverType();
	
	public Container getTemplateContainer();
	
	public OmicsMethods getMethod();
	
	public Map<String, IOmicsDataMap> getOmicsData();
}
