package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.MandatoryPropertyException;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.exceptions.PropertyCastException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.GenerationModelException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.MetabolicTask;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.components.EvidenceComparator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.components.ReactionEvidence;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.mCADREConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MinMaxFormulation;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class mCADREAlgorithm extends AbstractReconstructionAlgorithm {
	private static boolean debug = true;



	private List<ReactionEvidence> reactionEvidences;
	private Set<String> inactiveReactions;
	private Map<String, Set<String>> adjacentMatrixA;
	private MinMaxFormulation formulation;


	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

	public mCADREAlgorithm(mCADREConfiguration configuration) throws Exception {
		super(configuration);
		buidSteadyStateModel();
		this.reactionEvidences = new ArrayList<ReactionEvidence>();
		this.inactiveReactions = new HashSet<String>();
				
		ReactionDataMap expEvidence = getConfig().getReactionScores();	
		setReacionEvidence(expEvidence);
		
		if(getConfig().getConfidenceLevel()!=null)
			setConfidenceLevel();
		
		createProblem();
	}

	private void createProblem() throws Exception {
		formulation = new MinMaxFormulation(model);
		formulation.setIsMaximization(true);
		formulation.setSolverType(getConfig().getSolverType());
	}

	public void setReacionEvidence(ReactionDataMap expEvidence) {

		for (int i = 0; i < model.getReactions().size(); i++) {
			String reacId = model.getReactionId(i);
			ReactionEvidence re = new ReactionEvidence(reacId);
			// do this to distinguish gene associated reaction NOT expressed
			// (-0.01 score)
			// and non-gene associated reaction (0 score). In removal,gene
			// associated reaction NOT expressed to be removed first.
			double value = getConfig().getNotExpressedGeneValue();
			System.out.println(expEvidence.getMapValues().get(reacId));

			if (!expEvidence.getMapValues().containsKey(reacId) || expEvidence.getMapValues().get(reacId).isNaN()) {
				value = 0;
			} else if (expEvidence.getMapValues().get(reacId) != 0.0) {
				value = expEvidence.getMapValues().get(reacId);
			}
			re.setExpression(value);

			if (value >= getConfig().getCutOff())
				re.setCoreReaction(true);
			reactionEvidences.add(re);
		}
	}

	private double calculateConnectivityEvidence(String reacId) {
		double connectivityValue = 0.0;
		Set<String> relatedReacs = adjacentMatrixA.get(reacId);
		if (relatedReacs != null) {
			for (String r : relatedReacs) {
				// connectivityValue = connectivityValue +
				// getWeigthInfluenceOfReaction(r);
				// System.out.println(r + " --" +
				// reactionEvidences.get(model.getReactionIndex(r)).getExpression()
				// + " -- " +
				// reactionEvidences.get(model.getReactionIndex(r)).getReactionId());
				double exp = reactionEvidences.get(model.getReactionIndex(r)).getExpression();
				if (exp != getConfig().getNotExpressedGeneValue())
					connectivityValue += exp;
			}
			if (connectivityValue != 0.0)
				connectivityValue = connectivityValue / relatedReacs.size(); // new
		}
		return connectivityValue;
	}

	// FUN�AO DESCRITA NO PAPER MAS NA IMPLEMENTA��O DO INVESTIGADOR N�O � USADA
	// ASSIM
	// private double getWeigthInfluenceOfReaction(String reacId) {
	// double influence = 0.0;
	//
	// double sum = adjacentMatrixA.get(reacId).size();
	// if (sum != 0.0)
	// influence = 1.0 / sum;
	// double exp =
	// reactionEvidences.get(model.getReactionIndex(reacId)).getExpression();
	//
	// return influence * exp;
	// }

	// load the information from a CSV file. This confidence level is specific
	// for the template model (Recon1),
	// and tell us the level of biological evidence associated with each
	// reaction

	
	private void setConfidenceLevel(){
		ReactionDataMap conf = getConfig().getConfidenceLevel();
		for(Map.Entry<String, Double>entry: conf.getMapValues().entrySet()){
			if(!entry.getValue().isNaN() && model.getReactions().containsKey(entry.getKey())){
				reactionEvidences.get(model.getReactionIndex(entry.getKey())).setConfidence(entry.getValue());
			}
		}
	}

	// build the map with the reaction relations
	private void calculateAdjacentMatrix() {
		adjacentMatrixA = new HashMap<String, Set<String>>();
		for (int i = 0; i < model.getNumberOfMetabolites(); i++) {
			String metaId = model.getMetabolite(i).getId();
			String metaName = model.getMetabolite(i).getName();
			// don't make connection cross currency metabolites
			if (getConfig().getIgnoreMeta()!=null &&!getConfig().getIgnoreMeta().contains(metaName)) {
				Set<String> reactions = new HashSet<String>(getConfig().getTemplateContainer().getMetabolite(metaId).getReactionsId());
				for (String reac : reactions) {
					if (!adjacentMatrixA.containsKey(reac))
						adjacentMatrixA.put(reac, new HashSet<String>());
					for (String r : reactions) {
						if (!r.equals(reac))
							adjacentMatrixA.get(reac).add(r);
					}
				}
			}
		}
	}

	@Override
	public SpecificModelResult generateSpecificModel() throws Exception {

		Set<String> coreActiveG = new HashSet<String>();
		Set<String> noCoreReacs = new HashSet<String>();
		Set<String> newInactiveReactions;
		double s1, s2;

		Set<String> drains = getConfig().getTemplateContainer().identifyDrains();

		if (!checkModelFunction(null))
			throw new GenerationModelException("GenericModel failed to pass precursor metabolites test!");

		// calculate the connectivity evidence for reactions
		calculateAdjacentMatrix();
		for (int i = 0; i < model.getReactions().size(); i++) {
			double connectivity = calculateConnectivityEvidence(model.getReactionId(i));
			reactionEvidences.get(i).setConnectivity(connectivity);
		}

		// sort the reactions to be removed
		Collections.sort(reactionEvidences, EvidenceComparator.getInstance());

		// print values
		System.out.println("___________________________________________");
		for (int i = 0; i < reactionEvidences.size(); i++) {
			System.out.println(reactionEvidences.get(i).getReactionId() + ";"
					+ reactionEvidences.get(i).getConnectivity() + " ; " + reactionEvidences.get(i).getExpression()
					+ " ; " + reactionEvidences.get(i).getConfidence());
		}
		// Pruning model
		inactiveReactions.addAll(getReactionsNoFlux());

		// calculate the list of active core reactions and the non_core
		// reactions
		int numNoEvidenceExpression = 0;
		for (int i = 0; i < reactionEvidences.size(); i++) {
			String rId = reactionEvidences.get(i).getReactionId();
			if (reactionEvidences.get(i).getExpression() == getConfig().getNotExpressedGeneValue())
				numNoEvidenceExpression++;
			if (reactionEvidences.get(i).isCoreReactrion()) {
				if (!inactiveReactions.contains(rId))
					coreActiveG.add(rId);
			} else {
				noCoreReacs.add(rId);
			}
		}
		// The initial model used as a template has all reactions that can carry
		// flux
		HashSet<String> Rp = new HashSet<String>(model.getReactions().keySet());
		Rp.removeAll(inactiveReactions);

		System.out.println("model " + model.getReactions().size());
		System.out.println("Inative Reactions:" + inactiveReactions.size());
		System.out.println("rp" + Rp.size());
		System.out.println("Nr Non core Reac" + noCoreReacs.size());
		System.out.println("num NO EXP" + numNoEvidenceExpression);
		System.out.println("Core active:" + coreActiveG.size());

		putVariableBoundsZero(inactiveReactions);

		// if the reaction has GPR but none of the Genes has
		// expressed across all samples (Absent reaction) --> first
		// reaction to be treated

		for (int i = 0; i < numNoEvidenceExpression; i++) {
			String reacToRemove = reactionEvidences.get(i).getReactionId();
			if (debug)
				System.out.println(i + " reaction to test:" + reacToRemove + " Rp size:" + Rp.size());

			if (!inactiveReactions.contains(reacToRemove) && !drains.contains(reacToRemove)) {
				newInactiveReactions = checkModelConsistency(Rp, reacToRemove, false, null);
				s1 = CollectionUtils.getIntersectionValues(newInactiveReactions, coreActiveG).size();
				s2 = CollectionUtils.getIntersectionValues(newInactiveReactions, noCoreReacs).size();

				System.out.println(reactionEvidences.get(i).getExpression() + " - " + s1 + " - " + s2 + "=>" + s1 / s2);
				if (s1 / s2 <= getConfig().getRacio() && checkModelFunction(newInactiveReactions)) {
					Rp.removeAll(newInactiveReactions);
					inactiveReactions.addAll(newInactiveReactions);
					coreActiveG = CollectionUtils.getSetDiferenceValues(coreActiveG, newInactiveReactions);
					if (debug) {
						String output = "";
						for (String s : newInactiveReactions)
							output = output + ", " + s;
						System.out.println("also to remove:" + output);
					}
					putVariableBoundsZero(newInactiveReactions);
				}
			}
		}

		for (int i = numNoEvidenceExpression; i < noCoreReacs.size(); i++) {
			String reacToRemove = reactionEvidences.get(i).getReactionId();
			if (debug)
				System.out.println(i + " reaction to test:" + reacToRemove + " Rp size:" + Rp.size());
			if (!inactiveReactions.contains(reacToRemove) && !drains.contains(reacToRemove)) {
				newInactiveReactions = checkModelConsistency(Rp, reacToRemove, true, noCoreReacs);
				s1 = CollectionUtils.getIntersectionValues(newInactiveReactions, coreActiveG).size();

				System.out.println(reactionEvidences.get(i).getExpression() + " / " + s1);

				if (s1 == 0 && checkModelFunction(newInactiveReactions)) {
					Rp.removeAll(newInactiveReactions);
					inactiveReactions.addAll(newInactiveReactions);
					if (debug) {
						String output = "";
						for (String s : newInactiveReactions)
							output = output + ", " + s;
						System.out.println("also to remove:" + output);
					}
					putVariableBoundsZero(newInactiveReactions);
				}
			}
		}
		return buildTissueSpecificModel(Rp);
	}

	public void setEnvironmentalConditions(EnvironmentalConditions env) {
		formulation.setEnvironmentalConditions(env);
	}

	private void putVariableBoundsZero(Set<String> reactions) throws PropertyCastException, MandatoryPropertyException {
		Iterator<String> it = reactions.iterator();
		while (it.hasNext()) {
			formulation.setBounds(it.next(), 0.0, 0.0);
		}
	}

	private void putOriginalBounds(Set<String> reactions) throws PropertyCastException, MandatoryPropertyException {
		Iterator<String> it = reactions.iterator();
		while (it.hasNext()) {
			formulation.removeBounds(it.next());
		}
	}


	private Set<String> checkModelConsistency(Set<String> Rp, String r, boolean cutCore, Set<String> noCoreReacs)
			throws Exception {
		System.out.println("checkModelConsistency  in:" + sdf.format(Calendar.getInstance().getTime()));
		Set<String> inactiveReactions = new HashSet<String>();
		List<String> reactionsList = new ArrayList<String>();
		Set<String> activeReactions, activeReactionsRev;
		reactionsList.addAll(Rp);
		reactionsList.remove(r);
		inactiveReactions.add(r);

		// knockout reaction
		formulation.setBounds(r, 0, 0);

		// get reversible reactions
		Set<String> reactionsListRev = new HashSet<String>();
		for (String reacId : reactionsList) {
			if (model.getReaction(reacId).isReversible())
				reactionsListRev.add(reacId);
		}

		boolean runStep3 = false;
		int i = 0;
		while (reactionsList.size() > 0 && !runStep3) {
			runStep3 = true;
			i++;
			// System.out.print(reactionsList.size() + ", ");

			// Step 1
			formulation.createOF_FluxSet(new HashSet<String>(reactionsList), true);
			activeReactions = getActiveReactions(formulation.simulate().getFluxValues());
			if (activeReactions.size() > 0 && reactionsList.removeAll(activeReactions)) {
				reactionsListRev.removeAll(activeReactions);
				runStep3 = false;
			}
			// Step 2
			formulation.createOF_FluxSet(reactionsListRev, false);
			activeReactionsRev = getActiveReactions(formulation.simulate().getFluxValues());

			if (activeReactionsRev.size() > 0 && reactionsList.removeAll(activeReactionsRev)) {
				reactionsListRev.removeAll(activeReactionsRev);
				runStep3 = false;
			}
		}

		System.out.print("step3 " + reactionsList.size());
		while (reactionsList.size() > 0) {
			i++;
			// Step 3
			formulation.createOF_Flux(); // constant FO
			String reac = reactionsList.get(0);
			System.out.print(reac + "; ");
			if (!formulation.hasFlux(reac, getConfig().getFluxThreshold())) {
				inactiveReactions.add(reac);
				if (cutCore && !noCoreReacs.contains(reac)) {
					// set original bounds
					formulation.removeBounds(r);
					System.out.println(i);
					System.out.println("\n checkModelConsistency out:" + sdf.format(Calendar.getInstance().getTime()));
					return inactiveReactions;
				}
			}
			reactionsList.remove(reac);
			reactionsListRev.remove(reac);
		}
		// set original bounds
		formulation.removeBounds(r);
		System.out.println(i);
		System.out.println("\n checkModelConsistency out:" + sdf.format(Calendar.getInstance().getTime()));
		return inactiveReactions;
	}


	// for each metabolite find a reaction where the metabolite participate with
	// flux!=0
	private boolean checkModelFunction(Set<String> toRemoveReactions) throws Exception {
		if (getConfig().getMetabolicTasks() == null)
			return true;
		if (toRemoveReactions == null)
			toRemoveReactions = new HashSet<String>();
		putVariableBoundsZero(toRemoveReactions);
		Set<String> drains = getConfig().getTemplateContainer().identifyDrains();

		boolean check = false;

		for (MetabolicTask task : getConfig().getMetabolicTasks()) {
			for (String r : task.getReacConstraints().keySet()) {
				double threshold = task.getReacConstraints().get(r).getLowerLimit() > 0
						? task.getReacConstraints().get(r).getLowerLimit()
						: task.getReacConstraints().get(r).getUpperLimit();
				check = formulation.hasFlux(r, threshold);
				if (!check)
					return false;
			}
			Map<String, Pair<Double, Double>> keyMetabolites = task.getInternalMetaConstraints();
			for (String meta : keyMetabolites.keySet()) {
				check = false;
				if (getConfig().getTemplateContainer().getMetabolites().containsKey(meta)) {
					Set<String> reactions = getConfig().getTemplateContainer().getMetabolite(meta).getReactionsId();
					for (String r : reactions) {
						if (!(toRemoveReactions.contains(r) || drains.contains(r) || inactiveReactions.contains(r))
								&& formulation.hasFlux(r, getConfig().getFluxThreshold())) {
							check = true;
							break;
						}
					}
				}
				if (!check) {
					if (debug)
						System.out.println(meta + " can not be produced!");
					putOriginalBounds(toRemoveReactions);
					return false;
				}
			}
		}
		putOriginalBounds(toRemoveReactions);
		return true;
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "mCADRE";
	}

	// auxiliar
	private Set<String> getActiveReactions(FluxValueMap solution) {
		Set<String> active = new HashSet<String>();
		for (String reacId : model.getReactions().keySet()) {
			if (solution.getValue(reacId) >= getConfig().getFluxThreshold()
					|| solution.getValue(reacId) <= -getConfig().getFluxThreshold()) {
				active.add(reacId);
			}
		}
		return active;
	}

	private Set<String> getReactionsNoFlux() throws Exception {
		Set<String> inactive = new HashSet<String>();
		for (String r : model.getReactions().keySet()) {
			if (!formulation.hasFlux(r, getConfig().getFluxThreshold())) {
				inactive.add(r);
			}
		}
		System.out.println("mCADRE : INATIVE " + inactive.size());
		return inactive;
	}
	
	private mCADREConfiguration getConfig(){
		return (mCADREConfiguration)configuration;
	}
}
