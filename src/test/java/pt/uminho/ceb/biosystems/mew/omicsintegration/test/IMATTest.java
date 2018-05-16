package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.Reaction;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.components.enums.ReactionType;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.gpr.ISteadyStateGeneReactionModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Gene2GeneIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.configuration.IMATConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods.IMAT;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformDataMapGeneToReac;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class IMATTest {

	private static final double DOUBLE_PRECISSION = 0.001;
	
	private static final String modelFilePath = "./src/test/resources/models/Scerevisiae_iND750.xml";
	private static final String biomassReaction = "R_biomass_SC4_bal";

	private static final String geneExpressionFilePath = "./src/test/resources/imat/Daran_et_al_Gene_Exp_Yeast.txt";
	private static final String fluxomicsFilePath = "./src/test/resources/imat/Daran_et_al_Fluxomics_Yeast.txt";
	
	private static final String[] conditions = {"R_EX_glc_LPAREN_e_RPAREN_", "R_EX_malt_LPAREN_e_RPAREN_", "R_EX_etoh_LPAREN_e_RPAREN_", "R_EX_ac_LPAREN_e_RPAREN_"};

	private static final int[] conditionsGeneExpressionDataIndex = {5, 7, 9, 11};
	
	private static final int[] conditionsFluxomicsDataIndex = {2, 4, 6, 8};
	
	private static final int lowerBound = 50;
	private static final int[] upperBound = {200, 400, 600, 800};

	static Container container;
	private static ISteadyStateGeneReactionModel model;
	private CSVOmicsReader geneExpressionDataReader;
	private CSVOmicsReader fluxesDataReader;
	private EnvironmentalConditions environmentalConditions;
	
	private List<List<SteadyStateSimulationResult>> results;
	private List<Double> precisionList;
	
	@BeforeClass
	public static  void init() throws Exception {
		
		// Intialize the SBML reader for the iND750 yeast model
		JSBMLReader sbmlReader = new JSBMLReader(modelFilePath, "yeast", false);
		container = new Container(sbmlReader);
		Set<String> met = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		container.removeMetabolites(met);
		container.setBiomassId(biomassReaction);
		model = (ISteadyStateGeneReactionModel) ContainerConverter.convert(container);
		
	}
	
	@Test
	public void testYeastDifferentConditions() throws Exception {
		// Initialize Results Array
		results = new ArrayList<List<SteadyStateSimulationResult>>();
		precisionList = new ArrayList<Double>();
		
		// Initialize the Yeast Gene Expression Data Reader
		geneExpressionDataReader = new CSVOmicsReader(new Condition(), geneExpressionFilePath, OmicsDataType.GENE);
		geneExpressionDataReader.DELIMITER_INSIDE_FIELDS = ";";
		geneExpressionDataReader.USER_DELIMITER = "\t";
		geneExpressionDataReader.setIdColumnIndex(1);
		geneExpressionDataReader.setHasHeader(true);
		
		// Initialize the Yeast Fluxomics Data Reader
		fluxesDataReader = new CSVOmicsReader(new Condition(), fluxomicsFilePath, OmicsDataType.REACTION);
		fluxesDataReader.DELIMITER_INSIDE_FIELDS = ";";
		fluxesDataReader.USER_DELIMITER = "\t";
		fluxesDataReader.setIdColumnIndex(1);
		fluxesDataReader.setHasHeader(true);
		
		for (int i = 0; i < conditions.length; i++) {	
			
			// Initialize Condition Results Array
			List<SteadyStateSimulationResult> conditionResults = new ArrayList<SteadyStateSimulationResult>();
			
			// Add the limiting carbon source constraint 
			environmentalConditions = new EnvironmentalConditions();
			environmentalConditions.addReactionConstraint(conditions[i], new ReactionConstraint(-10, -10));
			
			// Set Condition Gene Expression Value Column Index and Load
			geneExpressionDataReader.setValuesColumnIndex(conditionsGeneExpressionDataIndex[i]);
			IOmicsContainer dataContainer = geneExpressionDataReader.load();
			
			Gene2GeneIntegrator integrator = new Gene2GeneIntegrator(container, Config.FIELD_ID, Config.FIELD_ID);
			
			GeneDataMap expEvidence = (GeneDataMap) integrator.convert(dataContainer);
			
			TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(container);
			ReactionDataMap scoreReac =  (ReactionDataMap) transformDataMap.transform(expEvidence);
			
			
			// Run IMAT for each upper and lower bound
			for (int j = 0; j < upperBound.length; j++) {
				System.out.println("Running Condition " + conditions[i] + " [" + lowerBound + ", " + upperBound[j] + "] ...");				
				
				SteadyStateSimulationResult result = runIMAT(scoreReac, lowerBound, upperBound[j]);				
				conditionResults.add(result);
				
				System.out.println("Otpmial Solution: " + result.getOFvalue());
				
				// Set Condition Fluxomics Value Column Index and Load
				fluxesDataReader.setValuesColumnIndex(conditionsFluxomicsDataIndex[i]);
				IOmicsContainer fluxomicsDataContainer = fluxesDataReader.load();
				
				double precision = validatePredictions(result, fluxomicsDataContainer);
								
				precisionList.add(precision);
				
			}
			
			results.add(conditionResults);
		}

		System.out.println("----------------");
		System.out.println("Precision mean: " + mean(precisionList));
	}
	
	public SteadyStateSimulationResult runIMAT(ReactionDataMap reacScores, double lb, double ub) throws Exception {
		

		// Initialize and Run IMAT
		
		IMATConfiguration config = new IMATConfiguration(container, SolverType.CPLEX3);
		config.setUpDownRegulatedReactions(reacScores, lb, ub);
		IMAT imat = new IMAT(model, config);
		
		imat.setEnvironmentalConditions(environmentalConditions);
		SteadyStateSimulationResult result = imat.simulate();

		return result;
	}

	
	private double validatePredictions(SteadyStateSimulationResult result, IOmicsContainer fluxomicsDataContainer) {
		int N = internalReactions().size();
		int k = measuredReactions(fluxomicsDataContainer).size();
		int n = predictedFluxes(result.getFluxValues()).size();

		HypergeometricDistribution test = new HypergeometricDistribution(N, k, n);
		
		int intersection = intersection(fluxomicsDataContainer, result.getFluxValues()).size();
		double probability = test.probability(intersection);
		double precision = intersection / ((double) k);
		
		System.out.println("Hyper-geometric test: " + probability);
		System.out.println("Precision: " + precision);

//		assertTrue(probability < 0.01);
//		assertTrue(precision > 0.5);

		return precision;
	}
	
	
	private List<String> internalReactions() {
		List<String> reactions = new ArrayList<String>();
		for (Reaction reaction : model.getReactions().values()) {
			switch (reaction.getType()) {
				case INTERNAL: 
					reactions.add(reaction.getId()); break;
					
				case TRANSPORT: 
					reactions.add(reaction.getId()); break;
					
				case UNKNOWN:
					reactions.add(reaction.getId()); break;
					
				case DRAIN: 
					break;
					
				default:;
			}
		}
		return reactions;
	}
	
	private List<String> measuredReactions(IOmicsContainer container) {
		
		List<String> reactions = new ArrayList<String>();		
		for (String reactionId : container.getValues().keySet()) {
			
			if (container.getValues().get(reactionId) != 0 && model.getReaction(reactionId) != null) {
				reactions.add(reactionId);
			}
		
		}
		return reactions;
	}
	
	private List<String> predictedFluxes(FluxValueMap predictions) {
		List<String> reactions = new ArrayList<String>();
		
		for (String reactionId : predictions.getReactionIds()) {
			
			if (predictions.getValue(reactionId) != 0.0 && model.getReaction(reactionId).getType() != ReactionType.DRAIN) {
				reactions.add(reactionId);
			}
			
		}
		return reactions;
	}
	
	private List<String> intersection(IOmicsContainer measurements, FluxValueMap predictions) {
		List<String> reactions = new ArrayList<String>();
		
		// Calculate the correct overlap between the measurements and the predictions
		for (String reactionId : measurements.getValues().keySet()) {
			
			if (model.getReaction(reactionId) != null && model.getReaction(reactionId).getType() != ReactionType.DRAIN) {
				
				Double measurement =  measurements.getValues().get(reactionId);
				Double prediction = predictions.getValue(reactionId);
				
				if (measurement > -DOUBLE_PRECISSION && measurement < DOUBLE_PRECISSION) { measurement = 0.0; }
				if (prediction > -DOUBLE_PRECISSION && prediction < DOUBLE_PRECISSION) { prediction = 0.0; }
			
				if (measurement < 0.0 && prediction < 0.0) {
					reactions.add(reactionId);
					
				} else if (measurement == 0.0 && prediction == 0.0) {
					reactions.add(reactionId);
				
				} else if (measurement > 0.0 && prediction > 0.0) {
					reactions.add(reactionId);
				}
				
			}
		}
		
		return reactions;
	}
	
	private double mean(List<Double> list) {
		double sum = 0;		
		for(Double value : list) {
			sum += value;
		}		
		return (sum / list.size());
	}
}
