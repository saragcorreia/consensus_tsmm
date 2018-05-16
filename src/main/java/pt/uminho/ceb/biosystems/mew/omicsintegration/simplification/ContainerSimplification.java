package pt.uminho.ceb.biosystems.mew.omicsintegration.simplification;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MaxNumberReactions;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MinMaxFormulation;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Utils;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class ContainerSimplification {

	public static Container simplifyModel(Container container, SolverType solver, double fluxThreshold) throws Exception {
		Container reducedContainer = container.clone();

		ISteadyStateModel model = ContainerConverter.convert(reducedContainer);

		// simplify model
		MinMaxFormulation form = new MinMaxFormulation(model);
		form.setSolverType(solver);
		form.setRecreateOF(true);
		String list="";
		
		
		for (String r : model.getReactions().keySet()) {
			boolean res = form.hasFlux(r, fluxThreshold);
			if (!res) {
				reducedContainer.removeReaction(r);
				list=list+","+r;
			}
		}
		System.out.println(list+"\n container size" + reducedContainer.getReactions().size());
		return reducedContainer;
	}

	// simplification used in FASTCORE paper - NOT 100% TESTED
	public static Container fastcc(Container container, SolverType solver, double fluxThreshold) throws Exception {
		ISteadyStateModel model = ContainerConverter.convert(container);

		Set<String> N = model.getReactions().keySet();
		Set<String> I = getIrrevReactions(model);
		Set<String> A = new HashSet<String>();

		// start with I
		Set<String> J = new HashSet<String>(I);

		MaxNumberReactions lp7 = new MaxNumberReactions(model, J, fluxThreshold);
		lp7.setSolverType(solver);
		SteadyStateSimulationResult simRes = lp7.simulate();
		Set<String> Supp = getActiveFluxes(simRes, fluxThreshold);
		A = Supp;

		Set<String> incI = CollectionUtils.getSetDiferenceValues(J, A);
		if (incI.size() != 0)
			System.out.println("inconsistent subset of I detected)");

		J = CollectionUtils.getSetDiferenceValues(CollectionUtils.getSetDiferenceValues(N, A), incI);

		// reversible reactions
		boolean flipped = false;
		boolean singleton = false;
		while (J.size() != 0) {
			Set<String> Ji;
			System.out.println("J:" + J.size());
			if (singleton) {
				Ji = new HashSet<String>();
				Ji.add(J.iterator().next());
				MinMaxFormulation lp3 = new MinMaxFormulation(model);
				lp3.setSolverType(solver);
				String x = Ji.iterator().next();
				System.out.println(x);
				boolean exist = lp3.hasFlux(x, fluxThreshold);
				if (exist)
					A.add(x);
			} else {
				Ji = new HashSet<String>(J);
				lp7 = new MaxNumberReactions(model, Ji, fluxThreshold);
				lp7.setSolverType(solver);
				simRes = lp7.simulate();

				Supp = getActiveFluxes(simRes, fluxThreshold);
				A.addAll(Supp);
			}

			if (!Utils.isIntersectionEmpty(J, A)) {
				J.removeAll(A);
				flipped = false;
			} else {
				Set<String> JiRev = CollectionUtils.getSetDiferenceValues(Ji, I);
				if (flipped || JiRev.size() == 0) {
					flipped = false;
					if (singleton) {
						J.removeAll(Ji);
						System.out.println("inconsistent reversible reaction detected");
					} else
						singleton = true;
				} else {
					flipped = true;
					for (String elem : JiRev) {
						if (model.getReaction(elem).isReversible()) {
							swapDirectionMatrix(model, elem);
						}

					}
				}
			}
		}

		// build the new container
		Container result = container.clone();
		result.removeReactions(CollectionUtils.getSetDiferenceValues(container.getReactions().keySet(), A));

		return result;
	}

	private static Set<String> getIrrevReactions(ISteadyStateModel model) {
		Set<String> res = new HashSet<String>();
		for (Reaction r : model.getReactions().values()) {
			if (!r.isReversible()) {
				res.add(r.getId());
			}
		}
		return res;
	}

	private static void swapDirectionMatrix(ISteadyStateModel model, String elem) {
		int indexReac = model.getReactionIndex(elem);
		// change bounds
		double lb = model.getReactionConstraint(indexReac).getLowerLimit();
		double up = model.getReactionConstraint(indexReac).getUpperLimit();
		model.getReactionConstraint(indexReac).setLowerLimit(up * -1);
		model.getReactionConstraint(indexReac).setUpperLimit(lb * -1);

		// change stoichiometric matrix
		for (int i = 0; i < model.getMetabolites().size(); i++) {
			double nv = model.getStoichiometricValue(i, indexReac) == 0 ? 0.0
					: model.getStoichiometricValue(i, indexReac) * -1.0;
			if (nv != 0.0)
				model.setStoichiometricValue(i, indexReac, nv);
		}

	}

	private static Set<String> getActiveFluxes(SteadyStateSimulationResult simRes, double fluxThreshold) {
		Set<String> res = new HashSet<String>();
		Set<String> reacs = simRes.getModel().getReactions().keySet();

		for (String r : reacs) {
			double val = simRes.getFluxValues().getValue(r);
			if (val >= fluxThreshold || val <= -1.0 * fluxThreshold) {
				res.add(r);
			}
		}
		return res;
	}


	/**
	 * Return a set of metabolite id, where each one is product or reagent
	 * across all reactions
	 * 
	 * @param simpContainer
	 *            model comtainer
	 * @return set of metabolite ids
	 * @throws InvalidSteadyStateModelException
	 */
	private static Set<String> getOnlyProductOrReagentMetas(Container simpContainer)
			throws InvalidSteadyStateModelException {
		ISteadyStateModel model = ContainerConverter.convert(simpContainer);
		IStoichiometricMatrix s = model.getStoichiometricMatrix();
		int[] usedMeta = countReacPerMeta(model);
		int[] onlyProds = countReacPerMetaProdOrReac(model, true);
		int[] onlyReag = countReacPerMetaProdOrReac(model, false);
		ArrayList<Integer> indToRemove = new ArrayList<Integer>();
		for (int i = 0; i < s.rows(); i++) {
			if (usedMeta[i] == onlyProds[i] || usedMeta[i] == onlyReag[i] || isOneRevReac(s.getRow(i), model))
				indToRemove.add(i);
		}
		Set<String> removeMetas = new HashSet<String>();
		for (int index : indToRemove) {
			removeMetas.add(model.getMetaboliteId(index));
		}
		return removeMetas;
	}

	private static boolean isOneRevReac(double[] row, ISteadyStateModel model) {
		int count = 0;
		for (int i = 0; i < row.length; i++) {
			if (row[i] != 0) {
				count++;
				if (count == 2 || !model.getReaction(i).isReversible())
					return false;
			}
		}
		return true;
	}

	/**
	 * For each metabolite calculate the number of reactions that use it.
	 * 
	 * @param ISteadyStateModel
	 *            metabolic model
	 * @return int [] each position contain the number of reaction that use the
	 *         metabolite i
	 */
	private static int[] countReacPerMeta(ISteadyStateModel model) {
		IStoichiometricMatrix s = model.getStoichiometricMatrix();
		int[] sum = new int[s.rows()];
		for (int i = 0; i < s.rows(); i++) {
			sum[i] = 0;
			double[] values = s.getRow(i);
			for (int j = 0; j < values.length; j++)
				if (values[j] != 0) {
					sum[i]++;
					if (model.getReaction(j).isReversible())
						sum[i]++;
				}
		}
		return sum;
	}

	/**
	 * For each metabolite calculate the number of reactions where the
	 * metabolite is (or not) a product.
	 * 
	 * @param ISteadyStateModel
	 *            model
	 * @param product
	 *            if true - consider product as a objective for counting
	 * @return array with the value for each metabolite
	 */
	private static int[] countReacPerMetaProdOrReac(ISteadyStateModel model, boolean product) {
		IStoichiometricMatrix s = model.getStoichiometricMatrix();
		int[] sum = new int[s.rows()];
		for (int i = 0; i < s.rows(); i++) {
			sum[i] = 0;
			double[] values = s.getRow(i);
			for (int j = 0; j < values.length; j++)
				if ((values[j] > 0 || (values[j] < 0 && model.getReaction(j).isReversible())) && product)
					sum[i]++;
				else if ((values[j] < 0 || (values[j] > 0 && model.getReaction(j).isReversible())) && !product)
					sum[i]++;
		}
		return sum;
	}

	/**
	 * Return the set of reactions that not carry any flux
	 * 
	 * @param simpContainer
	 *            model container
	 * @return
	 * @throws Exception
	 */
	public static Set<String> hasNoFlux(Container simpContainer, SolverType solver, double threshold) throws Exception {
		
		ISteadyStateModel model = ContainerConverter.convert(simpContainer);
		Set<String> reacList = new HashSet<String>();
		MinMaxFormulation minmax = new MinMaxFormulation(model);
		
		minmax.setSolverType(solver);

		for (int i = 0; i < model.getNumberOfReactions(); i++) {
			String reacId = model.getReactionId(i);
			try {
				minmax.createOF_Flux(reacId, true);
				SteadyStateSimulationResult solution = minmax.simulate();
				double valMax = solution.getFluxValues().get(reacId);
				if (!(valMax >= threshold)) {
					if (model.getReaction(reacId).isReversible()) {
						minmax.createOF_Flux(reacId, false);
						solution = minmax.simulate();
						double valMin = solution.getFluxValues().get(reacId);
						if (!(valMin <= threshold * -1)) {
							reacList.add(reacId);
//							System.out.println("irrev i:" + i + " - " + reacId);
						}
					} else {
						reacList.add(reacId);
//						System.out.println("i:" + i + " - " + reacId);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Number of inactive reactions:" + reacList.size());
		return reacList;
	}

	// #######################################
	/**
	 * Based on a set of available metabolites (in the beginning drains) get the
	 * reactions that uses them as a products and find other set of metabolites
	 * (the new available metabolites). The recursively process stop when the
	 * set of new metabolites is empty.
	 * 
	 * @param cont
	 *            metabolic container
	 * @return the simplify container
	 */
//	public static Container SimplifyModel(Container cont) {
//		Container simpContainer = cont.clone();
//		Set<String> availableMetaIds = new HashSet<String>();
//		Set<String> usedReactions = new HashSet<String>();
//
//		// calculate dependencies between metabolites and reactions
//		Map<String, Set<String>> metaReactionsRelation = getMetaboliteReactionsRelations(simpContainer);
//
//		Set<String> reactionsToRemove = new HashSet<String>();
//		reactionsToRemove.addAll(simpContainer.getReactions().keySet());
//
//		// set the available metabolites and the used reactions
//		Set<String> drains = simpContainer.getDrains();
//		for (String d : drains) {
//			ReactionCI r = cont.getReaction(d);
//			availableMetaIds.addAll(r.getProducts().keySet());
//			if (r.getReversible()) {
//				availableMetaIds.addAll(r.getReactants().keySet());
//			}
//		}
//
//		Set<String> newUsedMetabolites = new HashSet<String>();
//		newUsedMetabolites.addAll(availableMetaIds);
//		while (!newUsedMetabolites.isEmpty()) {
//			Set<String> validateReactions = getReacForValidate(newUsedMetabolites, metaReactionsRelation,
//					usedReactions);
//			if (validateReactions.isEmpty()) {
//				System.out.println("ASNEIRA ... MODELO INCOMPLETO");
//			}
//			newUsedMetabolites = new HashSet<String>();
//			for (String rId : validateReactions) {
//				Set<String> prods = cont.getReaction(rId).getProducts().keySet();
//				Set<String> reac = cont.getReaction(rId).getReactants().keySet();
//				if (isAllMetaPresent(reac, availableMetaIds)) {
//					usedReactions.add(rId);
//					reactionsToRemove.remove(rId);
//					newUsedMetabolites.addAll(prods);
//				} else if (cont.getReaction(rId).isReversible() && isAllMetaPresent(prods, availableMetaIds)) {
//					usedReactions.add(rId);
//					reactionsToRemove.remove(rId);
//					newUsedMetabolites.addAll(reac);
//				}
//			}
//			availableMetaIds.addAll(newUsedMetabolites);
//		}
//		simpContainer.removeReactions(reactionsToRemove);
//		return simpContainer;
//	}
//
//	/**
//	 * Return the new reactions that can be reached based from the new available
//	 * metabolites.
//	 * 
//	 * @param newUsedMetabolites
//	 *            set of new available metabolites
//	 * @param metaReactionsRelation
//	 *            relation between each metabolite and the reactions that use it
//	 * @param usedReactions
//	 *            set of reactions that was validated previously
//	 * @return set of reaction ids
//	 */
//	private static Set<String> getReacForValidate(Set<String> newUsedMetabolites,
//			Map<String, Set<String>> metaReactionsRelation, Set<String> usedReactions) {
//		Set<String> validateReactions = new HashSet<String>();
//		for (String meta : newUsedMetabolites) {
//			Set<String> metaReactions = metaReactionsRelation.get(meta);
//			for (String r : metaReactions) {
//				if (!usedReactions.contains(r))
//					validateReactions.add(r);
//			}
//		}
//		return validateReactions;
//	}

//	/**
//	 * Verify if a set of metabolites are present in other set.
//	 * 
//	 * @param metaIds
//	 *            set a metabolite ids to validate
//	 * @param availableMetaIds
//	 *            the available metabolites in the network
//	 * @return true if the available metaIds contains all metaIds
//	 */
//	private static boolean isAllMetaPresent(Set<String> metaIds, Set<String> availableMetaIds) {
//		if (metaIds.isEmpty())
//			return true;
//		for (String m : metaIds)
//			if (!availableMetaIds.contains(m))
//				return false;
//		return true;
//	}
//
//	/**
//	 * Build the relation between each metabolite and the reactions that use it
//	 * as a product or reagent.
//	 * 
//	 * @param cont
//	 *            metabolic container
//	 * @return map<metaboliteId, set<reactionID>>
//	 */
//	private static Map<String, Set<String>> getMetaboliteReactionsRelations(Container cont) {
//		Map<String, Set<String>> rel = new HashMap<String, Set<String>>();
//		for (String m : cont.getMetabolites().keySet()) {
//			rel.put(m, cont.getMetabolite(m).getReactionsId());
//		}
//		return rel;
//	}

	// #################################
	public static void printReactions(Set<String> reactions, String file) throws IOException {
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);

		for (String r : reactions) {
			bw.write(r);
			bw.newLine();
		}
		bw.close();
		fw.close();
	}

	public static void main(String[] args) {
		try {
			// String sbmlModel =
			// "D:/Metabolic Models/Human/iHuman2207_withDrains.xml";
			// String sbmlModel = "D:/Metabolic Models/exp2DrainsRev.xml";
			String sbmlModel = "D:/Metabolic Models/Human/Recon1.xml";

			Container cont = new Container(new JSBMLReader(sbmlModel, "Human"));
			cont.removeMetabolites(cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")));

			// Container simp = simplifyModel_RAVEN(cont);
			// JSBMLWriter w = new
			// JSBMLWriter("D:/Projects/PHD - P01 -
			// Reconst_Approaches/Results/Simplification_JAVA/iHuman2207_withDrains_simplifyRAVEN.xml",
			// simp);
			// w.writeToFile();
			//
			// printReactions(simp.getReactions().keySet(),
			// "D:/Projects/PHD - P01 -
			// Reconst_Approaches/Results/Simplification_JAVA/simplifyModel_RAVEN.txt");
			//
			// cont = new Container(new JSBMLReader(sbmlModel, "Human"));
			// System.out.println("N Meta " + cont.getMetabolites().size());
			// System.out.println("N Reac " + cont.getReactions().size());
			// System.out.println("N Meta " + simp.getMetabolites().size());
			// System.out.println("N Reac " + simp.getReactions().size());

			// Container simp = SimplifyModel(cont);

			System.out.println("N Meta " + cont.getMetabolites().size());
			System.out.println("N Reac " + cont.getReactions().size());
			// System.out.println("N Meta " + simp.getMetabolites().size());
			// System.out.println("N Reac " + simp.getReactions().size());

			Set<String> inactive = new HashSet<String>();
			ISteadyStateModel model = ContainerConverter.convert(cont);
			MinMaxFormulation formulation = new MinMaxFormulation(model);
			int count = 0;
			for (String r : model.getReactions().keySet()) {
				count++;
				System.out.println("" + count);
				if (!formulation.hasFlux(r, 0.00001))
					inactive.add(r);
			}
			System.out.println("Inactive " + inactive.size());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
