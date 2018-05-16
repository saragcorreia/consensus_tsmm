package pt.uminho.ceb.biosystems.mew.omicsintegration.utils;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;

public class Medium {

	public static EnvironmentalConditions getCloseAllDrains(Container container) {
		EnvironmentalConditions env = new EnvironmentalConditions();

		for (String r : container.identifyDrains()) {
			if (!env.containsKey(r)) {
				env.put(r, new ReactionConstraint(0.0, 1000.0));
				// System.out.println(r);
			}
		}
		// System.out.println("drains" + container.identifyDrains().size());

		return env;
	}

	// medium used by Emanuel and Rupin
	public static EnvironmentalConditions getRPM1640Recon1(Container container, double factor) {
		EnvironmentalConditions env = getRPM1640Recon1(container);
		for (Map.Entry<String, ReactionConstraint> constraint : env.entrySet()) {
			double lb = constraint.getValue().getLowerLimit();
			double ub = constraint.getValue().getUpperLimit();
			if (ub == 0.0)
				constraint.getValue().setLowerLimit(lb * factor);
		}
		return env;
	}

	public static EnvironmentalConditions getRPM1640Recon1(Container container) {
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.put("R_EX_arg_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1.0, 0.0));
		env.put("R_EX_ala_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_asn_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_asp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_Lcystin_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_glu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_gly_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_his_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_ile_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_leu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_lys_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_met_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_phe_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_pro_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_ser_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_thr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_tyr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_val_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_trp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_inost_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_chol_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_fol_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_ncam_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_pydx_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_thm_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_btn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_pnto_R_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_ribflv_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_na1_LPAREN_e_RPAREN_", new ReactionConstraint(-100, 0.0));
		env.put("R_EX_cl_LPAREN_e_RPAREN_", new ReactionConstraint(-100, 0.0));
		env.put("R_EX_ca2_LPAREN_e_RPAREN_", new ReactionConstraint(-1, 0.0));
		env.put("R_EX_k_LPAREN_e_RPAREN_", new ReactionConstraint(-1, 0.0));
		env.put("R_EX_pi_LPAREN_e_RPAREN_", new ReactionConstraint(-1, 0.0));
		env.put("R_EX_h2o_LPAREN_e_RPAREN_", new ReactionConstraint(-1, 0.0));
		env.put("R_EX_cmp_LPAREN_e_RPAREN_", new ReactionConstraint(-0.000001, 0.0));
		env.put("R_EX_ump_LPAREN_e_RPAREN_", new ReactionConstraint(-0.000001, 0.0));
		env.put("R_EX_ade_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_amp_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_gmp_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_imp_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_cytd_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_dcyt_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_duri_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_thymd_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_ura_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_uri_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_urate_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_lnlc_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_hdca_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_hdcea_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_triodthy_LPAREN_e_RPAREN_", new ReactionConstraint(-0.000001, 0.0));
		env.put("R_EX_thyox_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_thym_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_tchola_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_gthox_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_gchola_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_4pyrdx_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_4abut_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_EX_acac_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_akg_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_crn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_lcts_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_srtn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_sucr_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_EX_bilirub_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_cit_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_glyc_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_hxan_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_oxa_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_taur_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_creat_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_hom_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_orn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_succ_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_lac_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1, 0.0));
		env.put("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-10, 0.0));
		env.put("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(-100, 0.0));
		env.put("R_EX_gln_L_LPAREN_e_RPAREN_", new ReactionConstraint(-2, 0.0));
		env.put("R_EX_gthrd_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_Tyr_ggn_LPAREN_e_RPAREN_", new ReactionConstraint(-1, 0.0));

		// System.out.println("env:" + env.size());
		for (String r : container.identifyDrains()) {
			if (!env.containsKey(r)) {
				env.put(r, new ReactionConstraint(0.0, Config.UPPER_BOUND));
			
			}
		}
		// System.out.println("drains" + container.identifyDrains().size());

		return env;
	}

	public static EnvironmentalConditions getFolgerMedRecon1(Container container, double factor) {
		EnvironmentalConditions env = getFolgerMedRecon1(container);
		for (Map.Entry<String, ReactionConstraint> constraint : env.entrySet()) {
			double lb = constraint.getValue().getLowerLimit();
			double ub = constraint.getValue().getUpperLimit();
			if (ub == 0.0)
				constraint.getValue().setLowerLimit(lb * factor);
		}
		return env;
	}

	public static EnvironmentalConditions getFolgerMedRecon1(Container container) {
		EnvironmentalConditions env = new EnvironmentalConditions();

		env.put("R_EX_gln_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.5, 0.0));
		env.put("R_EX_gthrd_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-5, 0.0));
		env.put("R_EX_pi_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_cl_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_na1_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_k_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_fe2_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ca2_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_thm_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_ribflv_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_pydx_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_ncam_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_inost_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_fol_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_pnto_R_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_chol_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_btn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_ala_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_arg_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_asn_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_asp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_Lcystin_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_glu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_gly_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_his_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_ile_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_leu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_lys_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_met_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_phe_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_cys_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_ser_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_thr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_trp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_tyr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
		env.put("R_EX_val_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.05, 0.0));
//		env.put("R_UP_Tyr_DASH_ggn_LSQBKT_c_RSQBKT_", new ReactionConstraint(-1000, 0.0)); // U251 PRIME model growth simulation
		// System.out.println("env:" + env.size());
		for (String r : container.identifyDrains()) {
			if (!env.containsKey(r)) {
				env.put(r, new ReactionConstraint(0.0, Config.UPPER_BOUND));
				// System.out.println(r);
			
			} 
		}
		// System.out.println("drains" + container.identifyDrains().size());

		return env;
	}
	
	public static EnvironmentalConditions getJain2012Uptake(Container container) {
		EnvironmentalConditions env = Medium.getFolgerMedRecon1(container);
		env.put("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-258.4175, 0.0));
		env.put("R_EX_gln_L_LPAREN_e_RPAREN_", new ReactionConstraint(-54.487, 0.0));
		env.put("R_EX_ser_L_LPAREN_e_RPAREN_", new ReactionConstraint(-8.3925, 0.0));
		env.put("R_EX_lys_L_LPAREN_e_RPAREN_", new ReactionConstraint(-6.3915, 0.0));
		env.put("R_EX_leu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-6.2205, 0.0));
		env.put("R_EX_arg_L_LPAREN_e_RPAREN_", new ReactionConstraint(-5.005, 0.0));
		env.put("R_EX_ile_L_LPAREN_e_RPAREN_", new ReactionConstraint(-4.8595, 0.0));
		env.put("R_EX_val_L_LPAREN_e_RPAREN_", new ReactionConstraint(-4.578, 0.0));
		env.put("R_EX_thr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-3.758, 0.0));
		env.put("R_EX_gly_LPAREN_e_RPAREN_", new ReactionConstraint(-3.129, 0.0));
		env.put("R_EX_asp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-2.865, 0.0));
		env.put("R_EX_tyr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-2.6215, 0.0));
		env.put("R_EX_phe_L_LPAREN_e_RPAREN_", new ReactionConstraint(-2.109, 0.0));
		env.put("R_EX_met_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1.861, 0.0));
		env.put("R_EX_asn_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1.6555, 0.0));
		env.put("R_EX_hom_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1.142, 0.0));
		env.put("R_EX_chol_LPAREN_e_RPAREN_", new ReactionConstraint(-0.7625, 0.0));
		env.put("R_EX_trp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-0.568, 0.0));
		env.put("R_EX_creat_LPAREN_e_RPAREN_", new ReactionConstraint(-0.448, 0.0));
		env.put("R_EX_orn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.177, 0.0));
		env.put("R_EX_taur_LPAREN_e_RPAREN_", new ReactionConstraint(-0.1525, 0.0));
		env.put("R_EX_ncam_LPAREN_e_RPAREN_", new ReactionConstraint(-0.079, 0.0));
		env.put("R_EX_hxan_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0435, 0.0));
		env.put("R_EX_ura_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0205, 0.0));
		env.put("R_EX_srtn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0155, 0.0));
		env.put("R_EX_crn_LPAREN_e_RPAREN_", new ReactionConstraint(-0.011, 0.0));
		env.put("R_EX_oxa_LPAREN_e_RPAREN_", new ReactionConstraint(-0.01, 0.0));
		env.put("R_EX_urate_LPAREN_e_RPAREN_", new ReactionConstraint(-0.009, 0.0));
		env.put("R_EX_uri_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0075, 0.0));
		env.put("R_EX_pnto_R_LPAREN_e_RPAREN_", new ReactionConstraint(-0.005, 0.0));
		env.put("R_EX_acac_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0025, 0.0));
		env.put("R_EX_fol_LPAREN_e_RPAREN_", new ReactionConstraint(-0.0025, 0.0));
		env.put("R_EX_duri_LPAREN_e_RPAREN_", new ReactionConstraint(-0.002, 0.0));
		env.put("R_EX_cytd_LPAREN_e_RPAREN_", new ReactionConstraint(-0.002, 0.0));
		env.put("R_EX_gdchola_LPAREN_e_RPAREN_", new ReactionConstraint(-0.001, 0.0));
		env.put("R_EX_thym_LPAREN_e_RPAREN_", new ReactionConstraint(-0.00050, 0.0));

		env.put("R_EX_amp_LPAREN_e_RPAREN_", new ReactionConstraint(0.0001,1000));
//		env.put("R_EX_gmp_LPAREN_e_RPAREN_", new ReactionConstraint(0.0001,1000));
//		env.put("R_EX_thymd_LPAREN_e_RPAREN_", new ReactionConstraint(0.0001,1000));
		env.put("R_EX_lcts_LPAREN_e_RPAREN_", new ReactionConstraint(0.00015,1000));
//		env.put("R_EX_ump_LPAREN_e_RPAREN_", new ReactionConstraint(0.00015,1000));
		env.put("R_EX_sucr_LPAREN_e_RPAREN_", new ReactionConstraint(0.0003,1000));
		env.put("R_EX_gthox_LPAREN_e_RPAREN_", new ReactionConstraint(0.00035,1000));
//		env.put("R_EX_ppa_LPAREN_e_RPAREN_", new ReactionConstraint(0.00045,1000));
		env.put("R_EX_thm_LPAREN_e_RPAREN_", new ReactionConstraint(0.0013,1000));
		env.put("R_EX_glyb_LPAREN_e_RPAREN_", new ReactionConstraint(0.00275,1000));
		env.put("R_EX_dcyt_LPAREN_e_RPAREN_", new ReactionConstraint(0.0036,1000));
		env.put("R_EX_bilirub_LPAREN_e_RPAREN_", new ReactionConstraint(0.0042,1000));
		env.put("R_EX_succ_LPAREN_e_RPAREN_", new ReactionConstraint(0.006,1000));
		env.put("R_EX_4abut_LPAREN_e_RPAREN_", new ReactionConstraint(0.0095,1000));
		env.put("R_EX_glyc_LPAREN_e_RPAREN_", new ReactionConstraint(0.0298,1000));
		env.put("R_EX_cit_LPAREN_e_RPAREN_", new ReactionConstraint(0.07345,1000));
		env.put("R_EX_pro_L_LPAREN_e_RPAREN_", new ReactionConstraint(0.246,1000));
		
//		env.put("R_EX_glu_L_LPAREN_e_RPAREN_", new ReactionConstraint(0.70775,1000));
		env.put("R_EX_ala_L_LPAREN_e_RPAREN_", new ReactionConstraint(1.30265,1000));
		env.put("R_EX_lac_L_LPAREN_e_RPAREN_", new ReactionConstraint(29.4191,1000));
		
		return env;
	}
	
	public static EnvironmentalConditions getFolgerMedRecon1MC(Container container) {
		EnvironmentalConditions env = new EnvironmentalConditions();


		env.put("R_EX_gln_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_gthrd_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-5, 0.0));
		env.put("R_EX_pi_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_cl_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_na1_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_k_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_fe2_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ca2_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_thm_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ribflv_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_pydx_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ncam_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_inost_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_fol_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_pnto_R_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_chol_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_btn_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ala_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_arg_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_asn_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_asp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_Lcystin_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_glu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_gly_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_his_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ile_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_leu_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_lys_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_met_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_phe_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_cys_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_ser_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_thr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_trp_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_tyr_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		env.put("R_EX_val_L_LPAREN_e_RPAREN_", new ReactionConstraint(-1000, 0.0));
		// System.out.println("env:" + env.size());
		for (String r : container.identifyDrains()) {
			if (!env.containsKey(r)) {
				env.put(r, new ReactionConstraint(0.0, Config.UPPER_BOUND));
				// System.out.println(r);
			
			} 
		}
		// System.out.println("drains" + container.identifyDrains().size());

		return env;
	}


	public static EnvironmentalConditions getRPM1640HMR2(Container container) {
		EnvironmentalConditions env = new EnvironmentalConditions();

		env.put("R_HMR_9091", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9228", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9132", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9253", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9259", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9061", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9262", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9066", new ReactionConstraint(-1.0, 0.0));
		env.put("R_HMR_9062", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9070", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9273", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9109", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9082", new ReactionConstraint(-1, 0.0));
		env.put("R_HMR_9083", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9286", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9150", new ReactionConstraint(-100, 0.0));
		env.put("R_HMR_9287", new ReactionConstraint(-0.000001, 0.0));
		env.put("R_HMR_9290", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9292", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9295", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9296", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9310", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9146", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9281", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9034", new ReactionConstraint(-10, 0.0));
		env.put("R_HMR_9063", new ReactionConstraint(-2, 0.0));
		env.put("R_HMR_9071", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9067", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9085", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9343", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9350", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9351", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9047", new ReactionConstraint(-1, 0.0));
		env.put("R_HMR_9038", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9161", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9358", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9039", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9360", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9361", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9081", new ReactionConstraint(-1, 0.0));
		env.put("R_HMR_9135", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9102", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9363", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9040", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9035", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9041", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9042", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9077", new ReactionConstraint(-100, 0.0));
		env.put("R_HMR_9378", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9048", new ReactionConstraint(-100, 0.0));
		env.put("R_HMR_9087", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9165", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9043", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9072", new ReactionConstraint(-1, 0.0));
		env.put("R_HMR_9145", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9068", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9400", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9143", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9069", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9412", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9415", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9416", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9418", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9282", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9159", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9044", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9422", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9423", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9424", new ReactionConstraint(-0.00001, 0.0));
		env.put("R_HMR_9427", new ReactionConstraint(-0.000001, 0.0));
		env.put("R_HMR_9045", new ReactionConstraint(-0.01, 0.0));
		env.put("R_HMR_9064", new ReactionConstraint(-0.1, 0.0));
		env.put("R_HMR_9436", new ReactionConstraint(-0.000001, 0.0));
		env.put("R_HMR_9437", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9075", new ReactionConstraint(-0.001, 0.0));
		env.put("R_HMR_9439", new ReactionConstraint(-0.0001, 0.0));
		env.put("R_HMR_9046", new ReactionConstraint(-0.1, 0.0));

		// System.out.println("env:" + env.size());
		for (String r : container.identifyDrains()) {
			if (!env.containsKey(r)) {
				env.put(r, new ReactionConstraint(0.0, Config.UPPER_BOUND));
				// System.out.println(r);

			}
		}

		// System.out.println("drains" + container.identifyDrains().size());

		return env;
	}

	public static EnvironmentalConditions getFolgerMedHMR2(Container container) {
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.put("R_HMR_9061", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9066", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9062", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9070", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9109", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9082", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9083", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9150", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9065", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9076", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9146", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9034", new ReactionConstraint(-5, 0.0));
		env.put("R_HMR_9063", new ReactionConstraint(-0.5, 0.0));
		env.put("R_HMR_9071", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9067", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9351", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9038", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9039", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9361", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9081", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9363", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9040", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9041", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9042", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9077", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9378", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9048", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9043", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9072", new ReactionConstraint(-1000, 0.0));
		env.put("R_HMR_9145", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9400", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9143", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9069", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9159", new ReactionConstraint(-0.005, 0.0));
		env.put("R_HMR_9044", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9045", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9064", new ReactionConstraint(-0.05, 0.0));
		env.put("R_HMR_9046", new ReactionConstraint(-0.05, 0.0));
		// System.out.println("env:" + env.size());

		for (String r : container.identifyDrains()) {
			if (!env.containsKey(r)) {
				env.put(r, new ReactionConstraint(0.0, Config.UPPER_BOUND));
			} 
		}
		// System.out.println("drains" + container.identifyDrains().size());

		return env;
	}
}
