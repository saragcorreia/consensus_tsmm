package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.GeneticConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.ReactionChangesList;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.ManagerExceptionUtils;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

public class MinMaxFormulation extends AbstractSSBasicSimulation<LPProblem> {

	public MinMaxFormulation(ISteadyStateModel model) throws Exception {
		super(model);
		optionalProperties.add(SimulationProperties.MINMAX_BOUNDS);
	}

	// Max flux for the reaction
	public void createOF_Flux(String reac, boolean isMax) throws LinearProgrammingTermAlreadyPresentException {
		Map<String, Double> terms = new HashMap<String, Double>();
		terms.put(reac, 1.0);
		properties.put(SimulationProperties.IS_MAXIMIZATION, isMax);
		properties.put(SimulationProperties.OBJECTIVE_FUNCTION, terms);
	}

	public void createOF_Flux() throws LinearProgrammingTermAlreadyPresentException {
		properties.put(SimulationProperties.OBJECTIVE_FUNCTION, null);
		properties.put(SimulationProperties.IS_MAXIMIZATION, true);
	}

	public void createOF_FluxSet(Set<String> reacSet, boolean isMax) throws Exception {
		Map<String, Double> terms = new HashMap<String, Double>();
		for (String reac : reacSet)
			terms.put(reac, 1.0);
		properties.put(SimulationProperties.IS_MAXIMIZATION, isMax);
		properties.put(SimulationProperties.OBJECTIVE_FUNCTION, terms);
	}

