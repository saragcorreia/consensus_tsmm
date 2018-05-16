package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.GenericOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.OmicsMethods;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;


public class EFluxConfiguration extends GenericOmicsConfiguration{
	
	private static final long serialVersionUID = 1L;
	public static String VAR_OMICS = "expressionData";
	
	
	public EFluxConfiguration(Container container,SolverType solver){
		super(container, solver);	
		setProperty(SimulationProperties.METHOD, OmicsMethods.EFLUX);
	}

	public ReactionDataMap getReactionScores(){
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS);
	}
	
	public void setReactionScores(ReactionDataMap scores){
		Map<String, IOmicsDataMap> omics = new HashMap<String, IOmicsDataMap>();
		omics.put(VAR_OMICS, scores);
		setOmicsData(omics);
	}
}
