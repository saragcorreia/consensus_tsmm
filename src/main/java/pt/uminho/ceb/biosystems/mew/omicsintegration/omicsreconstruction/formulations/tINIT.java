package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.VarTerm;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.WrongFormulationException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.tINITConfiguration;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraint;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPConstraintType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPMapVariableValues;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblemRow;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolution;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPVariable;
import pt.uminho.ceb.biosystems.mew.solvers.lp.MILPProblem;
import pt.uminho.ceb.biosystems.mew.solvers.lp.exceptions.LinearProgrammingTermAlreadyPresentException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapStringNum;

//NOTA : formulação com SV =0 

public class tINIT extends AbstractSSBasicSimulation<MILPProblem> {
	/**
	 * Formulation FO: Min Sum w_i * y_i - weight Sum b_j
	 * 
	 * 0 < v_i < 1000
	 * 
	 * 0 < b_j < 1
	 * 
	 * 0 < y_i < 1 (binary)
	 * 
	 * Sv - b = 0
	 * 
	 * 1 < v_i + 1000 y_i < 1000
	 * 
	 * v_p_j - v_c_j > 1 (reactions that produce the metabolite j - consume)
	 * 
	 * y_i_+ + y_i_- > 1 (reaction in only one direction) .
	 */
	// Original model and Omic information

	private tINITConfiguration configuration;

	// auxiliar
	private Container container;

	private Set<String> metaPresent;
	private int numberReac, numberMeta, numberRevReac;
	private ReactionDataMap reacWeights;

	private boolean isConstantFO = true;
	// to know the position of constraints related with fakeMetabolites
	private Map<String, Integer> mapFakeMetaConstraint;

	// indexes of reversible reaction in the problem, the direction
	// <- is the next integer
	private List<Integer> indexRevReactions;

	// constructors
	public tINIT(ISteadyStateModel model, tINITConfiguration configuration) {
		super(model);
		this.configuration = configuration;
		initProps();

	}

	private void initProps() {
		numberReac = model.getNumberOfReactions();
		numberMeta = model.getNumberOfMetabolites();
		indexRevReactions = new ArrayList<Integer>();
		reacWeights = (ReactionDataMap) configuration.getReacScores();
		container = configuration.getTemplateContainer();
		if (configuration.getPresentMetabolites() != null)
			metaPresent = configuration.getPresentMetabolites().getMapValues().keySet();
		else
			metaPresent = new HashSet<String>();
		System.out.println("nunber meta present:" + metaPresent.size());
		setSolverType(this.configuration.getSolverType());
		mapFakeMetaConstraint = new HashMap<String, Integer>();
		System.out.println("Reac size" + numberReac);
	}

	// build problem
	@Override
	public MILPProblem constructEmptyProblem() {
		MILPProblem newProblem = new MILPProblem();
		return newProblem;
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
		int initYidx = getinitYindex();
		if (isConstantFO) {
			obj_coef.put(getIdxVar("CONSTANT"), 1.0);

		} else {
			for (int i = 0; i < numberReac + numberRevReac; i++) {
				String reacId = this.indexToIdVarMapings.get(i);
				if (reacId.endsWith("_Rev"))
					reacId = reacId.substring(0, reacId.length() - 4);
				if (reacWeights.getMapValues().containsKey(reacId)
						&& !this.reacWeights.getMapValues().get(reacId).isNaN()) {
					w = this.reacWeights.getMapValues().get(reacId);
				} else {
					w = configuration.getDefaultReactionWeight();
				}
				obj_coef.put(i + initYidx, w);
			}
//			if (configuration.getMetaWeight() != 0.0) {
//				for (int i = 0; i < numberMeta; i++) {
//					int bi = i + numberReac + numberRevReac;
//					obj_coef.put(bi, -1 * configuration.getMetaWeight());
//				}
//			}
		}
		return obj_coef;
	}

