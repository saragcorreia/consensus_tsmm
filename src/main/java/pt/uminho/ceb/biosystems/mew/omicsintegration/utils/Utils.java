package pt.uminho.ceb.biosystems.mew.omicsintegration.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.fva.FBAFluxVariabilityAnalysis;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.omicsintegration.simplification.ContainerSimplification;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class Utils {

	public static void mergeDataMaps(List<IOmicsDataMap> omics, IOmicsDataMap mergeRes) {
		Map<String, List<Double>> values = new HashMap<String, List<Double>>();

		for (IOmicsDataMap omic : omics) {
			if (omic.getOmicType().equals(mergeRes.getOmicType())) {

				if (mergeRes.getCondition() == null) {
					mergeRes.setCondition(omic.getCondition());
				}

				// merge values
				for (Map.Entry<String, Double> entry : omic.getMapValues().entrySet()) {
					if (values.keySet().contains(entry.getKey())) {
						values.get(entry.getKey()).add(entry.getValue());
					} else {
						ArrayList<Double> list = new ArrayList<Double>();
						list.add(entry.getValue());
						values.put(entry.getKey(), list);
					}
				}
			}

		}
		// calculate the mean of Omics data
		for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
			double value = Double.NaN;
			for (double d : entry.getValue()) {
				if (d != Double.NaN)
					value = value + d;
			}
			value = value == Double.NaN ? Double.NaN : value / entry.getValue().size();
			mergeRes.setValue(entry.getKey(), value);
		}
	}

	public static <T> boolean isIntersectionEmpty(Collection<T> collection1, Collection<T> collection2) {
		for (T value : collection1) {
			if (collection2.contains(value))
				return false;
		}
		return true;
	}

	public static void writeBounds(String file, Pattern p, String fileResName) {
		try {

			Container container = new Container(new JSBMLReader(file, "human", false));
			if (p != null) {
				container.removeMetabolites(container.identifyMetabolitesIdByPattern(p));
			}
			Set<String> deadEnds = container.identifyDeadEnds(true);

			System.out.println("reac" + container.getReactions().size());
			System.out.println("metas" + container.getMetabolites().size());
			System.out.println("dead ends :" + deadEnds.size());

			try (BufferedWriter b = new BufferedWriter(new FileWriter(fileResName))) {
				ISteadyStateModel model = ContainerConverter.convert(container);

				for (Reaction r : model.getReactions().values()) {
					b.write(r.getId() + ";" + r.getConstraints().getLowerLimit() + ";"
							+ r.getConstraints().getUpperLimit());
					b.newLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// return the list of active fluxes for each reacs simulation (pFBA)
	public static Map<String, Set<String>> validateProduction(Container container, EnvironmentalConditions env,
			Set<String> reacs) throws Exception {
		Map<String, Set<String>> listReactions = new HashMap<String, Set<String>>();
		for (String r : reacs) {
			listReactions.put(r, new HashSet<String>());
			Map<String, Double> fluxes = Utils.simulate(container, r, env).getFluxValues();
			System.out.println(r + " flux model:" + fluxes.get(r));
			for (Map.Entry<String, Double> entry : fluxes.entrySet()) {
				if (entry.getValue() != 0.0)
					listReactions.get(r).add(entry.getKey());
			}
		}
		return null;

	}

	public static SteadyStateSimulationResult simulate(Container container, String biomassId,
			EnvironmentalConditions medium) throws Exception {

		ISteadyStateModel model = ContainerConverter.convert(container);
		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(medium, null, model,
				SimulationProperties.FBA);
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomassId, 1.0);

		SteadyStateSimulationResult res = cc.simulate();
		return res;
	}

	public static Set<String> readIdsFromFile(String file) {
		Set<String> result = new HashSet<String>();

		try {
			FileReader f = new FileReader(file);
			BufferedReader b = new BufferedReader(f);
			while (b.ready()) {
				String id = b.readLine();
				result.add(id);
			}
			b.close();
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Container getReducedModel(String sbmlOriginal, Set<String> reacs) throws Exception {

		Container container = new Container(new JSBMLReader(sbmlOriginal, "human", false));
		System.out.println("Reactions" + container.getReactions().size());
		System.out.println("Metabolites" + container.getMetabolites().size());
		System.out.println("Genes" + container.getGenes().size());

		Set<String> toRemove = new HashSet<String>();
		for (String r : container.getReactions().keySet()) {
			if (!reacs.contains(r))
				toRemove.add(r);
		}

		// for(String r: reacs){
		// if(!container.getReactions().keySet().contains(r))
		// System.out.println(r);
		// }

		System.out.println("Reactions to Remove: " + toRemove.size());
		container.removeReactions(toRemove);
		System.out.println("Reactions" + container.getReactions().size());
		container.verifyDepBetweenClass();
		System.out.println("--------------------");
		System.out.println("Reactions" + container.getReactions().size());
		System.out.println("Metabolites" + container.getMetabolites().size());
		System.out.println("Genes" + container.getGenes().size());
		return container;
	}

	public static void adaptContainer(Container container, double minCoef, double d) throws IOException {
		for (ReactionCI r : container.getReactions().values()) {
			adaptStoiq(r.getProducts(), minCoef, d);
			adaptStoiq(r.getReactants(), minCoef, d);
		}
		container.verifyDepBetweenClass();

	}

	private static void adaptStoiq(Map<String, StoichiometryValueCI> stoic, double minCoef, double d) {
		Set<String> toRemove = new HashSet<String>();
		for (StoichiometryValueCI s : stoic.values())
			if (s.getStoichiometryValue() < minCoef) {
				toRemove.add(s.getMetaboliteId());
			} else
				s.setStoichiometryValue(s.getStoichiometryValue() * d);

		stoic.keySet().removeAll(toRemove);
	}

	public static void mergeMetabolites() throws Exception {
		Container recon = new Container(new JSBMLReader(
				"/Users/Sara/Documents/Metabolic_Models/Human/Recon1_with_drains.xml", "Human", false, true));
		Container hmr = new Container(new JSBMLReader(
				"/Users/Sara/Documents/Metabolic_Models/Human/HMR2.0_with_drains.xml", "Human", false, true));
		Set<String> metaIds = new HashSet<String>();

		metaIds.add("M_L2aadp_c");
		metaIds.add("M_2pg_c");
		metaIds.add("M_dad_DASH_2_c");
		metaIds.add("M_dcyt_c");
		metaIds.add("M_duri_c");
		metaIds.add("M_3hanthrn_c");
		metaIds.add("M_3pg_c");
		metaIds.add("M_4hbz_m");
		metaIds.add("M_4pyrdx_c");
		metaIds.add("M_5hoxindoa_c");
		metaIds.add("M_ahcys_c");
		metaIds.add("M_acac_c");
		metaIds.add("M_ade_c");
		metaIds.add("M_adn_c");
		metaIds.add("M_ala_DASH_L_c");
		metaIds.add("M_g3pc_c");
		metaIds.add("M_akg_c");
		metaIds.add("M_amp_c");
		metaIds.add("M_anth_c");
		metaIds.add("M_arg_DASH_L_c");
		metaIds.add("M_asn_DASH_L_c");
		metaIds.add("M_asp_DASH_L_c");
		metaIds.add("M_glyb_c");
		metaIds.add("M_bilirub_c");
		metaIds.add("M_crn_c");
		metaIds.add("M_carn_c");
		metaIds.add("M_chol_c");
		metaIds.add("M_4hpro_DASH_LT_m");
		metaIds.add("M_cit_c");
		metaIds.add("M_citr_DASH_L_c");
		metaIds.add("M_cmp_c");
		metaIds.add("M_creat_c");
		metaIds.add("M_crtn_c");
		metaIds.add("M_cyst_DASH_L_c");
		metaIds.add("M_cytd_c");
		metaIds.add("M_dcmp_c");
		metaIds.add("M_dhap_c");
		metaIds.add("M_dmgly_c");
		metaIds.add("M_fol_c");
		metaIds.add("M_fum_c");
		metaIds.add("M_4abut_c");
		metaIds.add("M_glc_DASH_D_c");
		metaIds.add("M_glcur_c");
		metaIds.add("M_glu_DASH_L_c");
		metaIds.add("M_gln_DASH_L_c");
		metaIds.add("M_gthox_c");
		metaIds.add("M_glyald_c");
		metaIds.add("M_glyc_c");
		metaIds.add("M_gly_c");
		metaIds.add("M_gchola_c");
		metaIds.add("M_gdchola_c");
		metaIds.add("M_gmp_c");
		metaIds.add("M_gudac_c");
		metaIds.add("M_hcys_DASH_L_c");
		metaIds.add("M_hom_DASH_L_c");
		metaIds.add("M_hxan_c");
		metaIds.add("M_imp_c");
		metaIds.add("M_ins_c");
		metaIds.add("M_icit_c");
		metaIds.add("M_ile_DASH_L_c");
		metaIds.add("M_kynate_c");
		metaIds.add("M_Lkynr_c");
		metaIds.add("M_lac_DASH_L_c");
		metaIds.add("M_lcts_c");
		metaIds.add("M_leu_DASH_L_c");
		metaIds.add("M_lys_DASH_L_c");
		metaIds.add("M_mal_DASH_L_c");
		metaIds.add("M_met_DASH_L_c");
		metaIds.add("M_cala_c");
		metaIds.add("M_nac_c");
		metaIds.add("M_ncam_c");
		metaIds.add("M_phpyr_c");
		metaIds.add("M_omeprazole_c");
		metaIds.add("M_orn_c");
		metaIds.add("M_orot_c");
		metaIds.add("M_oxa_c");
		metaIds.add("M_pnto_DASH_R_c");
		metaIds.add("M_pep_c");
		metaIds.add("M_phe_DASH_L_c");
		metaIds.add("M_cholp_c");
		metaIds.add("M_ethamp_c");
		metaIds.add("M_pro_DASH_L_c");
		metaIds.add("M_ppa_c");
		metaIds.add("M_quln_c");
		metaIds.add("M_ser_DASH_L_c");
		metaIds.add("M_srtn_c");
		metaIds.add("M_sbt_DASH_D_c");
		metaIds.add("M_spmd_c");
		metaIds.add("M_sprm_c");
		metaIds.add("M_succ_c");
		metaIds.add("M_sucr_e");
		metaIds.add("M_taur_c");
		metaIds.add("M_tchola_c");
		metaIds.add("M_tdchola_c");
		metaIds.add("M_thm_c");
		metaIds.add("M_thr_DASH_L_c");
		metaIds.add("M_thymd_c");
		metaIds.add("M_thym_c");
		metaIds.add("M_thyox_DASH_L_c");
		metaIds.add("M_triodthy_c");
		metaIds.add("M_trp_DASH_L_c");
		metaIds.add("M_tyr_DASH_L_c");
		metaIds.add("M_udpglcur_c");
		metaIds.add("M_ump_c");
		metaIds.add("M_ura_c");
		metaIds.add("M_urate_c");
		metaIds.add("M_uri_c");
		metaIds.add("M_val_DASH_L_c");
		metaIds.add("M_xan_c");
		metaIds.add("M_xtsn_c");
		metaIds.add("M_xmp_c");

		for (String meta : metaIds) {
			String metaNameRecon = recon.getMetabolite(meta).getName().substring(1);
			boolean hasIds = false;
			for (MetaboliteCI metaHMR : hmr.getMetabolites().values()) {
				if (metaNameRecon.equalsIgnoreCase(metaHMR.getName())) {
					System.out.println(meta + ";" + metaHMR.getId() + ";" + metaNameRecon);
					hasIds = true;
					break;
				}
			}
			if (!hasIds)
				System.out.println(meta + ";");
		}

	}

	public static void printAllIdsofReducedModel(String sbmlOriginal, Set<String> reacs, String fileRes)
			throws Exception {
		Container cont;

		cont = getReducedModel(sbmlOriginal, reacs);
		FileWriter f = new FileWriter(fileRes);
		BufferedWriter b = new BufferedWriter(f);

		for (String meta : cont.getMetabolites().keySet()) {
			b.write(meta);
			b.newLine();
		}

		for (String gene : cont.getGenes().keySet()) {
			b.write(gene);
			b.newLine();
		}
		for (String reac : cont.getReactions().keySet()) {
			b.write(reac);
			b.newLine();
		}
		b.close();
		f.close();

	}

	public static void printMappingMetabolitesIds(String file1, String file2)
			throws FileNotFoundException, IOException, XMLStreamException, ErrorsException {
		Container c1 = new Container(new JSBMLReader(file1, "human", false));
		Container c2 = new Container(new JSBMLReader(file2, "human", false));

		HashMap<String, String> compMap = new HashMap<String, String>();

		compMap.put("e", "C_1");
		compMap.put("c", "C_2");
		compMap.put("n", "C_3");
		compMap.put("c", "C_4");
		compMap.put("r", "C_5");
		compMap.put("g", "C_6");
		compMap.put("x", "C_7");
		compMap.put("l", "C_8");
		compMap.put("m", "C_9");

		for (MetaboliteCI meta : c1.getMetabolites().values()) {
			String res = meta.getId() + "$" + meta.getName() + "$";
			for (MetaboliteCI meta2 : c2.getMetabolites().values()) {
				if (meta2.getName().equals(meta.getName())) {
					String comp1 = c1.getMetaboliteCompartments(meta.getId()).iterator().next();
					String comp2 = c2.getMetaboliteCompartments(meta2.getId()).iterator().next();
					System.out.println(comp1);
					System.out.println(comp2);
					if (compMap.get(comp1).equals(comp2)) {
						res = res + meta2.getId();
						break;
					}
				}
			}
			System.out.println(res);
		}

	}

	public static void changeIdsTaskFile(String fileTask, String fileRes, String fileIds) throws Exception {
		FileReader f = new FileReader(fileIds);
		BufferedReader b = new BufferedReader(f);
		HashMap mapIds = new HashMap<String, String>();
		Pattern p = Pattern.compile("(M_.*?)[;$]");
		Matcher m;
		while (b.ready()) {
			String[] ids = b.readLine().split(";");
			mapIds.put(ids[0], ids[1]);
		}

		f = new FileReader(fileTask);
		b = new BufferedReader(f);
		while (b.ready()) {
			String line = b.readLine();

			m = p.matcher(line);
			while (m.find()) {
				System.out.println(m.group(1));
			}
		}

	}

	public static void printAllReacs(String file)
			throws FileNotFoundException, IOException, XMLStreamException, ErrorsException {
		Container c1 = new Container(new JSBMLReader(file, "human", false));
		for (String r : c1.getReactions().keySet())
			System.out.println(r);

	}

	private static void runFVA(Container container) throws Exception {

		ISteadyStateModel model = ContainerConverter.convert(container);
		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, SolverType.CPLEX3);
		List<String> res = fva.identifyFVAZeroFluxes().getZeroValueFluxes();
		System.out.println(res);
		System.out.println(res.size());
	}

	private static void printReacAndPathway(Container container) throws Exception {
		for (ReactionCI r : container.getReactions().values()) {
			System.out.println(r.getId() + "$" + r.getSusbystems());
		}
	}

	private static void printReac(Container container) throws Exception {

		System.out.println("Reactions");
		for (String r : container.getReactions().keySet()) {
		 System.out.println(r);
		}
		
	}

	//
	// public static void main(String[] args){
	// try {
	// testHasFlux("/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/TemplateModels/Recon2.xml");
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	// get all ids (metabolites, genes, reacs) for the selected reactions
	// public static void main(String[] args) {
	//
	//// String sbmlOriginal =
	// "/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/TemplateModels/Recon1.xml";
	// String sbmlOriginal =
	// "/Users/Sara/Documents/Projects/PHD_P05_U251/Results/Combinations/ConsensusModel.xml";
	//
	// String fileReactions =
	// "/Users/Sara/Documents/Projects/PHD_P06_HEPG2/Liver_and_HEPG2/Results_HPAv14_GEB/MBA_HPA_v2.txt";
	// String res =
	// "/Users/Sara/Documents/Projects/PHD_P06_HEPG2/Liver_and_HEPG2/Results_HPAv14_GEB/MBA_HPA_v2_allIds.txt";
	//
	// try {
	// Set<String> reacsComb = readIdsFromFile(fileReactions);
	// printAllIdsofReducedModel(sbmlOriginal, reacsComb, res);
	////
	//// Container container = new Container(new JSBMLReader(sbmlOriginal,
	// "human", false));
	//// container.removeMetabolites(container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")));
	//// Set<String> drains =container.identifyDrains();
	//// StringBuffer b = new StringBuffer();
	//// for(String d: drains)
	//// b.append(d + ",");
	////
	//// System.out.println(b);
	//
	//// printReacAndPathway(container);
	//
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	// calculate the critical genes
	public static void main(String[] args) {

//		String sbml = "/Users/Sara/Documents/Projects/PHD_P05_U251/PublishedData/mCADRE_glioblastoma_tumor.xml";
		String sbml = "/Users/Sara/Documents/Projects/PHD_P05_U251/PublishedData/PRIME_U251_correct_ReacNames.xml";
//		String sbml = "/Users/Sara/Documents/Projects/PHD_P05_U251/Results/Combinations/ConsensusModel.xml";
//		String sbml = "/Users/Sara/Documents/Projects/PHD_P05_U251/TemplateModels/Recon1.xml";
		// String sbml =
		// "/Users/Sara/Documents/Projects/PHD_P06_HEPG2/FillModels/Models/HepG2_MBA_HPA.xml";

		try {
			Container container = new Container(new JSBMLReader(sbml, "human", false));
			container.removeMetabolites(container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$"))); 
			container.setBiomassId("R_biomass_reaction");
			
			 Utils.printReac(container);
			
			
//			 container = ContainerSimplification.simplifyModel(container,SolverType.CPLEX3, 0.0001);
			//
			ISteadyStateModel model = ContainerConverter.convert(container);
			
			
//			Utils.printReac(container);
//
//			CriticalGenes critical = new CriticalGenes(model, Medium.getFolgerMedRecon1(container), SolverType.CPLEX3);
//			critical.identifyCriticalGenes();
//			List<String> genes = critical.getCriticalGenesIds();
//
//			String s = "";
//			for (String g : genes) {
//				s += g + "\",\"";
//			}
//
//			System.out.println(s);

			 EnvironmentalConditions env =
			 Medium.getFolgerMedRecon1(container);
//			//
			 SimulationSteadyStateControlCenter cc = new
			 SimulationSteadyStateControlCenter(env,null, model,
			 SimulationProperties.PFBA);
			 cc.setSolver(SolverType.CPLEX3);
			 cc.setMaximization(true);
			 cc.setFBAObjSingleFlux("R_biomass_reaction", 1.0);
//			//
//			//
			 SteadyStateSimulationResult res = cc.simulate();
			 System.out.println(res.getOFvalue());
			 Set<String> measureMetas = Utils.getMeasureMetas();
			 for(String reac:res.getFluxValues().keySet()){
				 if(measureMetas.contains(reac))
					 System.out.println(reac + " : "+res.getFluxValues().get(reac));
			 }
			 System.out.println("R_biomass_reaction" + res.getFluxValues().get("R_biomass_reaction"));
			//
			// System.out.println("R_biomass_reaction :
			// "+res.getFluxValues().get("R_biomass_reaction"));
			//// System.out.println("Recon1");
			//// for(String g : genesRecon1)
			//// System.out.println(g);
			////
			//// System.out.println("GBM");
			//// for(String g : genesGBM)
			//// System.out.println(g);
			////
			//// System.out.println("GBM exclusive");
			//// genesGBM.removeAll(genesRecon1);
			//// for(String g : genesGBM)
			//// System.out.println(g);
			//
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Set<String> getMeasureMetas (){
		Set<String> measureMetas = new TreeSet<String>();
		measureMetas.add("R_EX_4abut_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_4pyrdx_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_acac_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ade_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_adn_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_akg_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ala_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_amp_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_arg_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_asn_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_asp_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_bilirub_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_chol_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_cit_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_cmp_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_creat_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_crn_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_cytd_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_dcyt_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_duri_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_fol_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_gchola_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_gdchola_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_glc_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_gln_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_glu_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_gly_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_glyb_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_glyc_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_gmp_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_gthox_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_hom_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_hxan_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ile_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_imp_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ins_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_lac_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_lcts_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_leu_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_lys_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_met_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_nac_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ncam_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_omeprazole_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_orn_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_oxa_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_phe_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_pnto_R_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ppa_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_pro_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ser_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_srtn_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_succ_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_sucr_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_taur_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_tchola_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_tdchola_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_thm_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_thr_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_thym_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_thymd_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_thyox_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_triodthy_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_trp_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_tyr_L_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ump_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_ura_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_urate_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_uri_LPAREN_e_RPAREN_");
		measureMetas.add("R_EX_val_L_LPAREN_e_RPAREN_");
		return measureMetas;
	}
}
