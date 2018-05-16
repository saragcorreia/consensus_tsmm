package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.GenericOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.MetabolicTask;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.OmicsMethods;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;


public class GIMMEConfiguration extends GenericOmicsConfiguration{
	
	private static final long serialVersionUID = 1L;
	public static String VAR_OMICS = "expressionData";

	public GIMMEConfiguration(Container container, SolverType solver){
		super(container, solver);
		optionalPropertyMap.put(SimulationProperties.RMF_LIMITS, ReactionDataMap.class);
		optionalPropertyMap.put(SimulationProperties.RMF_PERCENTAGE, Double.class);
		optionalPropertyMap.put(SimulationProperties.TASKS,Set.class);
		mandatoryPropertyMap.put(SimulationProperties.CUTOFF, Double.class);
		
		setProperty(SimulationProperties.METHOD, OmicsMethods.GIMME);
	}
	
	
	public ReactionDataMap getReactionScores(){
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS);
	}
	
	public void setReactionScores(ReactionDataMap scores){
		Map<String, IOmicsDataMap> omics = new HashMap<String, IOmicsDataMap>();
		omics.put(VAR_OMICS, scores);
		setOmicsData(omics);
	}
	
	public ReactionDataMap getRMFLimits() {
		return (ReactionDataMap) getProperty(SimulationProperties.RMF_LIMITS);
	}

	public void setRMFLimits(ReactionDataMap limits) {
		setProperty(SimulationProperties.RMF_LIMITS, limits);
	}
	
	public double getRMFPercentage(){
		return (Double) getDefaultValue(SimulationProperties.RMF_PERCENTAGE, 0.5);  
	}
	
	public void setRMFPercentage(double value){
		setProperty(SimulationProperties.RMF_PERCENTAGE,value);
	}
	public Set<MetabolicTask> getTasks() {
		return (Set<MetabolicTask>) getProperty(SimulationProperties.TASKS);
	}

	public void setTasks(Set<MetabolicTask> tasks) {
		setProperty(SimulationProperties.TASKS, tasks);
	}
	
//	public double getDefaultReactionWeight() {
//		return (Double) getDefaultValue(SimulationProperties.DEFAULT_REACTION_WEIGHT, 0.0);
//	}
//	
//	public void setDefaultReactionWeight(double value) {
//		setProperty(SimulationProperties.DEFAULT_REACTION_WEIGHT, value);
//	}	
	
	public double getCutOff(){
		return (Double)getProperty(SimulationProperties.CUTOFF);
	}
	
	public void setCutOff(double value){
		setProperty(SimulationProperties.CUTOFF, value);
	}

}