	// create all variable of the formulation: v, b, y
	@Override
	protected void createVariables()
			throws PropertyCastException, MandatoryPropertyException, WrongFormulationException {
		numberRevReac = 0;
		// variables v_i
		for (int i = 0; i < numberReac; i++) {
			Reaction r = model.getReaction(i);
			putVarMappings(r.getId(), i + numberRevReac);

			double[] bounds = getBounds(r.getConstraints().getLowerLimit(), r.getConstraints().getUpperLimit());
			LPVariable var = new LPVariable(r.getId(), bounds[0], bounds[1]);
			problem.addVariable(var);
			// create 2 reactions for reversible reactions or when reaction is
			// x--> format and bounds in opposite way
			if (r.isReversible() || r.getConstraints().getLowerLimit() < 0) {
				indexRevReactions.add(i + numberRevReac);
				putVarMappings(r.getId() + "_Rev", i + numberRevReac + 1);
				LPVariable var2 = new LPVariable(r.getId() + "_Rev", bounds[2], bounds[3]);
				problem.addVariable(var2);
				numberRevReac++;
			}
		}
		// b_i
		int initBindex = numberReac + numberRevReac;
		for (int i = 0; i < this.numberMeta; i++) {
			String metaName = model.getMetaboliteId(i);
			putVarMappings("b_" + metaName, i + initBindex);
			LPVariable var = new LPVariable("b_" + metaName, 0, 1);
			problem.addVariable(var);
		}
		// y_i
		int initYindex = numberReac + numberRevReac + numberMeta;
		for (int i = 0; i < numberReac + numberRevReac; i++) {
			putVarMappings("y_" + indexToIdVarMapings.get(i), i + initYindex);
			((MILPProblem) problem).addIntVariable("y_" + indexToIdVarMapings.get(i), 0, 1);

		}
		// CONSTANT
		LPVariable var = new LPVariable("CONSTANT", 1, 1);
		putVarMappings("CONSTANT", getCurrentNumOfVar());
		problem.addVariable(var);
	}

	// create the constrains of the quasi steady-state
	private void createSteadyStateConstrains() throws WrongFormulationException {
		// Sv - b = 0 allows the metabolites accumulation
		for (int i = 0; i < numberMeta; i++) {
			LPProblemRow row = new LPProblemRow();
			int nRev = 0;
			try {
				for (int j = 0; j < numberReac; j++) {
					double value = model.getStoichiometricValue(i, j);
					boolean isReversible = indexRevReactions.contains(j + nRev);
					if (value != 0) {
						row.addTerm(j + nRev, value);
						// insert the row for reversible reaction .. direction
						// <-
						if (isReversible)
							row.addTerm(j + nRev + 1, -value);
					}
					if (isReversible)
						nRev++;
				}
				// add - bi parameter
//				 row.addTerm(numberReac + numberRevReac + i, -1);
				 LPConstraint constraint = new
				 LPConstraint(LPConstraintType.EQUALITY, row, 0.0);
				 problem.addConstraint(constraint);
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				// e.printStackTrace();
				throw new WrongFormulationException("Cannot add term in metabolite " + i);
			}

		}
	}

