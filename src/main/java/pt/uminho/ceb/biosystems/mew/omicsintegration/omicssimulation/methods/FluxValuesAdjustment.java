package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.core.simulation.mfa.methods.nullspace.MeasuredFlux;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class FluxValuesAdjustment  extends AbstractSSBasicSimulation<LPProblem> {
	private int numberReacs;
	private int numberRevReac;
	private List<Integer> indexRevReactions;
	private EnvironmentalConditions envCond;
	private Map<String, Double>mesuredValues;
	
	
	public FluxValuesAdjustment(ISteadyStateModel model, Map<String, Double>mesuredValues, SolverType solver, EnvironmentalConditions env){
		super(model);
		this.mesuredValues = mesuredValues;
		this.numberReacs = this.model.getNumberOfReactions();
		this.indexRevReactions = new ArrayList<Integer>();
		this.envCond = env;
		
		setProperty(SimulationProperties.IS_MAXIMIZATION, false);
		setProperty(SimulationProperties.SOLVER, solver);
	}
	
	
	@Override
	protected void createObjectiveFunction()
			throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), false);

		Map<Integer, Double> obj_coef = getObjectiveFunction();

		for (Integer y : obj_coef.keySet()) {
			double coef = obj_coef.get(y);
			objTerms.add(new VarTerm(y, coef, 0.0));
		}
	}

	protected Map<Integer, Double> getObjectiveFunction() throws WrongFormulationException {
		Map<Integer, Double> obj_coef = new HashMap<Integer, Double>();
		double w;

		for (int i = 0; i < numberReacs + numberRevReac; i++) {
		
		}
		return obj_coef;
	}

	@Override
	protected void createVariables()
			throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		numberRevReac = 0;
		// variables v_i
		for (int i = 0; i < numberReacs; i++) {
			Reaction r = model.getReaction(i);
			putVarMappings(r.getId(), i + numberRevReac);

			double lb = r.getConstraints().getLowerLimit();
			double ub = r.getConstraints().getUpperLimit();

			// set the environmental condition
			if (envCond!=null && envCond.containsKey(r.getId())) {
				lb = envCond.get(r.getId()).getLowerLimit();
				ub = envCond.get(r.getId()).getUpperLimit();
			}

			double[] bounds = getBounds(lb, ub);
			
			LPVariable var = new LPVariable(r.getId(), bounds[0], bounds[1]);
			problem.addVariable(var);
			// create 2 reactions for reversible reactions or when reaction is
			// x--> format and bounds in opposite way
			if (r.isReversible() && r.getConstraints().getLowerLimit() < 0) {
				indexRevReactions.add(i + numberRevReac);
				putVarMappings(r.getId() + "_Rev", i + numberRevReac + 1);
				LPVariable var2 = new LPVariable(r.getId() + "_Rev", bounds[2], bounds[3]);
				problem.addVariable(var2);
				numberRevReac++;
			}
		}

	}

	@Override
	protected void createConstraints()
			throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		int numberVariables = numberReacs;
		int numberConstraints = model.getNumberOfMetabolites();

		for (int i = 0; i < numberConstraints; i++) {
			LPProblemRow row = new LPProblemRow();
			int nRev = 0;
			for (int j = 0; j < numberVariables; j++) {
				double value = model.getStoichiometricValue(i, j);
				boolean isReversible = indexRevReactions.contains(j + nRev);
				try {
					if (value != 0.0) {
						row.addTerm(j + nRev, value);
						if (isReversible)
							row.addTerm(j + nRev + 1, -value);
					}
					if (isReversible)
						nRev++;
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					e.printStackTrace();
					throw new WrongFormulationException("Cannot add term " + j + "to row with value: " + value);
				}
			}

			LPConstraint constraint = new LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
			problem.addConstraint(constraint);
		}
	}


	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
	}


	@Override
	public String getObjectiveFunctionToString() {
		return "Min Sum abs(v_i - mesured_v_i)";
	}

	
	private double[] getBounds(double lb, double ub) {

		double[] bounds = { 0, 0, 0, 0 };
		if (lb < 0 && ub > 0) {
			bounds[1] = ub;
			bounds[3] = -lb;// ubRev
		} else if (lb > 0 && ub < 0) {
			bounds[1] = lb;
			bounds[3] = -ub;// ubRev
		} else {
			if (lb <= 0 && ub <= 0) {
				bounds[2] = -ub; // lbRev
				bounds[3] = -lb; // ubRev
			} else {
				bounds[0] = lb; // lb
				bounds[1] = ub; // ub
			}
		}

		return bounds;
	}

}
