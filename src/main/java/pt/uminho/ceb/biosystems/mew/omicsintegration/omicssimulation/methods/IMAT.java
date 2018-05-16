package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.IMATConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

public class IMAT extends AbstractSSBasicSimulation<MILPProblem>{

	
	protected Set<String> upRegReactions;
	protected Set<String> downRegReactions;
	protected double epsilon ;

	public IMAT(ISteadyStateModel model, IOmicsConfiguration configuration) {
		this(model, (IMATConfiguration)configuration);
		
	}
	
	public IMAT(ISteadyStateModel model, IMATConfiguration configuration) {
		super(model);

		downRegReactions = configuration.getDownRegulatedReactions().getMapValues().keySet();
		upRegReactions = configuration.getUpRegulatedReactions().getMapValues().keySet();
		epsilon = configuration.getEpsilon();
		initProps(configuration);
	}
	
	
	private void initProps(IMATConfiguration configuration) {	
		properties.put(SimulationProperties.IS_MAXIMIZATION, true);
		properties.put(SimulationProperties.SOLVER, configuration.getSolverType());
		properties.put(SimulationProperties.ENVIRONMENTAL_CONDITIONS, configuration.getEnvironmentalConditions());
	}
		
	@Override
	public MILPProblem constructEmptyProblem() {
		return new MILPProblem();		
	}
	
	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		super.createVariables();		
		int problemVar = problem.getNumberVariables();
		
		for(String reactionId: upRegReactions){
			String booleanVarId = "y_pos_" + reactionId;
			((MILPProblem)problem).addIntVariable(booleanVarId, 0, 1);
			putVarMappings(booleanVarId, problemVar);
			problemVar++;

			booleanVarId = "y_neg_" + reactionId;
			((MILPProblem)problem).addIntVariable(booleanVarId, 0, 1);
			putVarMappings(booleanVarId, problemVar);
			problemVar++;
		}
		
		for(String reactionId: downRegReactions){
			String booleanVarId = "y_"+reactionId;
			((MILPProblem)problem).addIntVariable(booleanVarId, 0, 1);
			putVarMappings(booleanVarId, problemVar);
			problemVar++;
		}
		
	}	
	
	protected void createConstraints() throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		super.createConstraints();
		try {
			for(String reactionId : upRegReactions){
				ReactionConstraint reactionConstraint = model.getReactionConstraint(reactionId);
				int reactionIdx = getIdxVar(reactionId);
				int booleanPositiveVarIdx = getIdxVar("y_pos_"+reactionId);
				int booleanNegativeVarIdx = getIdxVar("y_neg_"+reactionId);
				
				double vMin = reactionConstraint.getLowerLimit();
				double vMax = reactionConstraint.getUpperLimit();
				
				// calculate the v_max/v_min
				if(getEnvironmentalConditions()!=null && getEnvironmentalConditions().containsKey(reactionId)){
					vMin = getEnvironmentalConditions().get(reactionId).getLowerLimit();
					vMax = getEnvironmentalConditions().get(reactionId).getUpperLimit();
				}
				
		
				// yi + yi_pos (vi_min - e) > vi_min
				LPProblemRow row_l = new LPProblemRow();
				row_l.addTerm(reactionIdx, 1);
				row_l.addTerm(booleanPositiveVarIdx, (vMin- epsilon));
				LPConstraint constraint_l = new LPConstraint(LPConstraintType.GREATER_THAN, row_l, vMin);
				problem.addConstraint(constraint_l);

				// yi + yi_neg (vi_max + e) < vi_max
				LPProblemRow row_u = new LPProblemRow();
				row_u.addTerm(reactionIdx, 1);
				row_u.addTerm(booleanNegativeVarIdx, (vMax+epsilon));
				LPConstraint constraint_u = new LPConstraint(LPConstraintType.LESS_THAN, row_u, vMax);
				problem.addConstraint(constraint_u);
			}
			
			for(String reactionId :downRegReactions){
				
				ReactionConstraint reactionConstraint = model.getReactionConstraint(reactionId);
				int reactionIdx = getIdxVar(reactionId);
				int booleanVarIdx = getIdxVar("y_"+reactionId);
				
				
				double vMin = reactionConstraint.getLowerLimit();
				double vMax = reactionConstraint.getUpperLimit();
				
				// calculate the v_max/v_min
				if(getEnvironmentalConditions()!=null && getEnvironmentalConditions().containsKey(reactionId)){
					vMin = getEnvironmentalConditions().get(reactionId).getLowerLimit();
					vMax = getEnvironmentalConditions().get(reactionId).getLowerLimit();
				}
				
				
				//vi + yi * vi_min > vi_min
				LPProblemRow row_l = new LPProblemRow();
				row_l.addTerm(reactionIdx, 1);
				row_l.addTerm(booleanVarIdx, vMin);
				LPConstraint constraint_l = new LPConstraint(LPConstraintType.GREATER_THAN, row_l, vMin);
				problem.addConstraint(constraint_l);
				
				//vi + yi * vi_max < vi_max
				LPProblemRow row_u = new LPProblemRow();
				row_u.addTerm(reactionIdx, 1);
				row_u.addTerm(booleanVarIdx, vMax);
				LPConstraint constraint_u = new LPConstraint(LPConstraintType.LESS_THAN, row_u, vMax);
				problem.addConstraint(constraint_u);
			}
			
		} catch (LinearProgrammingTermAlreadyPresentException e) {
			throw new WrongFormulationException(e);
		}
	}
	
	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), true);
		for(String reactionId : upRegReactions){
			int idx = getIdxVar("y_pos_" + reactionId);
			objTerms.add(new VarTerm(idx));
			
			idx = getIdxVar("y_neg_" + reactionId);
			objTerms.add(new VarTerm(idx));
		}				
		for(String reactionId : downRegReactions){
			int idx = getIdxVar("y_"+reactionId);
			objTerms.add(new VarTerm(idx));
		}
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "IMAT";
	}
	
	@Override
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution) throws PropertyCastException, MandatoryPropertyException {
		SteadyStateSimulationResult result = super.convertLPSolutionToSimulationSolution(solution);
		result.addComplementaryInfoReactions("IMAT", getBooleanVariable(solution));
		return result;
	}

	public MapStringNum getBooleanVariable(LPSolution solution){
		MapStringNum ret = new MapStringNum();
		
		// if yi_pos ou yi_neg = 1 ... the reaction is active
		for(String reactionId : upRegReactions){
			String varProblemName = "y_pos_" + reactionId;
			int varIdx = getIdxVar(varProblemName);
			double value = solution.getValues().get(varIdx);
//			ret.put(reactionId, value);
			
			varProblemName = "y_neg_" + reactionId;
			varIdx = getIdxVar(varProblemName);
			value = value + solution.getValues().get(varIdx);
			
			
			
			ret.put(reactionId, value);
		}
		
		// y== 0 => reaction active y==1 => reaction with flux 0
		for(String reactionId : downRegReactions){
			String varProblemName = "y_"+reactionId;
			int varIdx = getIdxVar(varProblemName);
			double value = Math.abs(solution.getValues().get(varIdx)-1);
			ret.put(reactionId, value);
		}
		
		return ret;
	}
}