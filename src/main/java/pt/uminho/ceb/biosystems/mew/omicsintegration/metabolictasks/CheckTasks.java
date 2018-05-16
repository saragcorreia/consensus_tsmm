package pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TaskException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MinMaxFormulation;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.ParserEntities;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPSolutionType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class CheckTasks {
	private static boolean debug = false;
	private Container container;
	private Set<MetabolicTask> tasks;
	private boolean closeDrains;
	private SolverType solver;
	private MinMaxFormulation controlCenter;
	private EnvironmentalConditions envCond ;

	private double fluxThreshold;

	public CheckTasks(Container container, Set<MetabolicTask> tasks, boolean closeDrains, double fluxThreshold, EnvironmentalConditions env, SolverType solver) {
		this.container = container;
		this.tasks = tasks;
		this.closeDrains = closeDrains;
		this.fluxThreshold = fluxThreshold;
		this.envCond = env;
		this.solver = solver;
	}

	
	public CheckTasks(Container container, Set<MetabolicTask> tasks, boolean closeDrains, double fluxThreshold, SolverType solver) {
		this.container = container;
		this.tasks = tasks;
		this.closeDrains = closeDrains;
		this.fluxThreshold = fluxThreshold;
		this.solver = solver;
	}

	/**
	 * Get the list of essential reaction for satisfy a set of metabolic tasks.
	 * 
	 * @return a map where each task id is associated with the list of required
	 *         reactions ids to perform the task
	 * @throws Exception
	 */
	public Map<String, Set<String>> getRequiredReactions() throws Exception {
		Map<String, Set<String>> required = new HashMap<String, Set<String>>();
		Set<String> toTestKO = new HashSet<String>();
		Set<String> essential;

		for (MetabolicTask task : tasks) {
			if (debug)
				System.out.println("TASK " + task.getId());

			toTestKO.clear();
			essential = new HashSet<String>();

			Set<String> extraReactions = addExtraReactionsAndDrainsInContainer(task);

			// build Env Conditons
			EnvironmentalConditions env = buildEnvConditions(task, extraReactions);
			
			//override the env received as argument
			if(envCond!=null)
			env.addAllReactionConstraints(envCond);

			// get 1st distribution of fluxes
			SteadyStateSimulationResult result = runSimulationTask(task, env);

			// Task is possible
			if (result != null) {
				if (debug)
					System.out.println("Possible!");
				// get the small set that be checked by knockouts
				toTestKO = getSmallSetOfActiveReactions(result, task);

				// toTestKO.removeAll(extraReactions);

				// knockout each reaction and verify in the reaction is
				// essential for the task
				while (toTestKO.size() > 0) {
					if (debug)
						System.out.println("To Test: " + toTestKO.size() + " required:" + essential.size());

					String knockout = toTestKO.iterator().next();
					toTestKO.remove(knockout);
					boolean allowReactionKO = runSimulationReactionKO(task, knockout, env, toTestKO);
					if (allowReactionKO)
						essential.add(knockout);
				}

			} else
				essential.add("NOT SATISFY!");
			for (String id : extraReactions) {
				// if (debug)
				// System.out.println("removing: " + id);
				container.removeReaction(id);
				essential.remove(id); // remove the extra reactions
			}
			required.put(task.getId(), essential);
		}
		return required;

	}

	// public Set<String> getReactionWithFluxForTask(MetabolicTask task) {
	// Set<String> result = new HashSet<String>();
	// try {
	// // extra reaction added to perform the task: most be removed in
	// // the end
	// Set<String> extraReactions = addExtraReactionsAndDrainsInContainer(task);
	//
	// // build Env Conditons
	// EnvironmentalConditions env = buildEnvConditions(task, extraReactions);
	//
	// SteadyStateSimulationResult resultSimul = runSimulationTask(task, env);
	//
	// // Task is possible
	// if (resultSimul != null && Math.abs(resultSimul.getOFvalue()) >=
	// fluxThreshold
	// && !task.isFail()) {
	//
	//// System.out.println(task.getId() + " FO:" + resultSimul.getOFvalue());
	//
	// for (Pair<String, Double> p :
	// resultSimul.getFluxValues().getListFluxes()) {
	// if (Math.abs(p.getB()) > fluxThreshold)
	// result.add(p.getA());
	// }
	//
	//
	// } else {
	// System.out.println(task.getId() + " " + task.getObjectiveReaction() +
	// "<-> Not satisfy!");
	// }
	//
	// for (String id : extraReactions) {
	// if (debug)
	//// System.out.println("removing: " + id);
	// container.removeReaction(id);
	// }
	// } catch (Exception e) {
	// System.out.println(task.getId() + " not valid task!");
	// }
	// return result;
	//
	// }

	public boolean isAllValidWhithKO(Set<String> toRemove) throws InvalidSteadyStateModelException, Exception {
		boolean res = true;
		System.out.println("KO: " + toRemove.iterator().next());

		for (MetabolicTask task : tasks) {
			// System.out.println("task: "+ task.getId() );
			try {
				// extra reaction added to perform the task: most be removed in
				// the end
				Set<String> extraReactions = addExtraReactionsAndDrainsInContainer(task);

				// build Env Conditons
				EnvironmentalConditions env = buildEnvConditions(task, extraReactions);
				
				//override the env received as argument
				if(envCond!=null)
				env.addAllReactionConstraints(envCond);
				
				for (String reac : toRemove)
					env.addReactionConstraint(reac, new ReactionConstraint(0.0, 0.0));

				SteadyStateSimulationResult resultSimul = runSimulationTask(task, env);
				res = res && (isFeasible(resultSimul, task) != null);

				for (String id : extraReactions) {
					// System.out.println("remove extra reac:" + id);
					container.removeReaction(id);
				}

				if (!res)
					return res;

			} catch (Exception e) {
				System.out.println("problemas na task:" + task.getId() + " ao remover: " + toRemove.toString());
				e.printStackTrace();
				return false;
			}
		}
		return res;
	}

	public Set<String> getValidTaskIds(Set<String> reacs){
		Set<String> result = new HashSet<String>();
		String notTested = "";
		int nt = 0;
		for (MetabolicTask task : tasks) {
			try {
				// extra reaction added to perform the task: most be removed in
				// the end 
				
				Set<String> extraReactions = addExtraReactionsAndDrainsInContainer(task);

				// build Env Conditons
				EnvironmentalConditions env = buildEnvConditions(task, extraReactions);
				
				//override the env received as argument
				if(envCond!=null)
				env.addAllReactionConstraints(envCond);
				
				SteadyStateSimulationResult resultSimul = runSimulationTask(task, env);
//				 for(Map.Entry<String, Double> p
//				 :resultSimul.getFluxValues().entrySet()){
//				 if(p.getValue()!=0.0)
//				 System.out.println(p.getKey() + "--" + p.getValue());
//				 }
				//
				String taskId = isFeasible(resultSimul, task);
				if (taskId != null)
					result.add(taskId);

				for (String id : extraReactions) {
					if (debug)
						System.out.println("removing: " + id);
					container.removeReaction(id);
				}
			} catch (Exception e) {
				nt++;
				notTested = notTested + "," + task.getId();
				e.printStackTrace();
				System.out.println("------------->" + task.getId());

			}
			
		}
		System.out.println("task nao validada! " + nt + " - " + notTested);
		return result;

	}

	public Map<String, SteadyStateSimulationResult> getSimulationTasks() throws TaskException {
		Map<String, SteadyStateSimulationResult> res = new HashMap<String, SteadyStateSimulationResult>();
		for (MetabolicTask task : tasks) {
			// extra reaction added to perform the task: most be removed in
			// the end
			Set<String> extraReactions = addExtraReactionsAndDrainsInContainer(task);

			// build Env Conditons
			EnvironmentalConditions env = buildEnvConditions(task, extraReactions);
			
			//override the env received as argument
			if(envCond!=null)
			env.addAllReactionConstraints(envCond);
			
			
			SteadyStateSimulationResult resultSimul = runSimulationTask(task, env);

			String taskId = isFeasible(resultSimul, task);
			if (taskId != null)
				res.put(taskId, resultSimul);

			for (String id : extraReactions) {
				if (debug)
					System.out.println("removing: " + id);
				container.removeReaction(id);
			}

		}
		return res;
	}

	// Auxiliar funtions
	private boolean runSimulationReactionKO(MetabolicTask task, String knockout, EnvironmentalConditions env,
			Set<String> toTestKO) throws PropertyCastException, MandatoryPropertyException {
		boolean essential = false;

		if (env.containsKey(knockout) && (env.getReactionConstraint(knockout).getLowerLimit() > 0
				|| env.getReactionConstraint(knockout).getUpperLimit() < 0.0)) {
			essential = true;
		} else {
			ReactionConstraint rc = null;
			// remove envCondition
			if (env.containsKey(knockout)) {
				rc = env.remove(knockout);
				controlCenter.setEnvironmentalConditions(env);
			}
			controlCenter.setObjectiveFunction(null);
			controlCenter.setKnockout(knockout);
			controlCenter.setIsMaximization(true);
			try {
				SteadyStateSimulationResult result = controlCenter.simulate();

				if (result.getSolutionType().equals(LPSolutionType.FEASIBLE)
						|| result.getSolutionType().equals(LPSolutionType.OPTIMAL)) {
					Set<String> toTestAux = new HashSet<String>(toTestKO);
					for (String reac : toTestAux) {
						if (result.getFluxValues().get(reac) == 0.0) {
							toTestKO.remove(reac);
						}
					}
				} else {
					essential = true;
					if (debug)
						System.out.println(task.getId() + " Exception ->" + knockout);
				}
			} catch (Exception e) {
				essential = true;
				if (debug)
					System.out.println(task.getId() + " Exception ->" + knockout);
				e.printStackTrace();
			}
			if (rc != null) {
				env.addReactionConstraint(knockout, rc);
				controlCenter.setEnvironmentalConditions(env);
			}
		}
		return essential;

	}

	private SteadyStateSimulationResult runSimulationTask(MetabolicTask task, EnvironmentalConditions env) {
		SteadyStateSimulationResult result;
		try {
			// Set ControlCenter
			controlCenter = new MinMaxFormulation(ContainerConverter.convert(container));
			controlCenter.setSolverType(solver);
			controlCenter.setIsMaximization(task.isMaximization());
			controlCenter.setEnvironmentalConditions(env);

			if (task.getObjectiveReaction() != null) {
				controlCenter.createOF_Flux(task.getObjectiveReaction(), task.isMaximization());

			} else
				controlCenter.createOF_Flux();
			result = controlCenter.simulate();
			if (!(result.getSolutionType().equals(LPSolutionType.FEASIBLE)
					|| result.getSolutionType().equals(LPSolutionType.OPTIMAL)))
				result = null;
		} catch (Exception e) {
			result = null;
			if (debug)
				System.out.println("The problem is infeasible!");
			e.printStackTrace();
		}
		return result;
	}

	private String isFeasible(SteadyStateSimulationResult resultSimul, MetabolicTask task) {
		String res = null;
		if (resultSimul != null && ((Math.abs(resultSimul.getOFvalue()) >= fluxThreshold && !task.isFail())
				|| (Math.abs(resultSimul.getOFvalue()) <= fluxThreshold && task.isFail()))) {
			// System.out.println(task.getId() + " FO:" +
			// resultSimul.getOFvalue());
			res = task.getId();

			if (debug)
				System.out.println("Possible!");

		} else {
//			System.out.println(task.getId() + " FO:" + resultSimul.getOFvalue());
		}
		System.out.println(task.getId() + " FO:" + resultSimul.getOFvalue());
		return res;

	}

	// return the set of reaction that are always active in the task simulation
	private Set<String> getSmallSetOfActiveReactions(SteadyStateSimulationResult result, MetabolicTask task)
			throws Exception {
		Set<String> toTest = new HashSet<String>();
		// get flux distribution
		for (Map.Entry<String, Double> flux : result.getFluxValues().entrySet())
			if (flux.getValue() != 0.0)
				toTest.add(flux.getKey());

		// remove the reaction that are used in task and drains
		toTest.removeAll(task.getAddicionalReactions());
		toTest.removeAll(container.identifyDrains());

		int oldNumberToTest = Integer.MAX_VALUE;
		Map<String, Double> terms = new HashMap<String, Double>();
		// find the most small set of reaction from toTest
		//the obj function will be changet to Min SUM totest 
		controlCenter.setIsMaximization(false);
		while (toTest.size() > 0 && oldNumberToTest > toTest.size()) {
			if (debug)
				System.out.println("To Test: " + toTest.size());
			oldNumberToTest = toTest.size();
			terms.clear();
			for (String k : toTest)
				terms.put(k, 1.0);
			controlCenter.setObjectiveFunction(terms);
			// controlCenter.setEnvironmentalConditions(env);
			result = controlCenter.simulate();

			Set<String> toRemove = new HashSet<String>();
			for (String reac : toTest) {
				if (result.getFluxValues().get(reac) == 0.0) {
					toRemove.add(reac);
				}
			}
			toTest.removeAll(toRemove);
		}
		return toTest;
	}

	// add extra reaction to container and build extra drains to internal
	// metabolites
	private Set<String> addExtraReactionsAndDrainsInContainer(MetabolicTask task) throws TaskException {
		// Add additional reactions from task
		Set<String> extraReactions = task.verifyExistenceInContainer(container);

		try {
			for (ReactionCI reaction : task.getAddicionalReactions()) {
				container.addReaction(reaction);
			}
			// Add extra drains to internal metabolite
			for (Map.Entry<String, Pair<Double, Double>> metaDrain : task.getInternalMetaConstraints().entrySet()) {
//				System.out.println(metaDrain);
				String newDrainId = addDrain(task, metaDrain.getKey(), metaDrain.getValue().getA(),
						metaDrain.getValue().getB());

				extraReactions.add(newDrainId);
			}
			// objective metabolite
//			System.out.println("obj" + task.getObjectiveMetabolite());
			if (task.getObjectiveMetabolite() != null) {
				double lb = Config.LOWER_BOUND, ub = 0.0;
				if (task.isMaximization()) {
					lb = 0.0;
					ub = Config.UPPER_BOUND;
				}
				// if not exist drain for this metabolite
				String newDrainId = null;
				if (!container.getMetaboliteToDrain().keySet().contains(task.getObjectiveMetabolite())) {
					newDrainId = addDrain(task, task.getObjectiveMetabolite(), lb, ub);
					extraReactions.add(newDrainId);
				} else {
					newDrainId = container.getMetaboliteToDrain().get(task.getObjectiveMetabolite());
					// set the correct bounds for objective function based on
					// metbaolite
					task.getReacConstraints().addReactionConstraint(newDrainId, new ReactionConstraint(lb, ub));

				}
				task.setObjectiveReaction(newDrainId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new TaskException("Unable to test the metabolic task: " + task.getId() + "\n" + e.getMessage());
		}

		return extraReactions;
	}

	// add drain to container and to task reactions constraints
	private String addDrain(MetabolicTask task, String metabolite, double lb, double ub) throws Exception {
		String[] metaAndComp = ParserEntities.parseMetawithCompartment(metabolite);
		String metaId = metaAndComp[0];
		String comp = metaAndComp[1];

		if (comp == null && container.getMetaboliteCompartments(metaId).size() == 1) {
			comp = container.getMetaboliteCompartments(metaId).iterator().next();
		}
//		 System.out.println(metaId);
		String reacId = container.constructDrain(metaId, comp, lb, ub);
		task.getReacConstraints().addReactionConstraint(reacId, new ReactionConstraint(lb, ub));
		// if (debug)
		// System.out.println("DRAIN: comp " + comp + " meta " + metaId + " LB"
		// + lb + " UB" + ub);
		return reacId;
	}

	private EnvironmentalConditions buildEnvConditions(MetabolicTask task, Set<String> extraReactions) {
		EnvironmentalConditions env = new EnvironmentalConditions();

		// close the drains that are not presented in task
		if (closeDrains) {
			for (String drain : container.identifyDrains()) {
				if (!extraReactions.contains(drain))
					env.addReactionConstraint(drain, new ReactionConstraint(0.0, 0.0));
			}
		}	
		// Set task constraints
		env.addAllReactionConstraints(task.getReacConstraints());
		return env;

	}

}
