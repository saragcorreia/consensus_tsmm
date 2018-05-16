package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration;

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

public class mCADREConfiguration extends GenericOmicsConfiguration{
	
	private static final long serialVersionUID = 1L;
	public static String VAR_OMICS = "CoreSet";
	
	
	public mCADREConfiguration(Container container, SolverType solver){
		super(container, solver);
		optionalPropertyMap.put(SimulationProperties.CUTOFF, Double.class);
		optionalPropertyMap.put(SimulationProperties.RACIO, Double.class);
		optionalPropertyMap.put(SimulationProperties.NOT_EXP_GENE, Double.class);
		optionalPropertyMap.put(SimulationProperties.TASKS, Set.class);
		optionalPropertyMap.put(SimulationProperties.IGNORE_META_CONNECTIVITY, Set.class);
		optionalPropertyMap.put(SimulationProperties.CONFIDENCE_LEVEL, ReactionDataMap.class);
		
		setProperty(SimulationProperties.METHOD, OmicsMethods.mCADRE);
	}
	
	public ReactionDataMap getReactionScores(){
		return (ReactionDataMap)getOmicsData().get(VAR_OMICS);
	}
	
	public void setReactionScores(ReactionDataMap core){
		Map<String, IOmicsDataMap> omics = new HashMap<String, IOmicsDataMap>();
		omics.put(VAR_OMICS, core);
		setProperty(SimulationProperties.OMICS, omics);
	}

	
	public double getCutOff(){
		return getDefaultValue(SimulationProperties.CUTOFF, 0.5);
	}
	
	public double getRacio(){
		return getDefaultValue(SimulationProperties.RACIO, 1/3);
	}

	public double getNotExpressedGeneValue(){
		return getDefaultValue(SimulationProperties.NOT_EXP_GENE, -0.01);
	}
	
	public Set<MetabolicTask> getMetabolicTasks(){
		return (Set<MetabolicTask>) getProperty(SimulationProperties.TASKS); 
	}
	
	public Set<String> getIgnoreMeta(){
		return (Set<String>) getProperty(SimulationProperties.IGNORE_META_CONNECTIVITY);
	}
	
	public ReactionDataMap getConfidenceLevel(){
		return (ReactionDataMap) getProperty(SimulationProperties.CONFIDENCE_LEVEL);
	}
	
	public void setConfidenceLevel(ReactionDataMap confLevel){
		setProperty(SimulationProperties.CONFIDENCE_LEVEL,confLevel);
	}
	
	public void setIgnoreMeta(Set<String> metas){
		setProperty(SimulationProperties.IGNORE_META_CONNECTIVITY, metas);
		
	}
	public void setMetabolicTasks(Set<MetabolicTask> tasks){
		setProperty(SimulationProperties.TASKS, tasks);
	}

	public void setCutOff(double value){
		setProperty(SimulationProperties.CUTOFF, value);
	}
	
	public void setRacio(double value){
		setProperty(SimulationProperties.RACIO, value);
	}
	
	public void setNotExpressedGeneValue(double value){
		setProperty(SimulationProperties.NOT_EXP_GENE, value);
	}
}
