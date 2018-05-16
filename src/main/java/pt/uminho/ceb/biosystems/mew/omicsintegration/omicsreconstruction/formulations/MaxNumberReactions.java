package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.SolverException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;

/**
 * @author Sara Correia Maximize the number of reaction, from a set of
 *         reactions, with positive flux rate. Implementation of LP formulation
 *         (fastcore LP7)
 */
// FO: Max SUM z_i
// s.a: Sv=0
// v_i>= z_i , i in Reacs
// z_i in [0,e] , e threshold

public class MaxNumberReactions extends AbstractSSBasicSimulation<LPProblem> {
	private Set<String> reacs;
	private double threshold;

	public MaxNumberReactions(ISteadyStateModel model, Set<String> reacs, double threshold ) throws Exception {
		super(model);
		this.reacs = reacs;
		this.threshold = threshold;
	}

	@Override
	public LPProblem constructEmptyProblem() {
		LPProblem newProblem = new LPProblem();
		return newProblem;
	}

	@Override
	protected void createObjectiveFunction() throws PropertyCastException, MandatoryPropertyException {
		problem.setObjectiveFunction(new LPProblemRow(), true);
		for (String r : reacs) {
			objTerms.add(new VarTerm(getIdToIndexVarMapings().get("Z_" + r), 1.0, 0.0));
		}
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "max: Sum Z_i";
	}

	@Override
	protected void createVariables() throws PropertyCastException, MandatoryPropertyException,
			WrongFormulationException, SolverException {
		super.createVariables();
		int i = getCurrentNumOfVar();
		// Z_i variables
		for (String r : reacs) {
			putVarMappings("Z_" + r, i);
			problem.addVariable(new LPVariable("Z_" + r, 0.0, threshold));
			i++;
		}
	}

	@Override
	protected void createConstraints() throws WrongFormulationException, PropertyCastException,
			MandatoryPropertyException {
		super.createConstraints();
		for (String r : reacs) {
			int indexZ = getIdToIndexVarMapings().get("Z_" + r);
			int indexV = getIdToIndexVarMapings().get(r);
			LPProblemRow row = new LPProblemRow();
			try {
				row.addTerm(indexV, 1);
				row.addTerm(indexZ, -1);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException("Cannot add term " + indexV + "to row with value: 1");
			}
			LPConstraint constraint = new LPConstraint(LPConstraintType.GREATER_THAN, row, 0.0);
			problem.addConstraint(constraint);
		}
	}
}
