package pt.uminho.ceb.biosystems.mew.omicsintegration.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;

public class FolgerModelTests {


	public static String modelFile ="/Users/Sara/Documents/Projects/PHD_P05_U251/TemplateModels/Recon1.xml";
	public static String reacsFile = "/Users/Sara/Documents/Projects/PHD_P05_U251/CancerModel_Folger/Data/FolgerReactions_pathways.csv";
	public static String resultFile ="/Users/Sara/Documents/Projects/PHD_P05_U251/CancerModel_Folger/Models/Folder_Model_Reacs.txt ";

//	public static String modelFile2 = baseDir + "/Models/recon1_emanuel.xml";
//	public static String modelFile3 = baseDir + "/Models/recon1_emanuel_1000.xml";
//	public static String modelFile4 = baseDir + "/Models/recon1_emanuel_biomass_recon2.xml";
//	public static String modelFile5 = baseDir + "/Models/recon1_emanuel_biomass_recon2_1000.xml";
//	public static String modelFile6 = baseDir + "/Models/Folger_cancer_biomass_recon2.xml";
//	public static String taskFile2 = baseDir + "/Data/TaskGrowth_metas_notMedium.csv";
	public static Pattern pattern = Pattern.compile(".*_b$");

	public static void buildFolgerModel() throws Exception {

		Container container = new Container(new JSBMLReader(modelFile, "human", false));
		Map<String, Set<String>> reacs = new HashMap<String, Set<String>>();
		Set<String> reacsPresent = new HashSet<String>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(reacsFile))) {
			while (br.ready()) {
				String line = br.readLine();
				String[] tk = line.split("\\$");				
				String p = tk.length==1?"": tk[1];
				if(reacs.containsKey(tk[0])){
					reacs.get(tk[0]).add(p.trim());
				}
				else{
					Set<String> pathways = new HashSet<String>();
					pathways.add(p.trim());
					reacs.put(tk[0], pathways);
				}
				
			}
		}
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile))) {
			for(ReactionCI reac: container.getReactions().values()){
				if(reac.getName().equals("N-Acetylneuraminate 9-phosphate phosphohydrolase")){
					System.out.println("stop");
					Set<String> a = reacs.get(reac.getName());
					a.size();
				}
				String system = reac.getSubsystem().equals("null")?"": reac.getSubsystem();
				
				if(reacs.keySet().contains(reac.getName()) && reacs.get(reac.getName()).contains(system)){
					bw.write(reac.getId());
					bw.newLine();
					reacsPresent.add(reac.getName());
					
					System.out.println(reac.getId() + "\t" + reac.getName()+ "\t" + reac.getSubsystem());
				}
			}
		}
		System.out.println("Names with problems");
		reacs.keySet().removeAll(reacsPresent);
		for(String r : reacs.keySet() )
			System.out.println(r);
	}

	public static void main(String[] args) {
		try {
			buildFolgerModel();
			// simulation pFBA
			// Container container = new Container(new JSBMLReader(modelFile,
			// "human", false));
			// container.removeMetabolites(container.identifyMetabolitesIdByPattern(pattern));
			// Utils.adaptContainer(container, 0.0, 1000);
			//
			// Container container2 = new Container(new JSBMLReader(modelFile2,
			// "human", false));
			// container2.removeMetabolites(container2.identifyMetabolitesIdByPattern(pattern));
			// Utils.adaptContainer(container2, 0.0, 1000);
			//
			// Container container3 = new Container(new JSBMLReader(modelFile3,
			// "human", false));
			// container3.removeMetabolites(container3.identifyMetabolitesIdByPattern(pattern));
			//
			// Container container4 = new Container(new JSBMLReader(modelFile4,
			// "human", false));
			// container4.removeMetabolites(container4.identifyMetabolitesIdByPattern(pattern));
			// Utils.adaptContainer(container4, 0.0, 1000);
			//
			// Container container5 = new Container(new JSBMLReader(modelFile5,
			// "human", false));
			// container5.removeMetabolites(container5.identifyMetabolitesIdByPattern(pattern));

//			Container container6 = new Container(new JSBMLReader(modelFile6, "human", false));
//			container6.removeMetabolites(container6.identifyMetabolitesIdByPattern(pattern));
//			Utils.adaptContainer(container6, 0.0, 1000);

			// System.out.println(container.getMetabolites().size());
			// System.out.println(container.getReactions().size());
			// System.out.println(container.identifyDrains().size());
			// // JSBMLWriter w = new JSBMLWriter("/Users/Sara/funciona.xml",
			// container);
			// w.writeToFile();
			// get metabolites that can be removed
			// for (Map.Entry<String, MetaboliteCI> meta :
			// container.getMetabolites().entrySet()) {
			// System.out.println(meta.getKey());
			// }

			System.out.println("RPMI1640 Medium");
			// Map<String, Double> map = Utils.simulate(container, "R_BIOMASS",
			// Medium.getRPM1640Recon1(container))
			// .getFluxValues();
//			Map<String, Double> map1 = Utils.simulate(container6, "R_BIOMASS", Medium.getRPM1640Recon1(container6))
//					.getFluxValues();
			// Map<String, Double> map2 = Utils.simulate(container2,
			// "R_BIOMASS", Medium.getRPM1640Recon1(container2))
			// .getFluxValues();
			// Map<String, Double> map3 = Utils.simulate(container3,
			// "R_BIOMASS",
			// Medium.getRPM1640Recon1_x1000(container3)).getFluxValues();
			//
			// Map<String, Double> map4 = Utils.simulate(container4,
			// "R_BIOMASS", Medium.getRPM1640Recon1(container4))
			// .getFluxValues();
			// Map<String, Double> map5 = Utils.simulate(container5,
			// "R_BIOMASS",
			// Medium.getRPM1640Recon1_x1000(container5)).getFluxValues();
			//
			// System.out.println("Biomass cancer model:" +
			// map.get("R_BIOMASS"));
			// System.out.println("Biomass generic model:" +
			// map2.get("R_BIOMASS"));
			// System.out.println("Biomass generic model x 1000:" +
			// map3.get("R_BIOMASS"));
//			System.out.println("Biomass cancer model biomassRecon2 :" + map1.get("R_BIOMASS"));
			// System.out.println("Biomass generic model biomassRecon2:" +
			// map4.get("R_BIOMASS"));
			// System.out.println("Biomass generic model biomassRecon2 x 1000:"
			// + map5.get("R_BIOMASS"));

//			System.out.println("Folger Medium");
			// Map<String, Double> map6 = Utils.simulate(container, "R_BIOMASS",
			// Medium.getFolgerMedRecon1(container))
			// .getFluxValues();
			// Map<String, Double> map7 = Utils.simulate(container2,
			// "R_BIOMASS", Medium.getFolgerMedRecon1(container2))
			// .getFluxValues();
			//
			// Map<String, Double> map8 = Utils.simulate(container4,
			// "R_BIOMASS", Medium.getFolgerMedRecon1(container4))
			// .getFluxValues();

//			Map<String, Double> map9 = Utils.simulate(container6, "R_BIOMASS", Medium.getFolgerMedRecon1(container6))
//					.getFluxValues();
//
			// System.out.println("Biomass cancer model:" +
			// map6.get("R_BIOMASS"));
//			System.out.println("Biomass cancer model biomassRecon2:" + map9.get("R_BIOMASS"));
			// System.out.println("Biomass generic model:" +
			// map7.get("R_BIOMASS"));
			// System.out.println("Biomass generic model biomassRecon2:" +
			// map8.get("R_BIOMASS"));

			//
			// System.out.println("Biomass Folger medium:"
			// + Utils.simulate(container, "R_BIOMASS",
			// Medium.getFolgerMedRecon1(container)).getFluxValues()
			// .get("R_BIOMASS"));
			// //
			//
			// TasksReader tasks = new TasksReader(container, taskFile2,
			// Config.FIELD_ID, false);
			// tasks.load();

			// TasksSimulationControlCenter tcc = new
			// TasksSimulationControlCenter(container, tasks.getTasks(),
			// Medium.getFolgerMedRecon1(container), null, SolverType.CPLEX3,
			// true);
			// System.out.println("Percursores produzidoa" +
			// tcc.simulate().size());

			// Container container2 = new Container(new
			// JSBMLReader("/Users/Sara/dream-master/models/recon1.xml",
			// "human",
			// false));

			// Set<String> reacs = new HashSet<String>();
			//
			// Map<String, Set<String>> listReactions = new HashMap<String,
			// Set<String>>();
			//
			// for (String r : reacs) {
			// listReactions.put(r, new HashSet<String>());
			// System.out.println(r);
			// Map<String, Double> fluxes = Utils.simulate(container, r,
			// Medium.getRPM1640Recon1_x1000(container))
			// .getFluxValues();
			// System.out.println(r + " flux model:" + fluxes.get(r));
			// for (Map.Entry<String, Double> entry : fluxes.entrySet()) {
			// if (entry.getValue() != 0.0 &&
			// !container.getReactions().containsKey(entry.getKey()))
			// listReactions.get(r).add(entry.getKey());
			// }
			// }

			// for (Map.Entry<String, Set<String>> entry :
			// listReactions.entrySet()) {
			// System.out.println(">>>>>>>" + entry.getKey());
			// for (String r : entry.getValue())
			// System.out.println(r);
			//
			// }
			// get targets to inibit the precursors of biomass
			// TasksReader tasks2 = new TasksReader(container, taskFile2,
			// Config.FIELD_NAME);
			// tasks2.load();
			//
			// ISteadyStateModel model = ContainerConverter.convert(container);
			//
			// ArrayList<TargetsInputData> inputModels = new
			// ArrayList<TargetsInputData>();
			// TargetsInputData tissue = new TargetsInputData(container,
			// tasks2.getTasks(), true);
			//
			// inputModels.add(tissue);
			// inputModels.add(tissue);
			//
			// TargetsOptimControlCenter targets = new
			// TargetsOptimControlCenter(inputModels, null, false, true, 30,
			// AlgorithmTypeEnum.SPEA2, null, SolverType.CPLEX3,
			// new ArrayList<String>(container.identifyDrains()), 10);
			//
			// TargetsOptimResult res = targets.run();
			// Set<TargetsOptimizationSingleResult> bests =
			// res.getBestResults();
			//
			// for (TargetsOptimizationSingleResult b : bests) {
			// String knockouts = "";
			// for (String k : b.getKnockouts())
			// knockouts += k + ", ";
			//
			// System.out.println(knockouts + " Generic:" +
			// b.getFitnessesValue().get(0) + " Tissue:"
			// + b.getFitnessesValue().get(1));
			// }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
