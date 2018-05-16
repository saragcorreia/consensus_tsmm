package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

/**
 * @author Sara Correia minimize  the number of reaction from penalty set, when the reacsK must have flux.
 *  Implementation of LP formulation
 *         (fastcore LP10)
 */
// FO: Min SUM Z_i
// s.a: Sv=0
// v_i>= e , i in reacsK
// vi in [-zi,zi] , i in reacsPenalty

public class MinimizesFluxPenaltySet extends AbstractSSBasicSimulation<LPProblem> {
	private Set<String> reacsK;
	private Set<String> reacsPenalty;
	private double threshold;

	public MinimizesFluxPenaltySet(ISteadyStateModel model, Set<String> reacsK, Set<String> reacsPenalty, double threshold)
			throws Exception {
		super(model);
		this.reacsK = reacsK;
		this.reacsPenalty = reacsPenalty;
		this.threshold = threshold;
	}

	@Override
	public LPProblem constructEmptyProblem() {
		LPProblem newProblem = new LPProblem();
		return newProblem;
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), false);
		for (String r : reacsPenalty) {
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get("Z_" + r), 1.0, 0.0));
		}
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "Min: sum Z_i";
	}

	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException,
			WrongFormulationException, SolverException {
		super.createVariables();
		int i = getCurrentNumOfVar();
		// Z_i variables
		for (String r : reacsPenalty) {
			putVarMappings("Z_" + r, i);
			problem.addVariable(new LPVariable("Z_" + r, Config.LOWER_BOUND, Config.UPPER_BOUND));
			i++;
		}
	}

	@Override
	protected void createConstraints() throws WrongFormulationException, PropertyCastException,
			MandatoryPropertyException {
		super.createConstraints();
		// v_i in [-z_i, z_i]
		for (String r : reacsPenalty) {
			int indexZ = getIdToIndexVarMapings().get("Z_" + r);
			int indexV = getIdToIndexVarMapings().get(r);
			LPProblemRow row1 = new LPProblemRow();
			LPProblemRow row2 = new LPProblemRow();
			try {
				row1.addTerm(indexV, 1);
				row1.addTerm(indexZ, -1);
				row2.addTerm(indexV, 1);
				row2.addTerm(indexZ, 1);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException("Cannot add term " + indexV + " or " + indexZ
						+ "to row with value: 1");
			}
			LPConstraint constraint1 = new LPConstraint(LPConstraintType.LESS_THAN, row1, 0.0);
			LPConstraint constraint2 = new LPConstraint(LPConstraintType.GREATER_THAN, row2, 0.0);
			problem.addConstraint(constraint1);
			problem.addConstraint(constraint2);
		}
		// vi >= epsilon
		for (String r : reacsK) {
			int indexV = getIdToIndexVarMapings().get(r);
			LPProblemRow row = new LPProblemRow();
			try {
				row.addTerm(indexV, 1);

			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException("Cannot add term " + indexV + "to row with value: 1");
			}
			LPConstraint constraint = new LPConstraint(LPConstraintType.GREATER_THAN, row,
					1000*threshold); //paper:use a scaled version of e (we used 105e) in the second constraint of LP-10
			problem.addConstraint(constraint);
		}
	}

}
