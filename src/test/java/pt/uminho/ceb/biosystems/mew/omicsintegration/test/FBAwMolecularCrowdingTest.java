package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods.FBAwMolecularCrowding;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods.FBAwMolecularCrowdingFVA;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;



public class FBAwMolecularCrowdingTest {
	
	public static Map<String, Double> readCSVFile(String csvFile,boolean useMedian, double median){
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		int counter=0;
	 
		try {
			System.out.print("Reading file "+csvFile+"...");
	 
			Map<String, Double> maps = new HashMap<String, Double>();
	 
			br = new BufferedReader(new FileReader(csvFile));
			
			//headers
			br.readLine(); br.readLine();
			
			while ((line = br.readLine()) != null) {	 
				String[] lineSplit = line.split(csvSplitBy);				
				
				if(lineSplit.length == 5 && !lineSplit[4].contains("NaN")){
					maps.put("R_"+lineSplit[1], Double.parseDouble(lineSplit[4]));
					counter++;
				}	 
				else{
					if(useMedian){
						maps.put("R_"+lineSplit[1], (double) median); //Median value
					}
				}
			}
			
			System.out.println("done!");			
		
			return maps;
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
		
	}
	
	public static EnvironmentalConditions envConditionForRPMImedium(){
		EnvironmentalConditions environmentalConditions = new EnvironmentalConditions("RPMI medium");
		
		double blim = 0.0;
		double ulim = 1000.0;
		//constrain carbon source
		environmentalConditions.addReactionConstraint("R_EX_hista_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_h_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_whtststerone_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_elaid_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_aflatoxin_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucgalgbside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fuc13galacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ser_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gd1b2_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ile_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_mthgxl_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_whhdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cgly_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_btn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_phe_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pglyc_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ocdcea_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_for_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thm_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_s2l2n2m2masn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lcts_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ksii_core4_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_oagd3_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tchola_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dgchol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_oh1",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glyald_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_bildglcur_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ppa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cytd_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_arg_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gthox_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xyl_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_oxa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hexc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5dhf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_abt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dcsptn1_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ha_pre1_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4abut_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glyb_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cala_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retfa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pheacgln_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xylt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_rib_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lgnc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_but_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_duri_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sucr_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucacngalacglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pydx_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nad_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nac_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cca_d3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_perillyl_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dcmp_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_met_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_triodthy_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_estradiolglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gt1a_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dcyt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_2425dhvitd2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sphs1p_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gq1balpha_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_andrstrn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_whttdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_13_cis_retnglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_prostgf2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tsul_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_strdnc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thmtp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_urea_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ala_B_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acngalacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xan_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_prostge1_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pheme_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_utp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glcur_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_galfuc12gal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_bilglcur_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gudac_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lnlc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ksi_deg1_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_quln_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_yvite_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acald_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tmndnc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xolest_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thyox_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cys_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_strch2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sbt_DASH_D_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pchol_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retpalm_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_co2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cmp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acnacngalgbside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucfuc12gal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_man_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_adp",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pydam_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_10fthf6glu_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ribflv_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hco3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_adrnl_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_co_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_biocyt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_adrn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tetpent6_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ump_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_bz_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_rbt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cyst_DASH_L_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_11_cis_retfa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xtsn_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5adtststerones_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_din_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gp1c_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gam_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_carveol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_avite2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4hdebrisoquine_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucacngal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_taxol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cholate_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_chtn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glu_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_Tyr_ggn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nifedipine_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5hoxindoa_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_spmd_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_vitd3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ahcys_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tststerones_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gal_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_2hb_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4pyrdx_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tre_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ahandrostanglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_digalsgalside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_urate_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gd1c_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_CLPND_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_Lcystin_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_s2l2fn2m2masn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_htaxol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_crn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4hbz_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_omeprazole_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_3mlda_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dmantipyrine_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_prostge2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tdchola_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fe3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gchola_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_udp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_paf_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_val_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucfucgalacglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hcoumarin_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_bvite_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acetone_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fuc14galacglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acn23acngalgbside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_inost_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_spc_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_2pg_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_crtn_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_6htststerone_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ade_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_onpthl_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ebastineoh_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gsn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ser_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5adtststerone_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_taur_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_aldstrn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ps_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_srtn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_2mcit_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lipoate_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xoltri24_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gly_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cholp_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fum_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xolest2_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pro_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dad_5_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucfucfucgalacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_3aib_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4hphac_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gdchola_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_octa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_L2aadp_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_no_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gbside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cspg_d_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_10fthf7glu_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_aprgstrn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_24nph_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_h2o2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_10fthf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ksi_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_estroneglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_Rtotal2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_anth_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4nphsf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_bhb_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_leuktrC4_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gtp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ascb_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_h2o_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dhdascb_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tagat_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tststeroneglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_arab_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cspg_e_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_arachd_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_i_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_apnnox_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_caro_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hpdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nadp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cspg_c_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thr_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucgalfucgalacglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gln_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_npthl_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_vitd2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_galgalgalthcrm_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lnlncg_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_asp_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ala_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tag_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dopasf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dlnlcg_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ach_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5adtststeroneglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_34dhphe_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_7thf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dopa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_appnn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_adprib_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gmp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_icit_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_eicostet_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_phyQ_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_n2m2nmasn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tymsf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5thf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_asp_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xoltri27_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lpchol_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tetpent3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dgsn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_orn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_6thf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_vacc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dmhptcrn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_3hanthrn_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_aqcobal_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_creat_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ha_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sl_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hspg_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_chsterol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_uri_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_leu_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_camp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_triodthysuf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ins_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_6dhf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dag_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_o2s_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_galacglcgalgbside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ocdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_peplys_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_citr_DASH_L_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5fthf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_txa2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retinol_9_cis_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_estrones_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tcynt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4mtolbutamide_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ttdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_debrisoquine_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_drib_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_35cgmp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dhap_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gp1calpha_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_2425dhvitd3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_imp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ebastine_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acn13acngalgbside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_whddca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_adprbp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4hpro_DASH_LT_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_meoh_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ak2lgchol_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_malttr_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acnacngal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hdcea_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_1mncam_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_34dhoxpeg_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acgalfucgalacgalfucgalacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_1glyc_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glyc_S_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_25hvitd3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_crvnc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_etoh_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_prostgd2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pydxn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5mthf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucacgalfucgalacglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thymd_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_mal_DASH_L_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_adn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retinol_cis_11_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xoltri25_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_25hvitd2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_4nph_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lys_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glygn5_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ncam_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_kynate_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pep_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tyr_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_orot_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_succ_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_g3pc_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_atp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_10fthf5glu_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sel_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_arach_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_coumarin_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_chol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_asn_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_udpglcur_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_Lkynr_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lnlnca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thmmp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_xmp_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pnto_R_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glyc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_3aib_D_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pro_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hxan_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gua_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_7dhf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_phpyr_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cspg_b_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gdp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucfuc132galacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cspg_a_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_limnen_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fru_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tettet6_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_3pg_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retpalm",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_antipyrene_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ptdca_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fuc_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ala_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_phyt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_eaflatoxin_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ac_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gthrd_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5homeprazole_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_akg_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_Rtotal_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dhf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_csn_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dmgly_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ethamp_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_galfucgalacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_5htrp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_malt_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dheas_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ksii_core2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fucfucfucgalacglc13galacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acac_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hcys_DASH_L_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_lneldc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_estriolglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_mercplaccys_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glygn2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_strch1_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cit_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gq1b_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_his_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hestratriol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_andrstrnglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_thym_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tolbutamide_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cyan_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acgam_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_glygn4_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_hom_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_Rtotal3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_crmp_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nrvnc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sph1p_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sprm_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_oagt3_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_trp_L_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pe_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nrpphr_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_galgalfucfucgalacglcgalacglcgal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_carn_e_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retinol_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ura_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_idp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_mepi_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_tethex3_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_sarcs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_9_cis_retfa_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_bilirub_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_retnglc_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_dad_2_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_amp_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_mag_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_avite1_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_gluala_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nrpphrsf_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_acgalfucgalacgalfuc12gal14acglcgalgluside_hs_LPAREN_e_RPAREN_",new ReactionConstraint(blim, ulim));		
		
		blim = 0.0;
		ulim = 1000.0;
		environmentalConditions.addReactionConstraint("R_DM_avite1_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_Asn_X_Ser_FSLASH_Thr_LPAREN_ly_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_sprm_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_avite2_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_datp_LPAREN_m_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dttp_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_13_cis_oretn_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_gpi_sig_LPAREN_er_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dttp_LPAREN_m_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_kdn_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_bvite_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_ethamp_LPAREN_r_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dgtp_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dgtp_LPAREN_m_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_13_cis_retn_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_Ser_FSLASH_Thr_LPAREN_ly_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dsT_antigen_LPAREN_g_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_oretn_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_yvite_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dctp_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_hretn_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_sTn_antigen_LPAREN_g_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_datp_LPAREN_n_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_melanin_c",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_Ser_Gly_FSLASH_Ala_X_Gly_LPAREN_ly_RPAREN_",new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_DM_dctp_LPAREN_m_RPAREN_",new ReactionConstraint(blim, ulim));
		
		
//		blim = 0.0;
//		ulim = 1000.0;
//		environmentalConditions.addReactionConstraint("R_EX_o2_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_na1_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_k_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_ca2_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_fe2_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_cl_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_so4_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_pi_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//		environmentalConditions.addReactionConstraint("R_EX_nh4_LPAREN_e_RPAREN__fwd", new ReactionConstraint(blim, ulim));
//
//		blim = 0.0;
//		ulim = 1000.0;
//		environmentalConditions.addReactionConstraint("R_EX_o2_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));				
//		environmentalConditions.addReactionConstraint("R_EX_na1_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));		
//		environmentalConditions.addReactionConstraint("R_EX_k_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));		
//		environmentalConditions.addReactionConstraint("R_EX_ca2_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));		
//		environmentalConditions.addReactionConstraint("R_EX_fe2_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));			
//		environmentalConditions.addReactionConstraint("R_EX_cl_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));			
//		environmentalConditions.addReactionConstraint("R_EX_so4_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));		
//		environmentalConditions.addReactionConstraint("R_EX_pi_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));		
//		environmentalConditions.addReactionConstraint("R_EX_nh4_LPAREN_e_RPAREN__bwd", new ReactionConstraint(blim, ulim));

		
		//U251
		blim =-1000.0;
		ulim = 1000.0;
		environmentalConditions.addReactionConstraint("R_EX_o2_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_na1_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_k_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_ca2_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_fe2_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_cl_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_so4_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_pi_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		environmentalConditions.addReactionConstraint("R_EX_nh4_LPAREN_e_RPAREN_", new ReactionConstraint(blim, ulim));
		
		


		// See supplementary information "We therefore investigated the robustness of our model by accounting for a constant ATP maintenance requirement (via an ATP hydrolysis reaction constrained to have a minimal flux of 1.0625 mmol/gDW/h [10])"
		//environmentalConditions.addReactionConstraint("R_OPAHir", new ReactionConstraint(1.0625,1000.0));		
		
		//environmentalConditions.addReactionConstraint("R_ADK1m_bwd", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_ADK3m_fwd", new ReactionConstraint(0.0, 0.0));		
		//environmentalConditions.addReactionConstraint("R_AKGDm", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_ARTFR61", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_ARTPLM1_fwd", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_HDD2COAtx_bwd", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_L_LACt2r_bwd", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_PMTCOAtx_fwd", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_RTOT6", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_ACOAO7p", new ReactionConstraint(0.0, 0.0));
		environmentalConditions.addReactionConstraint("R_SUCD1m_fwd", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_RTOT1", new ReactionConstraint(0.0, 0.0));
		//environmentalConditions.addReactionConstraint("R_SUCOAS1m_bwd", new ReactionConstraint(0.0, 0.0));
		environmentalConditions.addReactionConstraint("R_EX_pyr_LPAREN_e_RPAREN_",new ReactionConstraint(0, 0.0));		

		return environmentalConditions;
	}

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSBMLReader reader;
		String biomass = "biomass = c(", gcl="glucose =c(", ox = "oxigen =c(", 
				lact = "lactate=c(", glycolisis="glycolisis =c(", oxidative = "oxidative = c(";
		try {
			reader = new JSBMLReader("/Users/Sara/Documents/Projects/PHD_P05_U251/MolecularCrowding/ConsensusModel_noRev.xml", "human", false);
//			reader = new JSBMLReader("/Users/Sara/Documents/Projects/PHD_P02_Simul_Enzime_Concent_Constraint/Recon1_with_drains_no_rev.xml", "human", false);

			Container cont = new Container(reader);
			 cont.removeMetabolites(cont.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")));			
			 cont.setBiomassId("R_biomass_reaction");
			
			//Read input files
			Map<String, Double> molecularWeightsMap = readCSVFile("/Users/Sara/Documents/Projects/PHD_P02_Simul_Enzime_Concent_Constraint/human_reactions_molecular_weights.csv", false, 85.3);
			Map<String, Double> turnoverNumberMap = readCSVFile("/Users/Sara/Documents/Projects/PHD_P02_Simul_Enzime_Concent_Constraint/human_reactions_turnover_number.csv", true, 90000.0);
			float metabolic_genes_concentration = (float) 0.078;			
			
			double[]  glc_conc_range = new double[] {0.002, 0.004, 0.006, 0.008, 0.01, 0.012, 0.014, 0.016, 0.018, 0.02, 0.022, 0.024, 0.026, 0.028, 0.03, 0.032, 0.034, 0.036, 0.038, 0.04, 0.042, 0.044, 0.046, 0.048, 0.05, 0.052, 0.054, 0.056, 0.058, 0.06, 0.062, 0.064, 0.066, 0.068, 0.07, 0.072, 0.074, 0.076, 0.078, 0.08, 0.082, 0.084, 0.086, 0.088, 0.09, 0.092, 0.094, 0.096, 0.098, 0.1, 0.102, 0.104, 0.106, 0.108, 0.11, 0.112, 0.114, 0.116, 0.118, 0.12, 0.122, 0.124, 0.126, 0.128, 0.13, 0.132, 0.134, 0.136, 0.138, 0.14};
//			double[]  glc_conc_range = new double[] {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2,1.3,1.4,1.5};
		
			
			ISteadyStateModel model = ContainerConverter.convert(cont);
			FBAwMolecularCrowding fba = new FBAwMolecularCrowding(model,molecularWeightsMap,turnoverNumberMap,metabolic_genes_concentration);
			fba.setIsMaximization(true);
			fba.setSolverType(SolverType.CPLEX3);	
			
			
			for(int i=0; i<glc_conc_range.length; i++){
				double glc_conc = glc_conc_range[i] * - 1 ;
								
				System.out.println("Running simulation with molecular crowding...");
				
				System.out.println("GLUCOSE: "+glc_conc);
				
				//Configure environmental conditions (RPMI medium)			
				EnvironmentalConditions environmentalConditions = Medium.getFolgerMedRecon1MC(cont);
				
//				EnvironmentalConditions environmentalConditions = envConditionForRPMImedium();
				
//				environmentalConditions.addReactionConstraint("R_EX_glc_LPAREN_e_RPAREN__bwd", new ReactionConstraint(0.0, glc_conc)); //Glucose
				environmentalConditions.addReactionConstraint("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(glc_conc,0.0)); //Glucose							
				environmentalConditions.addReactionConstraint("R_EX_lac_L_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, 1000.0));			
				environmentalConditions.addReactionConstraint("R_EX_lac_D_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, 1000.0));
				
				//extra necessary to have lactate
				environmentalConditions.addReactionConstraint("R_SUCD1m_fwd", new ReactionConstraint(0.0, 0.0));
				environmentalConditions.addReactionConstraint("R_EX_pyr_LPAREN_e_RPAREN_",new ReactionConstraint(0, 0.0));
				environmentalConditions.addReactionConstraint("R_EX_glu_L_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, 1000.0));
				environmentalConditions.addReactionConstraint("R_EX_gly_LPAREN_e_RPAREN_", new ReactionConstraint(0.0, 1000.0));
				environmentalConditions.addReactionConstraint("R_EX_gln_L_LPAREN_e_RPAREN_", new ReactionConstraint(0, 1000.0));
				environmentalConditions.addReactionConstraint("R_EX_gthrd_LPAREN_e_RPAREN_", new ReactionConstraint(0, 1000.0));
				
				
				fba.setEnvironmentalConditions(environmentalConditions);
				SteadyStateSimulationResult r = fba.simulate();				
				System.out.println( r.getOFvalue());
				
				System.out.println("Results:");
//				for(Map.Entry<String, Double> par : r.getFluxValues().entrySet()){
//					if(par.getValue()!=0.0 && cont.getDrains().contains(par.getKey()))
//					System.out.println(par.getKey() + " :"+ +par.getValue());
//				}
//				
//				
				
				//Fluxes
//				double gr = r.getOFvalue();
//				double gluc_flux = r.getFluxValues().get("R_EX_glc_LPAREN_e_RPAREN__bwd");
//				double gy = gr/gluc_flux;
//								
//				gcl = gcl + gluc_flux +",";
//				ox = ox +r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN__bwd") +",";
//				lact = lact +r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_") +",";
//				biomass = biomass + gr + ",";
//				
//				System.out.println("Glucose flux: "+gluc_flux);
//				System.out.println("Oxygen flux: "+r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN__bwd"));
//				System.out.println("L-Lactate flux: "+r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_"));			
//				
//				System.out.println("Oxygen flux (Norm): "+r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN__bwd")/gluc_flux);			
//				System.out.println("L-Lactate flux (Norm): "+r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_")/gluc_flux);				
//				System.out.println("Growth rate (1/h): " + gr);
//				System.out.println("Growth yield (gDW/mmol): "+ gy);			
//				
//				//calculate reactions activity in the glycolysis pathway 
//				HashMap<String, Double> res = getAllFluxesByFunction(cont,r,"Glycolysis/Gluconeogenesis");	
//				res.put("R_EX_lac_L_LPAREN_e_RPAREN_",r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_"));
//				double perc = 0.0;
//				for(double d : res.values()) { if(d != 0) { perc += Math.abs(d);} }
//				perc = perc / res.values().size();
//				glycolisis = glycolisis + perc + ",";
//				
//				System.out.println("Glycolysis/Gluconeogenesis pathway activity: " + perc);
//				
//				//calculate reactions activity in the oxidative phosphorylation pathway 
//				res = getAllFluxesByFunction(cont,r,"Oxidative Phosphorylation");				
//				res.put("R_EX_o2_LPAREN_e_RPAREN__bwd",r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN__bwd"));
//				perc = 0.0;
//				for(double d : res.values()) { if(d != 0) { perc += Math.abs(d);} }
//				perc = perc / res.values().size();				
//				oxidative = oxidative + perc + ",";
//				
//				System.out.println("Oxidative Phosphorylation pathway activity: " + perc);
				
				
				////////////////////////////////////////////////////////////////////////
				//U251 metabolic model
				//Fluxes
				double gr = r.getOFvalue();
				double gluc_flux = r.getFluxValues().get("R_EX_glc_LPAREN_e_RPAREN_");
				double gy = gr/gluc_flux;
								
				gcl = gcl + gluc_flux +",";
				ox = ox +r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN_") +",";
				lact = lact +r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_") +",";
				biomass = biomass + gr + ",";
				
				System.out.println("Glucose flux: "+gluc_flux);
				System.out.println("Oxygen flux: "+r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN_"));
				System.out.println("L-Lactate flux: "+r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_"));			
				
				System.out.println("Oxygen flux (Norm): "+r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN_")/gluc_flux);			
				System.out.println("L-Lactate flux (Norm): "+r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_")/gluc_flux);				
				System.out.println("Growth rate (1/h): " + gr);
				System.out.println("Growth yield (gDW/mmol): "+ gy);			
				
				//calculate reactions activity in the glycolysis pathway 
				HashMap<String, Double> res = getAllFluxesByFunction(cont,r,"Glycolysis/Gluconeogenesis");	
				res.put("R_EX_lac_L_LPAREN_e_RPAREN_",r.getFluxValues().get("R_EX_lac_L_LPAREN_e_RPAREN_"));
				double perc = 0.0;
				for(double d : res.values()) { if(d != 0) { perc += Math.abs(d);} }
				perc = perc / res.values().size();

				System.out.println("Glycolysis/Gluconeogenesis pathway activity: " + perc);
				glycolisis = glycolisis + perc + ",";
				
				//calculate reactions activity in the oxidative phosphorylation pathway 
				res = getAllFluxesByFunction(cont,r,"Oxidative Phosphorylation");				
				res.put("R_EX_o2_LPAREN_e_RPAREN__bwd",r.getFluxValues().get("R_EX_o2_LPAREN_e_RPAREN_"));
				perc = 0.0;
				for(double d : res.values()) { if(d != 0) { perc += Math.abs(d);} }
				perc = perc / res.values().size();				
				
				System.out.println("Oxidative Phosphorylation pathway activity: " + perc);
				oxidative = oxidative + perc + ",";
				
				

				
				
				//FVA
				FBAwMolecularCrowdingFVA fva = new FBAwMolecularCrowdingFVA(model, environmentalConditions, null, SolverType.CPLEX3,molecularWeightsMap,turnoverNumberMap,metabolic_genes_concentration);
				double[] fl_lim1 = fva.tightBounds("R_EX_o2_LPAREN_e_RPAREN_");
				double[] fl_lim2 = fva.tightBounds("R_EX_lac_L_LPAREN_e_RPAREN_");
				double[] fl_lim3 = fva.tightBounds("R_EX_glc_LPAREN_e_RPAREN_");
				
				System.out.println("\n##### FVA Analysis ##### " + glc_conc);
				System.out.println("FVA -> Oxygen (min,max): " + fl_lim1[0] + "\t" + fl_lim1[1]);
				System.out.println("FVA -> Lac-L  (min,max): " + fl_lim2[0] + "\t" + fl_lim2[1]);
				System.out.println("FVA -> GLuc.  (min,max): " + fl_lim3[0] + "\t" + fl_lim3[1]);
				System.out.println("##########################\n");
							
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(biomass);
		System.out.println(gcl);
		System.out.println(ox);
		System.out.println(lact);
		System.out.println(glycolisis);
		System.out.println(oxidative);
		
	}

	private static HashMap<String, Double> getAllFluxesByFunction(Container cont,
			SteadyStateSimulationResult r, String function_of_interest) {
		HashMap<String, Double> fluxesMap = new HashMap<String, Double>();
		
		for(int i=0; i < r.getModel().getNumberOfReactions(); i++)
		{
			Reaction react = r.getModel().getReaction(i);
			double fluxValue = r.getFluxValues().get(react.getId()); 
			
			if (cont.getReaction(react.getId()).getSubsystem().equals(function_of_interest))
			{			
				fluxesMap.put(react.getId(), fluxValue);		
			}			
		}
		
		return fluxesMap;
	}

}
