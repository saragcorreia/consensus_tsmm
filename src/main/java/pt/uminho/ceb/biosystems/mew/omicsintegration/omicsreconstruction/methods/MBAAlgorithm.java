package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.MBAConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MinMaxFormulation;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
/**
 * Assuming that omics data list have in first position the CH followed by CM
 * @author Sara
 *
 */
public class MBAAlgorithm extends AbstractReconstructionAlgorithm {
	private static boolean debug = true;


	private MinMaxFormulation formulation;
	private Set<String> highR;
	private Set<String> moderateR;
	private double threshold;

	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

	public MBAAlgorithm(MBAConfiguration configuration){
		super(configuration);
		highR = new HashSet<String>(getConfig().getCoreSet().getMapValues().keySet());
		moderateR = new HashSet<String>(configuration.getModerateSet().getMapValues().keySet());
		threshold = getConfig().getFluxThreshold();
	}

	@Override
	public SpecificModelResult generateSpecificModel() throws Exception {
		buidSteadyStateModel();

		formulation = new MinMaxFormulation(model);
		formulation.setSolverType(configuration.getSolverType());
		formulation.setIsMaximization(true);
	
		removeImpossibleReactions(highR);
		removeImpossibleReactions(moderateR);
		Set<String> finalModelReactions = run();
		return buildTissueSpecificModel(finalModelReactions);

	}

	public Set<String> run() throws Exception {

		// remove reaction that never has flux
		Set<String> Rp = new HashSet<String>(model.getReactions().keySet());
		Set<String> noFlux = getReactionsNoFlux();
		Rp.removeAll(noFlux);

		putVariableBoundsZero(noFlux);
		
		System.out.println("Modelo " + model.getReactions().size());
		System.out.println("Rp" + Rp.size());
		System.out.println("Inative Reactions:" + noFlux.size());
		System.out.println("Core Reac" + highR.size());
		System.out.println("ModReacs" + moderateR.size());

		List<String> randomP = getRandomPermutation(Rp);
		
		System.out.println("RandomP :" + randomP.size());

		for (int i = 0; i < randomP.size(); i++) {
			String r = randomP.get(i);
			if (debug)
				System.out.println("MBAAlgorithm " + i + " --> " + r + "-->"
						+ Rp.size());
			if (Rp.contains(r)) { // don't make sense analyze reactions that
									// already been removed
				// set the reaction with flux = 0 in problem formulation
				formulation.setBounds(r, 0.0, 0.0);
				Set<String> inactiveReactions = checkModelConsistency(Rp, r); // reset
																				// the
																				// bounds
				formulation.removeBounds(r);

				Set<String> eH = CollectionUtils.getIntersectionValues(
						inactiveReactions, highR);
				if (eH.size() == 0) {
					Set<String> eM = CollectionUtils.getIntersectionValues(
							inactiveReactions, moderateR);
					Set<String> eX = new HashSet<String>();
					eX.addAll(inactiveReactions);
					eX.removeAll(moderateR);
					if (eM.size() < (getConfig().getEpsilon() * eX.size())) {
						eM.addAll(eX);
						System.out.println("VOU TIRAR REACOES " + eM.size());
						putVariableBoundsZero(eM);
						System.out.println(printRemoveReactions(eM));
						Rp.removeAll(eM);
					}
				}
			}
		}
		return Rp;
	}

	private Set<String> getReactionsNoFlux() throws Exception {
		Set<String> inactive = new HashSet<String>();
		for (String r : model.getReactions().keySet()) {
			if (!formulation.hasFlux(r, threshold))
				inactive.add(r);
		}
		return inactive;
	}

