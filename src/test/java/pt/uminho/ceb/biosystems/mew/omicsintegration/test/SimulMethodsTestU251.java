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
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class SimulMethodsTestU251 {

	private static final double DOUBLE_PRECISSION = 0.00001;

	 private static final String modelFilePath ="/Users/Sara/Documents/Projects/PHD_P05_U251/Results/Combinations/ConsensusModel.xml";
//	 private static final String modelFilePath ="/Users/Sara/Documents/Projects/PHD_P05_U251/PublishedData/PRIME_U251_correct_ReacNames.xml";
//	 private static final String modelFilePath ="/Users/Sara/Documents/Projects/PHD_P05_U251/PublishedData/mCADRE_glioblastoma_tumor.xml";
//	private static final String modelFilePath = "/Users/Sara/Documents/Projects/PHD_P05_U251/TemplateModels/Recon1.xml";

	private static final String biomassReaction = "R_biomass_reaction";

	// data from GEB
	private static final String geneExpressionFilePath = "/Users/Sara/Documents/Projects/PHD_P05_U251/Data/gholami_nci60_U251_GSM803632_mean.txt";
	private static final String fluxomicsFilePath = "/Users/Sara/Documents/Projects/PHD_P05_U251/Data/jain_2012_metabolomics_u251.csv";

	private static final int[] conditionsGeneExpressionDataIndex = { 1 };

	private static final int[] conditionsFluxomicsDataIndex = { 1 };

	private static final double[] quartile1GeneExp = { 4.049038291 };
	private static final double[] quartile3GeneExp = { 6.868278802 };

	private static final String uptakeReaction = "R_EX_glc_LPAREN_e_RPAREN_";
	private static final String biomass = "R_biomass_reaction";

	private static Container container;
	private static EnvironmentalConditions environmentalConditions; 
	private static ISteadyStateGeneReactionModel model;
	private static ArrayList<ReactionDataMap> reacsScores;
	private static ArrayList<IOmicsContainer> fluxomicsData;
	private static ArrayList<String> reacs;

	private List<SteadyStateSimulationResult> results;
	private List<Double> precisionList;

	@BeforeClass
	public static void init() throws Exception {
		try{
		JSBMLReader sbmlReader = new JSBMLReader(modelFilePath, "human", false);
		container = new Container(sbmlReader);
		Set<String> met = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		container.removeMetabolites(met);
		container.setBiomassId(biomassReaction);
		environmentalConditions = Medium.getFolgerMedRecon1(container);
		
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
			
			
			//mCADRE
//			Container recon1mCADREProcess = new Container(new JSBMLReader("/Users/Sara/Documents/Projects/PHD_P05_U251/TemplateModels/Recon1.xml", "human", false)); 
//			Gene2GeneIntegrator integrator = new Gene2GeneIntegrator(recon1mCADREProcess, Config.FIELD_ID, Config.FIELD_ID);
//			GeneDataMap expEvidence = (GeneDataMap) integrator.convert(dataContainer);
//			
//			TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(recon1mCADREProcess);
////			
			
			

			
			// E-FULX
//			Map<String, Object> properties = new HashMap<String, Object>();
//			properties.put(TransformDataMapGeneToReac.VAR_CONTAINER, container);
//			properties.put(TransformDataMapGeneToReac.VAR_OPERATION_AND, "MIN");
//			properties.put(TransformDataMapGeneToReac.VAR_OPERATION_OR, "PLUS");
//			TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(properties);

			
			
			//Transform ids
			ReactionDataMap aux = (ReactionDataMap) transformDataMap.transform(expEvidence);
			
			//mCADRE
			aux.getMapValues().keySet().retainAll(container.getReactions().keySet());
			
			
			reacsScores.add(aux);
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



		System.out.println("END");
		}catch(Exception e){e.printStackTrace();}
	}

	@Test
	public void TestpFBA() throws Exception {
		System.out.println("TestpFBA");
		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {
			SimulationSteadyStateControlCenter cc = new SimulationSteadyStateControlCenter(environmentalConditions,
					null, model, SimulationProperties.PFBA);
			cc.setSolver(SolverType.CPLEX3);
			cc.setMaximization(true);
			cc.setFBAObjSingleFlux(biomass, 1.0);
			
			SteadyStateSimulationResult res = cc.simulate();
			
			
			double scaleValue = Math.abs(
					fluxomicsData.get(i).getValues().get(uptakeReaction) / res.getFluxValues().get(uptakeReaction));
			
			System.out.println(fluxomicsData.get(i).getValues().get(uptakeReaction) + " / "
					+ res.getFluxValues().get(uptakeReaction) + "scale Value" + scaleValue);

//			normalizeFlux(res, scaleValue);
			
			
			System.out.println("----------------");
			for (String r : reacs)
				System.out.println(r + " " + res.getFluxValues().getValue(r));
		}
	}


//	@Test
	public void testIMAT() throws Exception {
		// Initialize Results Array
		results = new ArrayList<SteadyStateSimulationResult>();
		precisionList = new ArrayList<Double>();

		System.out.println("---------------IMAT--------------- ");
		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {
			// Run IMAT for
			System.out.println("Running Condition " + uptakeReaction + " [" + quartile1GeneExp + ", " + quartile3GeneExp
					+ "] ...");

			IMATConfiguration config = new IMATConfiguration(container, SolverType.CPLEX3);
			config.setUpDownRegulatedReactions(reacsScores.get(i), quartile1GeneExp[i], quartile3GeneExp[i]);
					
			config.setEnvironmentalConditions(environmentalConditions);
			config.setEpsilon(1.0);

			IMAT imat = new IMAT(model, config);

			SteadyStateSimulationResult resultIMAT = imat.simulate();

			System.out.println("Otpmial Solution: " + resultIMAT.getOFvalue());

			double scaleValue = Math.abs(
					fluxomicsData.get(i).getValues().get(uptakeReaction) / resultIMAT.getFluxValues().get(uptakeReaction));
			
			System.out.println(fluxomicsData.get(i).getValues().get(uptakeReaction) + " / "
					+ resultIMAT.getFluxValues().get(uptakeReaction) + "scale Value" + scaleValue);

//			normalizeFlux(resultIMAT, scaleValue);
			results.add(resultIMAT);

			System.out.println("----------------");
			for (String r : reacs)
				System.out.println(r + " " + resultIMAT.getFluxValues().getValue(r));
		}

	}

//	@Test
	public void testGIMME() throws Exception {
		// Initialize Results Array
		results = new ArrayList<SteadyStateSimulationResult>();
		precisionList = new ArrayList<Double>();
		System.out.println("\n --------------- GIMME--------------- ");

		GIMMEConfiguration config = new GIMMEConfiguration(container, SolverType.CPLEX3);
		TasksReader tasks = new TasksReader(container,
				"/Users/Sara/Documents/Projects/PHD_P05_U251/Data/grow.txt", Config.FIELD_ID,
				true);
		tasks.load();
		config.setTasks(tasks.getTasks());
		config.setRMFPercentage(0.9);

		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {
			// Set Condition

			config.setCutOff(quartile1GeneExp[i]);

			config.setEnvironmentalConditions(environmentalConditions);

			config.setReactionScores(reacsScores.get(i));

			GIMME gimme = new GIMME(model, config);
			SteadyStateSimulationResult result = gimme.simulate();

			System.out.println("Otpmial Solution: " + result.getOFvalue());

			
			double scaleValue = Math.abs(
					fluxomicsData.get(i).getValues().get(uptakeReaction) / result.getFluxValues().get(uptakeReaction));
			
			System.out.println(fluxomicsData.get(i).getValues().get(uptakeReaction) + " / "
					+ result.getFluxValues().get(uptakeReaction) + "scale Value" + scaleValue);

//			normalizeFlux(result, scaleValue);
			results.add(result);
			
			System.out.println("----------------");
			for (String r : reacs)
				System.out.println(r + " " + result.getFluxValues().getValue(r));
		}

	}

//	@Test
	public void testEFLUX() throws Exception {
		// Initialize Results Array
		results = new ArrayList<SteadyStateSimulationResult>();
		precisionList = new ArrayList<Double>();


		EFluxConfiguration config = new EFluxConfiguration(container, SolverType.CPLEX3);

		System.out.println("--------------- EFLUX--------------- ");
		for (int i = 0; i < conditionsGeneExpressionDataIndex.length; i++) {

			config.setReactionScores(reacsScores.get(i));
			config.setEnvironmentalConditions(environmentalConditions);
			EFlux eflux = new EFlux(model, config);

			try{
			SteadyStateSimulationResult result = eflux.simulate();
			
			
			double scaleValue = Math.abs(
					fluxomicsData.get(i).getValues().get(uptakeReaction) / result.getFluxValues().get(uptakeReaction));
			
			System.out.println(fluxomicsData.get(i).getValues().get(uptakeReaction) + " / "
					+ result.getFluxValues().get(uptakeReaction) + "scale Value" + scaleValue);

//			normalizeFlux(result, scaleValue);
			results.add(result);

			System.out.println("----------------");
			for (String r : reacs)
				System.out.println(r + " " + result.getFluxValues().getValue(r));
		
		}catch(Exception e ){e.printStackTrace();}
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