	// create the rest of the restrictions
	@Override
	protected void createConstraints()
			throws WrongFormulationException, PropertyCastException, MandatoryPropertyException {
		LPProblemRow row;
		LPConstraint constraint;

		int initYIndex = getinitYindex();
		createSteadyStateConstrains();
		// 1 < v_i + 1000 y_i < 1000 Obs: 1000 must be a higher value than UB of
		// the flux
		for (int i = 0; i < numberReac + numberRevReac; i++) {
			row = new LPProblemRow();
			try {
				row.addTerm(i, 1); // v_i
				row.addTerm(initYIndex + i, 999999.0); // y_i
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException(
						"Cannot add term " + i + "to row in contraint 1 < v_i + 1000 y_i < 1000");
			}
			constraint = new LPConstraint(LPConstraintType.GREATER_THAN, row, 1.0);
			problem.addConstraint(constraint);
			constraint = new LPConstraint(LPConstraintType.LESS_THAN, row, 999999.0);
			problem.addConstraint(constraint);
		}
		// v_p_j - v_c_j > 0 --- for each reaction simulate the problem if has
		// solution it will be 1
		for (String metaId : metaPresent) {
			Map<Integer, Double> fakeLine = getFakeMetaStoicLine(metaId);
			row = new LPProblemRow();
			for (Entry<Integer, Double> m : fakeLine.entrySet())
				try {
					row.addTerm(m.getKey(), m.getValue());
				} catch (LinearProgrammingTermAlreadyPresentException e) {
					throw new WrongFormulationException(
							"Cannot add term " + m.getKey() + "to row with value" + m.getValue());
				}
			constraint = new LPConstraint(LPConstraintType.GREATER_THAN, row, 0);
			// save the position of constraint to change after
			mapFakeMetaConstraint.put(metaId, problem.getNumberConstraints());
			problem.addConstraint(constraint);
		}
		// y_i+ + y_i- >1
		for (int ind : indexRevReactions) {
			int idxNeg = ind + initYIndex + 1;
			int idxPos = ind + initYIndex;
			row = new LPProblemRow();
			try {
				row.addTerm(idxNeg, 1); // y_i positive
				row.addTerm(idxPos, 1); // y_i negative
			} catch (LinearProgrammingTermAlreadyPresentException e) {
				throw new WrongFormulationException("Cannot add term " + ind + "to row in contraint y_i+ + y_i- >1");
			}
			constraint = new LPConstraint(LPConstraintType.GREATER_THAN, row, 1);
			problem.addConstraint(constraint);
		}

	}