	// build the final model based on set of models (runs)
	// the model must satisfy: all reactions of coreReactions can have flux
	public static Set<String> getFinalModel(ISteadyStateModel genericModel,
			Set<String> coreReactions, String path, String[] filesReactions, SolverType solver)
			throws Exception {
		double FLUX_THRESHOLD = 0.00001;
		Set<String> finalModel = new HashSet<String>();
		// get the number of times of each reaction appears in the models
		TreeMap<Double, Set<String>> scores = getReactionScores(path,
				filesReactions);
		

		// control center to validate the consistence of model
		MinMaxFormulation cc = new MinMaxFormulation(genericModel);
		cc.setSolverType(solver);
		cc.setIsMaximization(true);

		// reactions that can carry flux in the final model

		// get the core reactions that can have flux in the original model
		Set<String> toRemove = new HashSet<String>();
		cc.createOF_Flux(); // constantFO
		for (String r : coreReactions) {
			// System.out.println("reaction " + r);
			if (!genericModel.getReactions().containsKey(r)) {
				toRemove.add(r);
			} else if (!cc.hasFlux(r, FLUX_THRESHOLD)) {
				toRemove.add(r);
			}
		}
		coreReactions.removeAll(toRemove);
		finalModel.addAll(coreReactions);

		System.out.println("core reactions" + coreReactions);

		// set all reactions with bounds 0 - to simulate the model only with
		// core reactions
		EnvironmentalConditions koAll = new EnvironmentalConditions();
		for (String rId : genericModel.getReactions().keySet()) {
			if (!coreReactions.contains(rId))
				koAll.addReactionConstraint(rId, new ReactionConstraint(0.0,
						0.0));
		}
		cc.setBounds(koAll);

		System.out.println("inative " + koAll.size());
		// insert reactions with same score and validate if all coreReaction
		// have flux
		boolean coreOK = false;
		Iterator<Double> it = scores.descendingKeySet().iterator();
		
		while (it.hasNext() && !coreOK) {
			double val = it.next();
			Set<String> reactions = scores.get(val);
			System.out.println("Add " + reactions.size() + "--" + val);
			cc.removeBounds(reactions); // remove
			finalModel.addAll(reactions);
			coreOK = true;
			for (String core : coreReactions) {
				if (!cc.hasFlux(core,FLUX_THRESHOLD)) {
					coreOK = false;
					break;
				}
			}
		}

		// get reactin with no flux
		toRemove = new HashSet<String>();
		for (String r : finalModel) {
			System.out.println(r);
			if (!cc.hasFlux(r, FLUX_THRESHOLD))
				toRemove.add(r);
		}
		System.out.println("A remover" + toRemove.size());

		finalModel.removeAll(toRemove);

		return finalModel;
	}

	// get the score for each reaction Score =
	// NºFilesReacionOccur/NºTotalofFiles
	private static TreeMap<Double, Set<String>> getReactionScores(String path,
			String[] filesReactions) throws IOException {
		TreeMap<Double, Set<String>> sortScores = new TreeMap<Double, Set<String>>();

		Map<String, Double> scores = new HashMap<String, Double>();
		int numFiles = filesReactions.length;
		for (String file : filesReactions) {
			try (BufferedReader b = new BufferedReader(new FileReader(path + file))) {
				while (b.ready()) {
					String reac = b.readLine();
					double value = 1.0 / numFiles;
					if (scores.containsKey(reac))
						value += scores.get(reac);
					scores.put(reac, value);
				}
			}
		}
		System.out.println("N reacs" + scores.keySet().size());
		// for (String s : scores.keySet()) {
		// System.out.println(s);
		// }

		// join the reaction with same score in a single list
		for (Map.Entry<String, Double> entry : scores.entrySet()) {
			if (sortScores.containsKey(entry.getValue())) {
				sortScores.get(entry.getValue()).add(entry.getKey());
			} else {
				Set<String> reacSet = new HashSet<String>();
				reacSet.add(entry.getKey());
				sortScores.put(entry.getValue(), reacSet);
			}
		}
		return sortScores;
	}


	private void putVariableBoundsZero(Set<String> reactions)
			throws PropertyCastException, MandatoryPropertyException {
		Iterator<String> it = reactions.iterator();
		while (it.hasNext()) {
			formulation.setBounds(it.next(), 0.0, 0.0);
		}
	}

	private List<String> getRandomPermutation(Set<String> baseModel) {
		List<String> P = new ArrayList<String>();
		List<String> Rg = new ArrayList<String>();
		Rg.addAll(baseModel);
		Rg.removeAll(moderateR);
		Rg.removeAll(highR);
		Rg.removeAll(configuration.getTemplateContainer().identifyDrains());

		while (Rg.size() > 0) {
			int index = (int) Math.round(Math.random() * (Rg.size() - 1));
			P.add(Rg.get(index));
			Rg.remove(index);
		}
		return P;
	}

