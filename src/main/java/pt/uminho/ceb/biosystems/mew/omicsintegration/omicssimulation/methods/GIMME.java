package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TaskException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.CheckTasks;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.MetabolicTask;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.GIMMEConfiguration;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPMapVariableValues;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * Min SUM c_i * |v_i| 
 * SV=0 
 * a_i < v_i < b_i 
 * v_i is one a RMF 
 * c_i = {x_cutoff > expressionReaction ? x_cutoff - expressionReaction : 0
 * 
 * @author Sara
 *
 */

public class GIMME extends AbstractSSBasicSimulation<LPProblem> {

	private int numberReacs;
	private int numberRevReac;
	private List<Integer> indexRevReactions;
	private Double MIN_FLUX = 0.00001; // change to configuration
	private ReactionDataMap fluxScores;
	private Map<String, Pair<Double, Double>> rmfLimits;
	private EnvironmentalConditions envCond; // enviromental condition
												// constraints must be treated
												// in this class because of the
												// conversion of
												// reversiblereaction .NOT use
												// the OverrideModel,

	public GIMME(ISteadyStateModel model, IOmicsConfiguration configuration) throws TaskException{
		this(model,(GIMMEConfiguration) configuration);
	}
	
	public GIMME(ISteadyStateModel model, GIMMEConfiguration configuration) throws TaskException {

		super(model);
		initProps(configuration);
		envCond = configuration.getEnvironmentalConditions();

		if (configuration.getRMFLimits() == null) {
			rmfLimits = runRMFs(configuration);
		} else {
			rmfLimits = new HashMap<String, Pair<Double, Double>>();
			for (Map.Entry<String, Double> entry : configuration.getRMFLimits().getMapValues().entrySet()) {
				rmfLimits.put(entry.getKey(), new Pair<Double, Double>(
						entry.getValue() * configuration.getRMFPercentage(), entry.getValue()));
			}
		}
		this.numberReacs = this.model.getNumberOfReactions();
		this.indexRevReactions = new ArrayList<Integer>();
		MIN_FLUX = configuration.getFluxThreshold();

		processScores(configuration);
		initProps(configuration);
	}

	private void initProps(GIMMEConfiguration configuration) {
		setProperty(SimulationProperties.IS_MAXIMIZATION, false);
		setProperty(SimulationProperties.SOLVER, configuration.getSolverType());

	}

	private void processScores(GIMMEConfiguration configuration) {
		fluxScores = new ReactionDataMap();
		for (Map.Entry<String, Double> entry : configuration.getReactionScores().getMapValues().entrySet()) {
			double val = 0.0;

			if (!entry.getValue().isNaN() && entry.getValue() < configuration.getCutOff()) {
				val = configuration.getCutOff() - entry.getValue();
			}
			fluxScores.getMapValues().put(entry.getKey(), val);

		}
	}

	private Map<String, Pair<Double, Double>> runRMFs(GIMMEConfiguration configuration) throws TaskException {
		Map<String, Pair<Double, Double>> rfm = new HashMap<String, Pair<Double, Double>>();

		CheckTasks checkTasks = new CheckTasks(configuration.getTemplateContainer(), configuration.getTasks(), false, // SGC: colocar
																														// o
																														// close
																														// drains
																														// como
																														// parametro
																														// de
																														// configuração
				configuration.getFluxThreshold(), configuration.getEnvironmentalConditions(), configuration.getSolverType());

		Map<String, SteadyStateSimulationResult> tasksSimulRes = checkTasks.getSimulationTasks();
		for (MetabolicTask task : configuration.getTasks()) {
			String objReac = task.getObjectiveReaction();
			double val = tasksSimulRes.get(task.getId()).getOFvalue();
			Pair<Double, Double> p = new Pair<Double, Double>(val * configuration.getRMFPercentage(), val);
			System.out.println("biomass :" + p.getA());
			rfm.put(objReac, p);
		}
		return rfm;
	}

	@Override
	public LPProblem constructEmptyProblem() {
		return new LPProblem();
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
			String reacId = this.indexToIdVarMapings.get(i);
			if (reacId.endsWith("_Rev"))
				reacId = reacId.substring(0, reacId.length() - 4);
			if (fluxScores.getMapValues().containsKey(reacId) && !fluxScores.getMapValues().get(reacId).isNaN()) {
				w = fluxScores.getMapValues().get(reacId);
				// if (w != 0.0)
				obj_coef.put(i, w);
			}
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

			if (rmfLimits.containsKey(r.getId())) {
				if (rmfLimits.get(r.getId()).getA() > 0) {
					bounds[0] = rmfLimits.get(r.getId()).getA();
					// bounds[1] = rmfLimits.get(r.getId()).getB();
				} else if (rmfLimits.get(r.getId()).getA() < 0) {
					bounds[2] = Math.abs(rmfLimits.get(r.getId()).getA());
					// bounds[3] = Math.abs(rmfLimits.get(r.getId()).getB());
				}
			}

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
	public String getObjectiveFunctionToString() {
		return "Min Sum c_i * |v_i|";

	}

	@Override
	public FluxValueMap getFluxValueListFromLPSolution(LPSolution solution) {

		LPMapVariableValues varValueList = null;
		if (solution != null)
			varValueList = solution.getValues();

		FluxValueMap fluxValues = new FluxValueMap();

		for (int i = 0; i < numberReacs + numberRevReac; i++) {

			double value = Double.NaN;
			if (varValueList != null)
				value = varValueList.get(i);

			if (solution != null && !(solution.getSolutionType().equals(LPSolutionType.OPTIMAL)
					|| solution.getSolutionType().equals(LPSolutionType.FEASIBLE)
					|| solution.getSolutionType().equals(LPSolutionType.UNKNOWN))) {
				value = Double.NaN;
			}

			String rId = indexToIdVarMapings.get(i);
			if (rId.endsWith("_Rev")) {
				String nr = rId.substring(0, rId.length() - 4);
				double value2 = varValueList.get(i - 1);
				// System.out.println(nr+":" + value2 +" - "+ value);

				if ((value > MIN_FLUX && value2 <= MIN_FLUX)
						|| (value > value2 && value >= MIN_FLUX && value2 >= MIN_FLUX)) {
					fluxValues.put(nr, -value);
				}
			} else if (!fluxValues.containsKey(rId)) {
				fluxValues.put(rId, value);
			}

		}

		return fluxValues;
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
