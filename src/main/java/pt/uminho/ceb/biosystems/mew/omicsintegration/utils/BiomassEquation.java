package pt.uminho.ceb.biosystems.mew.omicsintegration.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.fva.FBAFluxVariabilityAnalysis;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class BiomassEquation {

	public static Set<String> reconstructBiomassEquation(Container templateContainer, EnvironmentalConditions env,
			Container specificContainer) throws Exception {

		Set<String> metNotProduced = new HashSet<String>();
		ReactionCI biomassEquation = templateContainer.getReaction(templateContainer.getBiomassId());
		Set<String> precursor = biomassEquation.getReactants().keySet();
		Map<String, ReactionCI> reactions = new HashMap<String, ReactionCI>();
		Map<String, MetaboliteCI> metabolites = new HashMap<String, MetaboliteCI>();

		reactions.putAll(specificContainer.getReactions());
		metabolites.putAll(specificContainer.getMetabolites());

		HashMap<String, StoichiometryValueCI> reactantsInfo = new HashMap<String, StoichiometryValueCI>();
		HashMap<String, StoichiometryValueCI> productsInfo = new HashMap<String, StoichiometryValueCI>();

		Set<String> newDrains = new HashSet<String>();
		Set<String> newPercursor = new HashSet<String>();

		// create the new drains for biomass precursor
		for (String m : precursor) {
			if (specificContainer.getMetabolites().keySet().contains(m)) {
				String drainName = "SGC_" + m;
				ReactionCI drain = buildDrain(drainName, m, specificContainer.getExternalCompartmentId());
				reactions.put(drainName, drain);
				newDrains.add(drainName);
				newPercursor.add(m);
			}
		}
		specificContainer.setMetabolites(metabolites);
		specificContainer.setReactions(reactions);

		ISteadyStateModel model = ContainerConverter.convert(specificContainer);
		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, env, null, SolverType.CPLEX, null);

		// validate what precursors have flux!=0
		for (String drain : newDrains) {
			double[] limits = fva.limitsFlux(drain);
			String meta = drain.substring(4);
			if (limits[0] != 0.0 || limits[1] != 0.0) {
				// metabolite is produced, can be included in the biomass
				// equation
				reactantsInfo.put(meta, biomassEquation.getReactants().get(meta).clone());
			} else {
				metNotProduced.add(meta);
			}
		}

		// remove the drains used to validate the precursor production
		specificContainer.removeReactions(newDrains);

		// build the new biomass equation
		for (String meta : biomassEquation.getProducts().keySet()) {
			productsInfo.put(meta, biomassEquation.getProducts().get(meta).clone());
		}
		ReactionCI newBiomass = new ReactionCI(biomassEquation.getId(), biomassEquation.getId(), false, reactantsInfo,
				productsInfo);

		reactions.put(biomassEquation.getId(), newBiomass);
		specificContainer.setReactions(reactions);
		specificContainer.setBiomassId(newBiomass.getId());
		specificContainer.verifyDepBetweenClass();

		return metNotProduced;
	}

	public static ReactionCI buildDrain(String drainName, String meta, String comp) {

		Map<String, StoichiometryValueCI> reactante = new HashMap<String, StoichiometryValueCI>();
		StoichiometryValueCI stoic = new StoichiometryValueCI(meta, 1.0, comp);
		reactante.put(drainName, stoic);
		ReactionCI drain = new ReactionCI(drainName, drainName, false, reactante,
				new HashMap<String, StoichiometryValueCI>());

		return drain;
	}

	public static Set<String> getDrainsPercursorBiomassRecon1() {
		Set<String> res = new HashSet<String>();
		res.add("R_EX_chsterol_LPAREN_e_RPAREN_");
		res.add("R_EX_cys_L_LPAREN_e_RPAREN_");
		res.add("R_EX_pail_hs_e_");
		res.add("R_EX_pchol_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_pe_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_ps_hs_LPAREN_e_RPAREN_");

		res.add("R_EX_dag_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_damp_e_");
		res.add("R_EX_dcmp_e_");
		res.add("R_EX_dgmp_e_");
		res.add("R_EX_dtmp_e_");
		res.add("R_EX_lpchol_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_mag_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_pa_hs_e_");
		res.add("R_EX_xolest_hs_LPAREN_e_RPAREN_");
		return res;
	}

	public static Set<String> getDrainsPercursorBiomassRecon2() {
		Set<String> res = new HashSet<String>();
		res.add("R_EX_chsterol_LPAREN_e_RPAREN_");
		res.add("R_EX_cys_L_LPAREN_e_RPAREN_");
		res.add("R_EX_pail_hs_e_");
		res.add("R_EX_pchol_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_pe_hs_LPAREN_e_RPAREN_");
		res.add("R_EX_ps_hs_LPAREN_e_RPAREN_");
		return res;
	}

	public Set<String> getDrainsPercursorBiomassHMR() {
		Set<String> res = new HashSet<String>();
		res.add("");

		return res;
	}
}
