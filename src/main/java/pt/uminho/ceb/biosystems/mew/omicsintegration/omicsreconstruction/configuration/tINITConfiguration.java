package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.GenericOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.MetaboliteDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.MetabolicTask;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.OmicsMethods;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class tINITConfiguration extends GenericOmicsConfiguration {

	private static final long serialVersionUID = 1L;
	public static String VAR_OMICS_REACS = "ReactionScores";
	public static String VAR_OMICS_METAS = "PresentMetabolites";

	public tINITConfiguration(Container container, SolverType solver) {
		super(container, solver);

		optionalPropertyMap.put(SimulationProperties.DEFAULT_REACTION_WEIGHT,Double.class);
		optionalPropertyMap.put(SimulationProperties.METABOLITE_WEIGHT,Double.class);
		optionalPropertyMap.put(SimulationProperties.TASKS,Set.class);
		
		optionalPropertyMap.put(SimulationProperties.REQUIRED_REACS,Map.class);
		

		setProperty(SimulationProperties.METHOD, OmicsMethods.tINIT);

	}

	public ReactionDataMap getReacScores() {
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS_REACS);
	}

	public void setReacScores(ReactionDataMap core) {
		Map<String, IOmicsDataMap> omics = getOmicsData();
		if (omics == null)
			omics = new HashMap<String, IOmicsDataMap>();
		omics.put(VAR_OMICS_REACS, core);
		setOmicsData(omics);
	}

	public MetaboliteDataMap getPresentMetabolites() {
		return (MetaboliteDataMap) getOmicsData().get(VAR_OMICS_METAS);
	}

	public void setPresentMetabolites(MetaboliteDataMap presentMetas) {
		Map<String, IOmicsDataMap> omics = getOmicsData();
		if (omics == null)
			omics = new HashMap<String, IOmicsDataMap>();
		omics.put(VAR_OMICS_METAS, presentMetas);
		setOmicsData(omics);
	}

	public double getDefaultReactionWeight() {
		return (Double) getDefaultValue(SimulationProperties.DEFAULT_REACTION_WEIGHT, -2.0);
	}

	public void setDefaultReactionWeight(double value) {
		setProperty(SimulationProperties.DEFAULT_REACTION_WEIGHT, value);
	}

	public double getMetaWeight() {
		return (Double) getDefaultValue(SimulationProperties.METABOLITE_WEIGHT, 0.0);
	}

	public void setMetaWeight(double value) {
		setProperty(SimulationProperties.METABOLITE_WEIGHT, value);
	}

	public Set<MetabolicTask> geMetabolicTasks() {
		return (Set<MetabolicTask>) getProperty(SimulationProperties.TASKS);
	}

	public void setTasks(Set<MetabolicTask> tasks) {
		setProperty(SimulationProperties.TASKS, tasks);
	}
	
	
	// used in tINIT formulation class
	
	public Map<String, Set<String>> getRequiredReactions() {
		return (Map<String, Set<String>>) getProperty(SimulationProperties.REQUIRED_REACS);
	}

	public void setRequiredReactions(Map<String, Set<String>> reacs) {
		setProperty(SimulationProperties.REQUIRED_REACS, reacs);
	}
}
