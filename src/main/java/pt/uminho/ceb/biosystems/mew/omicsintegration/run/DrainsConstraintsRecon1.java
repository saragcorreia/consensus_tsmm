package pt.uminho.ceb.biosystems.mew.omicsintegration.run;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;

public class DrainsConstraintsRecon1 {

	public static EnvironmentalConditions envConditionForRPMImedium(Set<String> drains) {
		EnvironmentalConditions environmentalConditions = new EnvironmentalConditions("RPMI medium");

		double blim = 0.0;
		double ulim = 1000.0;
		for (String d : drains) {
			environmentalConditions.addReactionConstraint(d, new ReactionConstraint(blim, ulim));
		}

		// Shlomi-Molecular crowding: The growth medium is defined via an upper
		// bound on the glucose uptake
		// exchange reaction (as the carbon source) and by allowing an unlimited
		// uptake flux of oxygen, sodium, potassium, calcium, iron, chlorine,
		// phosphate, sulfate and ammonia (based on the RPMI- 1640 medium
		// definition; as none of these substances can be used as a carbon
		// source). Growth yield (growth rate divided by the glucose uptake
		// rate), oxygen uptake and lactate secretion rates were computed under
		// a wide range of glucose uptake rates (varying from 0 to 1.55
		// umol/mgDW/h, the uptake achieving maximal growth rate)

		blim = -1000.0;
		ulim = 1000.0;
		environmentalConditions.addReactionConstraint("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(blim, 0));
		environmentalConditions.addReactionConstraint("R_EX_na1_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_k_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ca2_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fe2_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cl_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_so4_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pi_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nh4_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));

		environmentalConditions.addReactionConstraint("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-0.5, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lac_L_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lac_D_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, ulim));

		// END RPMI JO�O

		// EMANUEL RPMI

		// blim = -0.05;
		// ulim = 1000.0;
		// environmentalConditions
		// .addReactionConstraint("R_EX_ala_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_arg_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_asn_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_asp_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_Lcystin_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim,
		// ulim));
		//
		// environmentalConditions
		// .addReactionConstraint("R_EX_glu_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_his_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_met_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_phe_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		//
		// environmentalConditions.addReactionConstraint("R_EX_gly_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		//
		// environmentalConditions
		// .addReactionConstraint("R_EX_pro_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_ser_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_lys_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_trp_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_tyr_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		//
		// environmentalConditions
		// .addReactionConstraint("R_EX_ile_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_leu_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		//
		// environmentalConditions
		// .addReactionConstraint("R_EX_thr_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_val_L_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		//
		// environmentalConditions.addReactionConstraint("R_EX_btn_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_chol_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_pnto_R_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_fol_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_inost_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_ncam_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_pydx_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_ribflv_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_thm_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions
		// .addReactionConstraint("R_EX_gthrd_LPAREN_e_RPAREN_", new
		// ReactionConstraint(blim, ulim));
		//
		// environmentalConditions.addReactionConstraint("R_EX_gln_L_LPAREN_e_RPAREN_",
		// new ReactionConstraint(0, ulim));
		//
		// blim = -5.0;
		// ulim =0
		// environmentalConditions.addReactionConstraint("R_EX_ca2_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_fe2_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_k_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_na1_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_cl_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_pi_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		//
		// environmentalConditions.addReactionConstraint("R_EX_glc_LPAREN_e_RPAREN_",
		// new ReactionConstraint(blim, ulim));
		// environmentalConditions.addReactionConstraint("R_EX_o2_LPAREN_e_RPAREN_",
		// new ReactionConstraint(-10.0, ulim));

		// See supplementary information
		// "We therefore investigated the robustness of our model by accounting for a constant ATP maintenance requirement (via an ATP hydrolysis reaction constrained to have a minimal flux of 1.0625 mmol/gDW/h [10])"
		// environmentalConditions.addReactionConstraint("R_OPAHir", new
		// ReactionConstraint(1.0625,1000.0));

		// environmentalConditions.addReactionConstraint("R_ADK1m_bwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_ADK3m_fwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_AKGDm", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_ARTFR61", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_ARTPLM1_fwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_HDD2COAtx_bwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_L_LACt2r_bwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_PMTCOAtx_fwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_RTOT6", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_ACOAO7p", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_SUCD1m_fwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_RTOT1", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_SUCOAS1m_bwd", new
		// ReactionConstraint(0.0, 0.0));
		// environmentalConditions.addReactionConstraint("R_EX_pyr_LPAREN_e_RPAREN_",
		// new ReactionConstraint(0.0, 0.0));

		return environmentalConditions;
	}
}
