package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.GenericOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.OmicsMethods;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class MBAConfiguration extends GenericOmicsConfiguration {

	private static final long serialVersionUID = 1L;
	public static String VAR_OMICS_HR = "CoreSet";
	public static String VAR_OMICS_MR = "ModerateSet";

	public MBAConfiguration(Container container, SolverType solver) {
		super(container, solver);
		
		optionalPropertyMap.put(SimulationProperties.EPSILON, Double.class);
		
		setProperty(SimulationProperties.METHOD, OmicsMethods.MBA);
	}

	public ReactionDataMap getCoreSet() {
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS_HR);
	}

	public void setCoreSet(ReactionDataMap core) {
		Map<String, IOmicsDataMap> existing = getOmicsData();
		if(existing==null){
			existing = new HashMap<String, IOmicsDataMap>();
		}
		existing.put(VAR_OMICS_HR, core);
		setOmicsData(existing);
	}

	public ReactionDataMap getModerateSet() {
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS_MR);
	}

	public void setModerateSet(ReactionDataMap core) {		
		Map<String, IOmicsDataMap> existing = getOmicsData();
		if(existing==null){
			existing = new HashMap<String, IOmicsDataMap>();
		}
		existing.put(VAR_OMICS_MR, core);
		setOmicsData(existing);
	}
	
	public double getEpsilon(){
		return getDefaultValue(SimulationProperties.EPSILON, 0.5);
	}
	
	public void setEpsilon(double value){
		setProperty(SimulationProperties.EPSILON, value);
	}
	
}
