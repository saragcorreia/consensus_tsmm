package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.FastCoreConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MaxNumberReactions;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.formulations.MinimizesFluxPenaltySet;
import pt.uminho.ceb.biosystems.mew.omicsintegration.simplification.ContainerSimplification;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Utils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class FastCoreAlgorithm extends AbstractReconstructionAlgorithm {

	private Map<String , Double> core;
	private double threshold;
	private Container reducedContainer;

	public FastCoreAlgorithm(IOmicsConfiguration configuration) {
		super(configuration);
		threshold = getConfig().getFluxThreshold();
		
		core = new HashMap<String, Double>();
		Map<String, Double> values = getConfig().getCoreSet().getMapValues();
		core.putAll(values);

	}
	
	@Override
	public SpecificModelResult generateSpecificModel() throws Exception {
		
		// crate a clone with reaction that can have flux
		reducedContainer = ContainerSimplification.simplifyModel(getConfig().getTemplateContainer(), getConfig().getSolverType(), getConfig().getFluxThreshold());
		
		//model are based on the container after remove the fluxes ==0
		buidSteadyStateModel(reducedContainer); 

		// remove reaction with no flux from core 
		Set<String> ids = new HashSet<String>(core.keySet());
		for (String id : ids){
			if(!reducedContainer.getReactions().keySet().contains(id))
				core.remove(id);
		}

		Set<String> IrrevCore = getCoreIrrevReac();
		Set<String> IrrevReacs = getIrrevReactions();

		Set<String> P = new HashSet<String>(model.getReactions().keySet());
		P.removeAll(core.keySet());

		boolean flipped = false;
		boolean singleton = false;
		Set<String> A = findSparseMode(IrrevCore, P, singleton);

		Set<String> I = CollectionUtils.getSetDiferenceValues(core.keySet(), A);
		Set<String> visited = new TreeSet<String>();

		while (I.size() > 0) {
			System.out.println("Iter: " + I.size() + " fliped: " + flipped + " singleton: " + singleton + "visited:"
					+ visited.size());
			P.removeAll(A);
			A.addAll(findSparseMode(I, P, singleton));
			if (!Utils.isIntersectionEmpty(I, A)) {
				I.removeAll(A);
				flipped = false;
			} else {
				if (flipped) {
					flipped = false;
					singleton = true;
				} else {
					flipped = true;

					Set<String> I_line = new HashSet<String>();
					if (singleton) {
						Iterator<String> it = I.iterator();
						String elem = "";
						while (elem.equals("") && it.hasNext()) {
							elem = it.next();
							if (
							// !model.getReaction(elem).isReversible() ||
							visited.contains(elem)) {
								elem = "";
							}
						}
						I_line.add(elem);
						visited.add(elem);

					} else {
						I_line.addAll(I);
					}
					I_line.removeAll(IrrevReacs);

					for (String elem : I_line) {
						if (I_line.size() == 1 && elem.equals("")) {
							return buildTissueSpecificModel(A);
						}
						if (model.getReaction(elem).isReversible()) {
							swapDirectionMatrix(elem);
						}
					}
				}
			}

		}
		for (String r : A) {
			System.out.println(r);
		}
		return buildTissueSpecificModel(A);
	}

	// change the default reaction direction
	private void swapDirectionMatrix(String elem) {
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

	private Set<String> findSparseMode(Set<String> irrevCore, Set<String> penaltySet, boolean singleton)
			throws Exception {
		Set<String> res = new HashSet<String>();
		Set<String> I = irrevCore;
		if (irrevCore.size() > 0) {
			if (singleton) {
				I = new HashSet<String>();
				I.add(irrevCore.iterator().next());
			}
			MaxNumberReactions lp7 = new MaxNumberReactions(model, I, getConfig().getFluxThreshold());
			lp7.setSolverType(getConfig().getSolverType());

			SteadyStateSimulationResult simRes = lp7.simulate();
			Set<String> K = getActiveFluxes(irrevCore, simRes, false);

			if (K.size() != 0) {
				MinimizesFluxPenaltySet lp10 = new MinimizesFluxPenaltySet(model, K, penaltySet, getConfig().getFluxThreshold());
				lp10.setSolverType(getConfig().getSolverType());
				simRes = lp10.simulate();
				res = getActiveFluxes(null, simRes, true);
			}
		}
		return res;
	}

	// return the list of reactions from the set "reacs" that contains flux. If
	// set is empty, retrieve for all model reactions
	private Set<String> getActiveFluxes(Set<String> reacs, SteadyStateSimulationResult simRes, boolean bothDirections) {
		Set<String> res = new HashSet<String>();

		if (reacs == null)
			reacs = simRes.getModel().getReactions().keySet();
		for (String r : reacs) {
			double val = simRes.getFluxValues().getValue(r);
			if (val >= threshold || (bothDirections && val <= -1.0 * threshold)) {
				res.add(r);
			}
		}
		return res;
	}

	// return the irreversible reactions that belong to the core set
	private Set<String> getCoreIrrevReac() {
		Set<String> res = new HashSet<String>();
		for (String r : core.keySet()) {
			if (!model.getReactions().get(r).isReversible()) {
				res.add(r);
			}
		}
		return res;
	}

	private Set<String> getIrrevReactions() {
		Set<String> res = new HashSet<String>();
		for (Reaction r : model.getReactions().values()) {
			if (!r.isReversible()) {
				res.add(r.getId());
			}
		}
		return res;
	}

	@Override
	public String getObjectiveFunctionToString() {
		return "FASTCORE";
	}

	@Override
	public SpecificModelResult buildTissueSpecificModel(Set<String> reactions) {
		Container specificModel = reducedContainer;
		specificModel.removeReactions(
				CollectionUtils.getSetDiferenceValues(reducedContainer.getReactions().keySet(), reactions));
		return new SpecificModelResult(specificModel, getConfig());

	}
	private FastCoreConfiguration getConfig(){
		return (FastCoreConfiguration)configuration;
	}

}
