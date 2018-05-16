package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.EFluxConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class EFlux extends FBA{

	protected ReactionDataMap scores;
	protected EnvironmentalConditions env ;
	
	public EFlux(ISteadyStateModel model, IOmicsConfiguration configuration) {
		this(model,(EFluxConfiguration) configuration);
	}
	public EFlux(ISteadyStateModel model, EFluxConfiguration configuration) {
		super(model);
		this.scores = configuration.getReactionScores();
		this.env = configuration.getEnvironmentalConditions();
		initProps(configuration);
	}
		
	private void initProps(EFluxConfiguration configuration){
		setProperty(SimulationProperties.IS_MAXIMIZATION, true);
		setProperty(SimulationProperties.SOLVER, configuration.getSolverType());
//		setProperty(SimulationProperties.ENVIRONMENTAL_CONDITIONS, configuration.getEnvironmentalConditions());
	}
	
	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		Map<String, Pair<Double,Double>> limits = getReactionLimits();
		
		int numberVariables = model.getNumberOfReactions();
		for (int i = 0; i < numberVariables; i++) {
			Reaction r = model.getReaction(i);
			putVarMappings(r.getId(), i);
			
			LPVariable var = null;
			if(limits.containsKey(r.getId())){
				var = new LPVariable(r.getId(), limits.get(r.getId()).getA(), limits.get(r.getId()).getB());
			}
			else{
				var = new LPVariable(r.getId(), model.getReactionConstraint(i).getLowerLimit(), model.getReactionConstraint(i).getUpperLimit());
			}
			problem.addVariable(var);
		}	
	}
	
	/**
	 *  The new limits for reaction r is calculated as new_limit = original_limit * valExpression
	 *  The valExpression are normalized to be between 0 and 1.
	 * @return new limits for each reaction
	 */
//	private	Map<String, Pair<Double,Double>> getReactionLimits(){
//		
//		Map<String, Pair<Double,Double>> res = new HashMap<String, Pair<Double, Double>>();
//		
//		double maxValue = 0;
//		Set<String> reacsWIthoutScores = new HashSet<String>();
//		for(Map.Entry<String, Double> d : scores.getMapValues().entrySet()){
//			if(!d.getValue().isNaN())
//			maxValue = Math.max(maxValue,d.getValue());
//			else
//				reacsWIthoutScores.add(d.getKey());
//		}
//		scores.removeValuesByKey(reacsWIthoutScores);
//		
//		for(Map.Entry<String, Double> entry: scores.getMapValues().entrySet()){
//			double lb = model.getReaction(entry.getKey()).getConstraints().getLowerLimit();
//			double ub = model.getReaction(entry.getKey()).getConstraints().getUpperLimit();
////			System.out.println(entry.getKey() + ": " + lb + " ; " + ub );
//			Pair<Double, Double> limits = new Pair<Double, Double>(lb*(entry.getValue()/maxValue), ub*(entry.getValue()/maxValue));
////			System.out.println(entry.getKey() + ": " + limits.getA() + " ; " + limits.getB() );
//			res.put(entry.getKey(), limits);
//		}
//		return res;
//	}
	
	private	Map<String, Pair<Double,Double>> getReactionLimits(){
		
		Map<String, Pair<Double,Double>> res = new HashMap<String, Pair<Double, Double>>();
		
		double maxValue = 0;
		Set<String> reacsWIthoutScores = new HashSet<String>();
		for(Map.Entry<String, Double> d : scores.getMapValues().entrySet()){
			if(!d.getValue().isNaN())
			maxValue = Math.max(maxValue,d.getValue());
			else
				reacsWIthoutScores.add(d.getKey());
		}
		scores.removeValuesByKey(reacsWIthoutScores);
		
		
		for(String r : model.getReactions().keySet()){
			double lb=0.0, ub=0.0;
			
			if(scores.getMapValues().containsKey(r))
				ub = scores.getMapValues().get(r)/maxValue;
			else
				ub=1;
			if(model.getReaction(r).isReversible())
				lb= -ub;
			if(model.getReaction(r).getConstraints().getUpperLimit()<=0.0)
				ub=0.0;
			if(model.getReaction(r).getConstraints().getLowerLimit()>=0.0)
				lb=0.0;	
			Pair<Double, Double> limits = new Pair<Double, Double>(lb, ub);
			res.put(r, limits);
		}
		
		//all uptake reactions must be constrained to a lower bound of -1.
		for(String r: env.keySet()){
			if(env.getReactionConstraint(r).getLowerLimit()<=0.0 && env.getReactionConstraint(r).getUpperLimit()==0.0)
				res.put(r, new Pair<Double, Double>(-1.0, 0.0));
			else if(env.getReactionConstraint(r).getLowerLimit()==0.0 && env.getReactionConstraint(r).getUpperLimit()>=0.0)
				res.put(r, new Pair<Double, Double>(0.0, 1.0));
				
		}
		return res;
	}
	
}