package pt.uminho.ceb.biosystems.mew.omicsintegration.configuration;


import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.strainoptimization.configuration.GenericConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.OmicsMethods;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class GenericOmicsConfiguration extends GenericConfiguration implements IOmicsConfiguration{
	private static final long serialVersionUID = 1L;

	public GenericOmicsConfiguration(Container container, SolverType solver){
		mandatoryPropertyMap.put(SimulationProperties.SOLVER, SolverType.class);
		mandatoryPropertyMap.put(SimulationProperties.TEMPLATE_CONTAINER, Container.class);
		mandatoryPropertyMap.put(SimulationProperties.METHOD, OmicsMethods.class);
		mandatoryPropertyMap.put(SimulationProperties.OMICS, Map.class);
		
		optionalPropertyMap.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, EnvironmentalConditions.class);
		optionalPropertyMap.put(SimulationProperties.FLUX_THRESHOLD, Double.class);
		
		setProperty(SimulationProperties.TEMPLATE_CONTAINER,container);
		setProperty(SimulationProperties.SOLVER, solver);	
	}
 
	public SolverType getSolverType(){
		return (SolverType)getProperty(SimulationProperties.SOLVER);
	}

	public Container getTemplateContainer(){
		return (Container)getProperty(SimulationProperties.TEMPLATE_CONTAINER);
	}
	
	public OmicsMethods getMethod() {
		return (OmicsMethods)getProperty(SimulationProperties.METHOD);
	}

	public Map<String, IOmicsDataMap> getOmicsData() {
		return (Map<String, IOmicsDataMap>) getProperty(SimulationProperties.OMICS);
	}
	
	public void setOmicsData(Map<String, IOmicsDataMap> omics) {
		setProperty(SimulationProperties.OMICS, omics);
	}

	public EnvironmentalConditions getEnvironmentalConditions(){
		return(EnvironmentalConditions) propertyMap.get(SimulationProperties.ENVIRONMENTAL_CONDITIONS);
	}
	
	public void setEnvironmentalConditions(EnvironmentalConditions env){
		this.setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, env);
	}
	
	
	public double getFluxThreshold(){
		return getDefaultValue(SimulationProperties.FLUX_THRESHOLD, 0.0001);
	}
	
	public void setFluxThreshold(double value){
		setProperty(SimulationProperties.FLUX_THRESHOLD, value);
	}
	
}
