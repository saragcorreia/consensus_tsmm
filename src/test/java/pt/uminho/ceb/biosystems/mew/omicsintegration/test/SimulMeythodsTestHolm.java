package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SimulationSteadyStateControlCenter;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Gene2GeneIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.TasksReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.EFluxConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.GIMMEConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.IMATConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods.EFlux;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods.GIMME;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods.IMAT;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.SimulationProperties;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformDataMapGeneToReac;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class SimulMeythodsTestHolm {

	private static final double DOUBLE_PRECISSION = 0.00001;

	private static final String modelFilePath = "/Volumes/Data/Documents/Projects/PHD_P01_Reconst_Approaches/SimulMethods/iAF1260.xml";
	private static final String biomassReaction = "R_Ec_biomass_iAF1260_core_59p81M";

	private static final String geneExpressionFilePath = "/Volumes/Data/Documents/Projects/PHD_P01_Reconst_Approaches/SimulMethods/holm_transcriptome.csv";
	private static final String fluxomicsFilePath = "/Volumes/Data/Documents/Projects/PHD_P01_Reconst_Approaches/SimulMethods/holm_fluxes_QP.csv";

	private static final int[] conditionsGeneExpressionDataIndex = { 1, 2, 3 };

	private static final int[] conditionsFluxomicsDataIndex = { 1, 2, 3 };

	private static final double[] quartile1GeneExp = { 8.11806682, 8.954627681, 8.580724585 };
	private static final double[] quartile3GeneExp= { 11.41439423, 11.27056071, 11.2483921 };
	private static final double[] limitGlc = { -9.2, -11.7, -15.6 };

	private static final String uptakeReaction = "R_EX_glc_e_";
	private static final String biomass = "R_Ec_biomass_iAF1260_core_59p81M";

	private static Container container;
	private static ISteadyStateGeneReactionModel model;
	private static ArrayList<ReactionDataMap> reacsScores;
	private static ArrayList<IOmicsContainer> fluxomicsData;
	private static ArrayList<String> reacs;

	private List<SteadyStateSimulationResult> results;
	private List<Double> precisionList;

	@BeforeClass
	public static void init() throws Exception {

		// Intialize the SBML reader for the iND750 yeast model
		JSBMLReader sbmlReader = new JSBMLReader(modelFilePath, "yeast", false);
		container = new Container(sbmlReader);
		Set<String> met = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		container.removeMetabolites(met);
		container.setBiomassId(biomassReaction);
		model = (ISteadyStateGeneReactionModel) ContainerConverter.convert(container);

		// Initialize the Gene Expression Data Reader
		CSVOmicsReader geneExpressionDataReader = new CSVOmicsReader(new Condition(), geneExpressionFilePath,
				OmicsDataType.GENE);
		geneExpressionDataReader.DELIMITER_INSIDE_FIELDS = ";";
		geneExpressionDataReader.USER_DELIMITER = ",";
		geneExpressionDataReader.setIdColumnIndex(0);
		geneExpressionDataReader.setHasHeader(true);
		// read all gene expression conditions
		reacsScores = new ArrayList<ReactionDataMap>();

		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {
			geneExpressionDataReader.setValuesColumnIndex(conditionsGeneExpressionDataIndex[i]);
			IOmicsContainer dataContainer = geneExpressionDataReader.load();
			Gene2GeneIntegrator integrator = new Gene2GeneIntegrator(container, Config.FIELD_ID, Config.FIELD_ID);

			GeneDataMap expEvidence = (GeneDataMap) integrator.convert(dataContainer);

			TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(container);
			// E-FULX
//			 Map<String, Object> properties = new HashMap<String, Object>();
//			 properties.put(TransformDataMapGeneToReac.VAR_CONTAINER,
//			 container);
//			 properties.put(TransformDataMapGeneToReac.VAR_OPERATION_AND,
//			 "MIN");
//			 properties.put(TransformDataMapGeneToReac.VAR_OPERATION_OR,
//			 "PLUS");
//			
//			 TransformDataMapGeneToReac transformDataMap = new
//			 TransformDataMapGeneToReac(properties);

			reacsScores.add((ReactionDataMap) transformDataMap.transform(expEvidence));
		}

		// Initialize the Fluxomics Data Reader
		CSVOmicsReader fluxesDataReader = new CSVOmicsReader(new Condition(), fluxomicsFilePath,
				OmicsDataType.REACTION);
		fluxesDataReader.DELIMITER_INSIDE_FIELDS = ";";
		fluxesDataReader.USER_DELIMITER = ",";
		fluxesDataReader.setIdColumnIndex(0);
		fluxesDataReader.setHasHeader(true);
		fluxomicsData = new ArrayList<IOmicsContainer>();
		for (int i = 0; i < conditionsFluxomicsDataIndex.length; i++) {
			fluxesDataReader.setValuesColumnIndex(conditionsFluxomicsDataIndex[i]);
			IOmicsContainer dataContainer = fluxesDataReader.load();
			fluxomicsData.add(dataContainer);
		}
		reacs = new ArrayList<String>();
		reacs.addAll(fluxomicsData.get(0).getValues().keySet());

		Collections.sort(reacs);

		for (String r : reacs)
			System.out.println(r);

	}
	//@Test
	public void TestpFBA() throws Exception {
		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {
		EnvironmentalConditions environmentalConditions = new EnvironmentalConditions();
		environmentalConditions.addReactionConstraint(uptakeReaction, new ReactionConstraint(limitGlc[i], 0));

		SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(environmentalConditions,null,model,SimulationProperties.PFBA);		
		cc.setSolver(SolverType.CPLEX3);
		cc.setMaximization(true);
		cc.setFBAObjSingleFlux(biomass, 1.0);
		
		SteadyStateSimulationResult res = cc.simulate();
		System.out.println("----------------");
		for (String r : reacs)
			System.out.println(r + " " + res.getFluxValues().getValue(r));
	}
	}
	
	//@Test
	public void MinFlux() throws Exception {
		FBA fba = new FBA(model);
		EnvironmentalConditions env = new EnvironmentalConditions();
		env.put("R_ACKr",new ReactionConstraint(-9.57000,-9.56000));
		env.put("R_ACONTa",new ReactionConstraint(1.00000,1.0000));
		env.put("R_CS",new ReactionConstraint(1.00000,1.0000));
		env.put("R_ENO",new ReactionConstraint(16.56000,16.57000));
		env.put("R_FBA",new ReactionConstraint(5.86000,5.87000));
		env.put("R_FUM",new ReactionConstraint(4.00000,4.0000));
		env.put("R_G6PDH2r",new ReactionConstraint(13.50000,13.51000));
		env.put("R_GAPD",new ReactionConstraint(17.56000,17.57000));
		env.put("R_GLCptspp",new ReactionConstraint(11.70000,11.7000));
		env.put("R_GND",new ReactionConstraint(12.50000,12.5000));
		env.put("R_MDH",new ReactionConstraint(4.00000,4.0000));
		env.put("R_ME1",new ReactionConstraint(0.0000,0.0000));
		env.put("R_NADTRHD",new ReactionConstraint(1.00000,1.0000));
		env.put("R_PDH",new ReactionConstraint(6.43000,6.44000));
		env.put("R_PFK",new ReactionConstraint(5.86000,5.87000));
		env.put("R_PGI",new ReactionConstraint(-1.81000,-1.80000));
		env.put("R_PGK",new ReactionConstraint(-17.57000,-17.56000));
		env.put("R_PGL",new ReactionConstraint(13.50000,13.51000));
		env.put("R_PGM",new ReactionConstraint(-16.57000,-16.56000));
		env.put("R_PPC",new ReactionConstraint(1.00000,1.0000));
		env.put("R_PTAr",new ReactionConstraint(9.56000,9.57000));
		env.put("R_PYK",new ReactionConstraint(3.86000,3.87000));
		env.put("R_RPE",new ReactionConstraint(7.66000,7.67000));
		env.put("R_RPI",new ReactionConstraint(-4.84000,-4.83000));
		env.put("R_SUCDi",new ReactionConstraint(1.00000,1.0000));
		env.put("R_TALA",new ReactionConstraint(3.83000,3.84000));
		env.put("R_TKT1",new ReactionConstraint(3.83000,3.84000));
		env.put("R_TKT2",new ReactionConstraint(3.83000,3.84000));
		env.put("R_TPI",new ReactionConstraint(5.86000,5.87000));
		env.put(uptakeReaction,new ReactionConstraint(-11.7,0));
		env.put("R_EX_ac_e",new ReactionConstraint(10.57,10.57));
		
		fba.setEnvironmentalConditions(env);
		fba.setSolverType(SolverType.CPLEX3);
		fba.setIsMaximization(false);
		HashMap<String, Double>obj_coef = new HashMap ();
		obj_coef.put("R_SUCOAS", 1.0);
		fba.setObjectiveFunction(obj_coef);
		SteadyStateSimulationResult res = fba.simulate();
		System.out.println(res.getOFvalue());
		
		fba.setIsMaximization(true);
		res = fba.simulate();
		System.out.println(res.getOFvalue());
		
		
	}
	
//	@Test
	public void testIMAT() throws Exception {
		// Initialize Results Array
		results = new ArrayList<SteadyStateSimulationResult>();
		precisionList = new ArrayList<Double>();

		System.out.println("---------------IMAT--------------- ");
		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {
			// Run IMAT for
			System.out.println("Running Condition " + uptakeReaction + " [" + quartile1GeneExp + ", " + quartile3GeneExp + "] ...");

			IMATConfiguration config = new IMATConfiguration(container, SolverType.CPLEX3);
			config.setUpDownRegulatedReactions(reacsScores.get(i), quartile1GeneExp[i], quartile3GeneExp[i]);

			EnvironmentalConditions environmentalConditions = new EnvironmentalConditions();
			environmentalConditions.addReactionConstraint(uptakeReaction, new ReactionConstraint(limitGlc[i], 0));
			config.setEnvironmentalConditions(environmentalConditions);
			config.setEpsilon(1.0);
			
			IMAT imat = new IMAT(model, config);

			SteadyStateSimulationResult resultIMAT = imat.simulate();

			System.out.println("Otpmial Solution: " + resultIMAT.getOFvalue());

			results.add(resultIMAT);
			System.out.println("----------------");
			for (String r : reacs)
				System.out.println(resultIMAT.getFluxValues().getValue(r));
		}

	}

	@Test
	public void testGIMME() throws Exception {
		// Initialize Results Array
		results = new ArrayList<SteadyStateSimulationResult>();
		precisionList = new ArrayList<Double>();
		System.out.println("\n --------------- GIMME--------------- ");

		GIMMEConfiguration config = new GIMMEConfiguration(container, SolverType.CPLEX3);
		TasksReader tasks = new TasksReader(container,
				"/Volumes/Data/Documents/Projects/PHD_P01_Reconst_Approaches/SimulMethods/grow.txt", Config.FIELD_ID, true);
		tasks.load();
		config.setTasks(tasks.getTasks());
		config.setRMFPercentage(0.9);

		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++){
			// Set Condition

			config.setCutOff(quartile1GeneExp[i]);

			EnvironmentalConditions environmentalConditions = new EnvironmentalConditions();
			environmentalConditions.addReactionConstraint(uptakeReaction, new ReactionConstraint(limitGlc[i], 0));
			config.setEnvironmentalConditions(environmentalConditions);
			
			config.setReactionScores(reacsScores.get(i));

			GIMME gimme = new GIMME(model, config);
			SteadyStateSimulationResult result = gimme.simulate();

			System.out.println("Otpmial Solution: " + result.getOFvalue());

			results.add(result);
			System.out.println("----------------");
			for (String r : model.getReactions().keySet())
				System.out.println(r +" "+ result.getFluxValues().getValue(r));
		}

	}

	// @Test
	public void testEFLUX() throws Exception {
		// Initialize Results Array
		results = new ArrayList<SteadyStateSimulationResult>();
		precisionList = new ArrayList<Double>();
		
		for (String r : reacs)
			System.out.println(r);

		EFluxConfiguration config = new EFluxConfiguration(container, SolverType.CPLEX3);

		System.out.println("--------------- EFLUX--------------- ");
		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {

			config.setReactionScores(reacsScores.get(i));
			EFlux eflux = new EFlux(model, config);

			SteadyStateSimulationResult result = eflux.simulate();

			double scaleValue = Math.abs(
					fluxomicsData.get(i).getValues().get(uptakeReaction) / result.getFluxValues().get(uptakeReaction));
			System.out.println(fluxomicsData.get(i).getValues().get(uptakeReaction) + " / "
					+ result.getFluxValues().get(uptakeReaction) + "scale Value" + scaleValue);

			normalizeFlux(result, scaleValue);
			results.add(result);

			System.out.println("----------------");
			for (String r : reacs)
				System.out.println(result.getFluxValues().getValue(r));
		}

	}

	private void normalizeFlux(SteadyStateSimulationResult result, double scale_val) {
		for (String r : result.getFluxValues().keySet()) {
			double value = result.getFluxValues().get(r) * scale_val;
			result.getFluxValues().setValue(r, value);
		}
	}

	private double mean(List<Double> list) {
		double sum = 0;
		for (Double value : list) {
			sum += value;
		}
		return (sum / list.size());
	}
}