	// Check Consistency
	// ************************************************

	private Set<String> checkModelConsistency(Set<String> Rp, String r)
			throws Exception {
		System.out.println("checkModelConsistency  in:"
				+ sdf.format(Calendar.getInstance().getTime()));
		Set<String> inactiveReactions = new HashSet<String>();
		Set<String> reactionsList = new HashSet<String>();
		Set<String> activeReactions, activeReactionsRev;
		reactionsList.addAll(Rp);
		reactionsList.remove(r);
		inactiveReactions.add(r);

		// get reversible reactions
		Set<String> reactionsListRev = new HashSet<String>();
		for (String reacId : reactionsList) {
			if (model.getReaction(reacId).isReversible())
				reactionsListRev.add(reacId);
		}

		while (reactionsList.size() > 0) {
			// System.out.println("\t\t" + reactionsList.size() + "\t"
			// + sdf.format(Calendar.getInstance().getTimeInMillis() - dt));
			boolean runStep3 = true;
			// Step 1
			formulation.createOF_FluxSet(reactionsList, true);
			activeReactions = getActiveReactions(formulation.simulate()
					.getFluxValues());

			if (activeReactions.size() > 0
					&& reactionsList.removeAll(activeReactions)) {
				reactionsListRev.removeAll(activeReactions);
				runStep3 = false;
			}
			// Step 2
			formulation.createOF_FluxSet(reactionsListRev, false);
			activeReactionsRev = getActiveReactions(formulation.simulate()
					.getFluxValues());

			if (activeReactionsRev.size() > 0
					&& reactionsList.removeAll(activeReactionsRev)) {
				reactionsListRev.removeAll(activeReactionsRev);
				runStep3 = false;
			}

			// Step 3
			if (runStep3) {
				formulation.createOF_Flux(); // constant FO
				String reac = getRandomReaction(reactionsList);
				if (!formulation.hasFlux(reac, threshold)) {
					inactiveReactions.add(reac);
					if (highR.contains(reac)) {
						return inactiveReactions; // r inactives a highR
						// reaction
					}
				}
				reactionsList.remove(reac);
				reactionsListRev.remove(reac);
			}
		}
		System.out.println("checkModelConsistency out:"
				+ sdf.format(Calendar.getInstance().getTime()));
		return inactiveReactions;
	}

	private void removeImpossibleReactions(Set<String> reactions)
			throws Exception {
		Set<String> toRemove = new HashSet<String>();
		formulation.createOF_Flux(); // constantFO

		for (String r : reactions) {
			// System.out.println("reaction " + r);
			if (!model.getReactions().containsKey(r)) {
				toRemove.add(r);
			} else if (!formulation.hasFlux(r, threshold)) {
				toRemove.add(r);
			}
		}
		reactions.removeAll(toRemove);
	}

	// return a random reaction but if the list has required reactions CH return
	// one. Because if it required reaction don't have flux, the knockout is not
	// valid.
	private String getRandomReaction(Set<String> list) {
		for (String reac : list) {
			if (highR.contains(reac))
				return reac;
		}
		int index = (int) Math.round(Math.random() * (list.size() - 1));
		return (String) list.toArray()[index];
	}

	private Set<String> getActiveReactions(FluxValueMap solution) {
		Set<String> active = new HashSet<String>();
		for (String reacId : model.getReactions().keySet()) {
			if (solution.getValue(reacId) >= threshold
					|| solution.getValue(reacId) <= -threshold) {
				active.add(reacId);
			}
		}
		return active;
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "MBA";
	}

	public static String printRemoveReactions(Set<String> reactions) {
		String str = "Remove: ";
		Iterator<String> it = reactions.iterator();
		while (it.hasNext())
			str += it.next() + ",";
		return str;
	}
	
	private MBAConfiguration getConfig(){
		return (MBAConfiguration)configuration;
	}
}