	@Override
	public LPProblem constructEmptyProblem() {
		LPProblem newProblem = new LPProblem();
		return newProblem;
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), getIsMaximization());

		Map<String, Double> obj_coef = getObjectiveFunction();
		for (String r : obj_coef.keySet()) {
			double coef = obj_coef.get(r);
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get(r), coef, 0.0));
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Double> getObjectiveFunction() {
		Map<String, Double> obj_coef = null;
		try {
			obj_coef = ManagerExceptionUtils.testCast(properties, Map.class, SimulationProperties.OBJECTIVE_FUNCTION,
					false);
		} catch (Exception e) {
		}
		if (obj_coef == null) {
			obj_coef = new HashMap<String, Double>();
			obj_coef.put("CONSTANT", 1.0);
			properties.put(SimulationProperties.OBJECTIVE_FUNCTION, obj_coef);
		}
		return obj_coef;
	}

	@Override
	public String getObjectiveFunctionToString() {
		String ret = "";
		boolean max = true;
		try {
			max = getIsMaximization();
		} catch (PropertyCastException e) {
			e.printStackTrace();
		} catch (MandatoryPropertyException e) {
			e.printStackTrace();
		}

		if (max)
			ret = "max:";
		else
			ret = "min:";
		Map<String, Double> obj_coef = getObjectiveFunction();
		for (String id : obj_coef.keySet()) {
			double v = obj_coef.get(id);
			if (v != 1)
				ret += " " + v;
			ret += " " + id;
		}

		return ret;
	}

	@Override
	protected void createVariables()
			throws PropertyCastException, MandatoryPropertyException, WrongFormulationException, SolverException {
		super.createVariables();
		putVarMappings("CONSTANT", getCurrentNumOfVar());
		problem.addVariable(new LPVariable("CONSTANT", 1, 1));
	}

	@Override
	public EnvironmentalConditions getEnvironmentalConditions()
			throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions ec = new EnvironmentalConditions();
		ec.addAllReactionConstraints(ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class,
				SimulationProperties.ENVIRONMENTAL_CONDITIONS, true));
		ec.addAllReactionConstraints(ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class,
				SimulationProperties.MINMAX_BOUNDS, true));
		return ec;
	}

	// instead of Max / Min the flux .. put a constraint to force the flux be !=
	// 0.0
	public boolean hasFlux(String reac, double threshold) {
		boolean hasFlux = false;
		SteadyStateSimulationResult result;
			double[] bounds = getBoundsToProcess(model.getReaction(reac), threshold);
			double maxVal = 0.0, minVal = 0.0;
			// the reaction have --> direction
			if (bounds[1] != 0.0) {
				setBounds(reac, bounds[0], bounds[1]);
				setIsMaximization(true);
				try{
					result = simulate();
					if (result.getSolutionType().equals(LPSolutionType.FEASIBLE)
							|| result.getSolutionType().equals(LPSolutionType.OPTIMAL))
						maxVal = result.getFluxValues().getValue(reac);
				} catch (Exception e) {
				System.out.println("Infeasible Problem!");
			}
				removeBounds(reac);	
			}
			if (bounds[2] != 0.0 && maxVal < threshold) {
				setBounds(reac, bounds[2], bounds[3]);
				setIsMaximization(false);
				try{
				result = simulate();
				removeBounds(reac);
				if (result.getSolutionType().equals(LPSolutionType.FEASIBLE)
						|| result.getSolutionType().equals(LPSolutionType.OPTIMAL))
					minVal = result.getFluxValues().getValue(reac);
				} catch (Exception e) {
					System.out.println("Infeasible Problem!");
				}
				removeBounds(reac);	
			}
			if (maxVal >= threshold || minVal <= -threshold)
				hasFlux = true;
		return hasFlux;
	}

	public void setKnockout(String knockout) {
		ReactionChangesList rcl = new ReactionChangesList();
		rcl.addReaction(knockout, 0.0);
		GeneticConditions geneticConditions = new GeneticConditions(rcl);
		this.setGeneticConditions(geneticConditions);
	}

	public void setObjectiveFunction(Map<String, Double> terms)
			throws PropertyCastException, MandatoryPropertyException {
		properties.put(SimulationProperties.OBJECTIVE_FUNCTION, terms);
	}

	private double[] getBoundsToProcess(Reaction reaction, double threshold) {
		double[] result = { 0.0, 0.0, 0.0, 0.0 };
		double lb = reaction.getConstraints().getLowerLimit();
		double ub = reaction.getConstraints().getUpperLimit();

		if (reaction.isReversible()) {
			result[0] = threshold;
			result[1] = ub;
			result[2] = lb;
			result[3] = -threshold;
		} else if (lb >= 0.0) {
			result[0] = threshold;
			result[1] = ub;
		} else if (ub == 0.0) {
			result[2] = lb;
			result[3] = -threshold;
		}
		return result;
	}

	public void setBounds(String reac, double lb, double ub) throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions env = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class,
				SimulationProperties.MINMAX_BOUNDS, true);
		if (env == null)
			env = new EnvironmentalConditions();
		env.addReactionConstraint(reac, new ReactionConstraint(lb, ub));
		properties.put(SimulationProperties.MINMAX_BOUNDS, env);
	}

	public void setBounds(EnvironmentalConditions envCond) throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions env = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class,
				SimulationProperties.MINMAX_BOUNDS, true);
		if (env == null)
			env = new EnvironmentalConditions();
		for (Map.Entry<String, ReactionConstraint> entry : envCond.entrySet())
			env.addReactionConstraint(entry.getKey(), entry.getValue());
		properties.put(SimulationProperties.MINMAX_BOUNDS, env);
	}

	public void removeBounds(String reac) throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions env = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class,
				SimulationProperties.MINMAX_BOUNDS, true);
		if (env != null && env.containsKey(reac))
			env.removeReactionConstraint(reac);
	}

	public void removeBounds(Set<String> reactions) throws PropertyCastException, MandatoryPropertyException {
		EnvironmentalConditions env = ManagerExceptionUtils.testCast(properties, EnvironmentalConditions.class,
				SimulationProperties.MINMAX_BOUNDS, true);
		if (env != null)
			for (String reac : reactions)
				env.removeReactionConstraint(reac);
	}

	public void setIsMaximization(boolean isMaximization) {
		properties.put(SimulationProperties.IS_MAXIMIZATION, isMaximization);
	}

	public boolean getIsMaximization() throws PropertyCastException, MandatoryPropertyException {
		return ManagerExceptionUtils.testCast(properties, Boolean.class, SimulationProperties.IS_MAXIMIZATION, false);
	}
}