	@Override
	public void preSimulateActions() {
		try {
			createProblemIfEmpty();
		} catch (WrongFormulationException | MandatoryPropertyException | PropertyCastException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			for (String metaId : metaPresent) {
				insertRequiredMetabolite(metaId);
			}
		
		Map<String, Set<String>> requiredReactions = configuration.getRequiredReactions();
		if (requiredReactions != null) {
			for (Entry<String, Set<String>> task : requiredReactions.entrySet()) {

				try {
					insertRequiredReactions(task.getKey(), task.getValue());
				} catch (LinearProgrammingTermAlreadyPresentException e) {

					e.printStackTrace();
				}
			}
		}

		// create the normal objective function
		setFOConstant(false);
			
	}

	// create the restriction imposed by metabolic tasks .. drains are not
	// required
	public List<LPConstraint> addConstrains(Set<String> requiredReacForTask)
			throws LinearProgrammingTermAlreadyPresentException {
		LPProblemRow row;
		int indexReac;
		LPConstraint constraint;
		int initYIndex = getinitYindex();
		List<LPConstraint> constraints = new ArrayList<LPConstraint>();
		// add constrains to tasks reactions.. reaction that must have flux y_i
		// = 0
		Set<String> drains = container.identifyDrains();
		for (String reac : requiredReacForTask) {
			if (container.getReactions().keySet().contains(reac)
					|| (!drains.contains(reac) && !reac.equals("NOT SATISFY!"))) {
				// if (debug)
				System.out.println("tINITAlgorithm :add constraints to reaction  :" + reac);
				indexReac = idToIndexVarMapings.get(reac);
				row = new LPProblemRow();
				row.addTerm(initYIndex + indexReac, 1);
				// if reversible y_i + y_i- = 1 one reaction must be active
				if (container.getReaction(reac).isReversible()) {
					row.addTerm(initYIndex + indexReac + 1, 1);
					constraint = new LPConstraint(LPConstraintType.EQUALITY, row, 1);
					if (debug)
						System.out.println("tINITAlgorithm :" + constraint.toString());
				} else {
					constraint = new LPConstraint(LPConstraintType.EQUALITY, row, 0);
					if (debug)
						System.out.println("tINITAlgorithm :" + constraint.toString());
				}
				problem.addConstraint(constraint);
				constraints.add(constraint);
			}
		}
		return constraints;
	}

	public void setFOConstant(boolean b) {
		isConstantFO = b;
		_recreateOF = true;
	}

	@Override
	public SteadyStateSimulationResult convertLPSolutionToSimulationSolution(LPSolution solution)
			throws PropertyCastException, MandatoryPropertyException {

		SteadyStateSimulationResult result = super.convertLPSolutionToSimulationSolution(solution);

		result.addComplementaryInfoReactions("tINIT", getBooleanVariables(solution));
		return result;
	}
	
	
	
	@Override()
	public FluxValueMap getFluxValueListFromLPSolution(LPSolution solution) {
		
		LPMapVariableValues varValueList = null;
		if (solution != null) varValueList = solution.getValues();
		
		FluxValueMap fluxValues = new FluxValueMap();
		for (String rId : model.getReactions().keySet()) {
			
			int idx = getReactionVariableIndex(rId);
			
			double value = 0.0;
			if (varValueList != null) value = varValueList.get(idx);
			
			if (solution != null && !(solution.getSolutionType().equals(LPSolutionType.OPTIMAL) || solution.getSolutionType().equals(LPSolutionType.FEASIBLE) || solution.getSolutionType().equals(LPSolutionType.UNKNOWN))){
				value = Double.NaN;
			}
			
			// buscar o valor das reações inversas
			if(value < configuration.getFluxThreshold()  && indexRevReactions.contains(idx)){
				value = varValueList.get(idx+1) * -1.0;
			}
			
			fluxValues.put(rId, value);
		}
		
		if (debug && solution != null && !(solution.getSolutionType().equals(LPSolutionType.OPTIMAL) || solution.getSolutionType().equals(LPSolutionType.FEASIBLE) || solution.getSolutionType().equals(LPSolutionType.UNKNOWN))){
			System.out.println(">>>>>>>>>NAN = "+solution.getProblem().getNumberVariables()+"~"+solution.getProblem().getNumberConstraints()+" / "+this.getClass()+" / "+solution.getSolutionType().toString());
		}
		
		return fluxValues;
		
	}

	protected MapStringNum getBooleanVariables(LPSolution solution) {
		MapStringNum map = new MapStringNum();
		LPMapVariableValues values = solution.getValues();
		int initYindex = numberReac + numberRevReac + numberMeta;

		for (int i = 0; i < numberReac + numberRevReac; i++) {
			int indexY = i + initYindex;
			String reac = indexToIdVarMapings.get(i);
		
			if (!reac.endsWith("_Rev")) {
				double valYis = values.get(indexY);
				// reversible reaction
				if (indexRevReactions.contains(i)) {
					valYis = valYis +values.get(indexY + 1);
				}
				//Irrev: Se yi =0 -> tem fluxo
				//Rev: yi+=0 e y_-=0 -> impossivel  um deles yi=1 --> tem fluxo  yi=1+ e yi-=1 não tem fluxo 
				double hasFlux = 1.0;
				if((valYis == 1.0 && !indexRevReactions.contains(i)) || (valYis == 2.0 && indexRevReactions.contains(i)))
					hasFlux=0.0;

				int idx = getReactionVariableIndex(reac);
				System.out.println(reac +" " + indexToIdVarMapings.get(indexY) +" fluxo: "+ values.get(idx) + "binario " + valYis);
				if (indexRevReactions.contains(i))
				System.out.println(reac +" " + indexToIdVarMapings.get(indexY+1) +" fluxo: "+ values.get(idx+1) + "binario " + valYis);
				map.put(reac, hasFlux);
			}
		}
		return map;
	}

	// Auxiliary methods
	public String getObjectiveFunctionToString() {
		String ofString = "";
		if (problem != null) {
			boolean max = problem.getObjectiveFunction().isMaximization();
			if (max)
				ofString = "max:";
			else
				ofString = "min:";
			Map<String, Double> obj_coef = getMyObjectiveFunction();
			for (String id : obj_coef.keySet()) {
				double v = obj_coef.get(id);
				if (v != 1)
					ofString += " " + v;
				ofString += " " + id;
			}
		}
		return ofString;
	}

	private Map<String, Double> getMyObjectiveFunction() {
		Map<String, Double> res = new HashMap<String, Double>();
		LPProblemRow fo = problem.getObjectiveFunction().getRow();
		for (Integer idx : fo.getVarIdxs()) {
			String id = indexToIdVarMapings.get(idx);
			double val = fo.getTermCoefficient(idx);
			res.put(id, val);
		}
		return res;
	}

	// return a new line of Fake metabolite for stoichiometric matrix
	// R0 R1 R2
	// M_meta1_c 0 -1 0
	// M_meta1_c -1 1 0
	// return <0,-1> <1,0> <Reaction, Value>
	private Map<Integer, Double> getFakeMetaStoicLine(String metaId) {
		Map<Integer, Double> stoicLine = new HashMap<Integer, Double>();
		int ind = model.getMetaboliteIndex(metaId);
		int numRev = 0;
		for (int j = 0; j < numberReac; j++) {
			int indJ = j + numRev;
			double val = model.getStoichiometricValue(ind, j);
			boolean isReversible = indexRevReactions.contains(indJ);
			if (val != 0) {
				if (stoicLine.containsKey(indJ))
					val += stoicLine.get(indJ);
				stoicLine.put(indJ, val);
				// if reaction is reversible the next j are the <- direction
				if (isReversible) {
					stoicLine.put(indJ + 1, -val);
				}
			}
			if (isReversible)
				numRev++;
		}
		return stoicLine;
	}

	public void insertRequiredReactions(String taskId, Set<String> reacs)
			throws LinearProgrammingTermAlreadyPresentException {

		List<LPConstraint> constraints = addConstrains(reacs);
		try {
			LPSolution result = simulateProblem();
			if (!(result.getSolutionType().equals(LPSolutionType.FEASIBLE)
					|| result.getSolutionType().equals(LPSolutionType.OPTIMAL))) {
				System.out.println("NO satisfies task: " + taskId);
				problem.removeConstraintRange(constraints);
			} else
				System.out.println("PASS" + taskId);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("NO satisfies task: " + taskId);
			problem.removeConstraintRange(constraints);
		}
	}

	public void insertRequiredMetabolite(String metaId) {
		LPConstraint c = problem.getConstraint(mapFakeMetaConstraint.get(metaId));
		c.setRightSide(1.0);
		try {
			simulateProblem();
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("NO satisfies metabolite: " + metaId);
			c.setRightSide(0.0);
		}

	}

	public int getinitYindex() {
		return numberReac + numberRevReac + numberMeta;
	}

	public void printProblemToFile(String fileName) {
		try {
			FileWriter f = new FileWriter(fileName);
			BufferedWriter b = new BufferedWriter(f);

			// print variables
			for (LPVariable var : problem.getVariables()) {
				b.write(var.getLowerBound() + " < " + var.getVariableName() + " < " + var.getUpperBound());
				b.newLine();
			}
			// print constraints
			for (LPConstraint constraint : problem.getConstraints()) {
				String str = "";
				for (Integer varIndex : constraint.getLeftSide().getVarIdxs()) {
					str = str + constraint.getLeftSide().getTermCoefficient(varIndex) + " "
							+ problem.getVariable(varIndex).getVariableName() + " + ";
				}
				String type = constraint.getType().name().equals("LESS_THAN") ? " < "
						: (constraint.getType().name().equals("GREATER_THAN") ? " > " : " = ");

				b.write(str + type + constraint.getRightSide());
				b.newLine();
			}
			// print FO
			String str = "";
			for (Integer varIndex : problem.getObjectiveFunction().getRow().getVarIdxs()) {
				str = str + problem.getObjectiveFunction().getRow().getTermCoefficient(varIndex) + " "
						+ problem.getVariable(varIndex).getVariableName() + " + ";
			}

			b.write(str);
			b.close();
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Auxiliar methods
	// return the bounds (lb and ub) of reaction and reactionRev
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
