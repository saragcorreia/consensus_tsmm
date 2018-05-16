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


public class IMATConfiguration extends GenericOmicsConfiguration{
	
	private static final long serialVersionUID = 1L;

	public static String VAR_OMICS_UPREGULATED = "UpRegulatedReactions";
	public static String VAR_OMICS_DOWNREGULATED = "DownRegulatedReactions";
	
	public IMATConfiguration(Container container,SolverType solver){
		super(container, solver);
		optionalPropertyMap.put(SimulationProperties.EPSILON, Double.class);
		
		setProperty(SimulationProperties.METHOD, OmicsMethods.IMAT);
	}
	
	public ReactionDataMap getUpRegulatedReactions(){
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS_UPREGULATED);
	}
	
	public ReactionDataMap getDownRegulatedReactions(){
		return (ReactionDataMap) getOmicsData().get(VAR_OMICS_DOWNREGULATED);
	}

	
	public void setUpRegulatedReactions(ReactionDataMap upReacs){
		Map<String, IOmicsDataMap> map= getOmicsData();
		if(map==null){
			map = new HashMap<String, IOmicsDataMap>();
		}
		map.put(VAR_OMICS_UPREGULATED, upReacs);
		setOmicsData(map);
	}
	
	public void setDownRegulatedReactions(ReactionDataMap downReacs){
		Map<String, IOmicsDataMap> map= getOmicsData();
		if(map==null){
			map = new HashMap<String, IOmicsDataMap>();
		}
		map.put(VAR_OMICS_DOWNREGULATED, downReacs);
		setOmicsData(map);
	}
	
	
	public void setUpDownRegulatedReactions(ReactionDataMap reactionScores, double lb, double ub){
		ReactionDataMap upRegulatedReacs = new ReactionDataMap();
		ReactionDataMap downRegulatedReacs = new ReactionDataMap();
		upRegulatedReacs.setCondition(reactionScores.getCondition());
		downRegulatedReacs.setCondition(reactionScores.getCondition());

		for(Map.Entry<String, Double> entry : reactionScores.getMapValues().entrySet()){
			if(entry.getValue()>=ub){
				upRegulatedReacs.setValue(entry.getKey(),1.0);
			}else if(entry.getValue()<= lb){
				downRegulatedReacs.setValue(entry.getKey(),-1.0);				
			}
		}
		setDownRegulatedReactions(downRegulatedReacs);
		setUpRegulatedReactions(upRegulatedReacs);
	}
	
	public void setEpsilon(double value){
	setProperty(SimulationProperties.EPSILON, value);	
	}
	
	public double getEpsilon(){
		return getDefaultValue(SimulationProperties.EPSILON, 1.0);
	}
}
