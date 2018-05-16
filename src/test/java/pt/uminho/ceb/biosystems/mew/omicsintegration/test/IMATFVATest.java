package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Gene2GeneIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformDataMapGeneToReac;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformDataMapLog;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class IMATFVATest {

	private static final String modelFilePath = "./src/test/resources/models/Recon1.xml";
	private static final String biomassReaction = "R_biomass_reaction";
	private static final SolverType solverType = SolverType.CPLEX3;
	
	private static final String transcriptomicsFile = "./src/test/resources/nci60/rma_normalized.csv";
	private CSVOmicsReader transcriptomicsDataReader;
	
	private static Container container;
	private static  ISteadyStateModel model;
	private EnvironmentalConditions environmentalConditions;

	private StringBuilder SYSTEM_OUT_PRINT = new StringBuilder();
	
	@BeforeClass
	public static void importModel() throws Exception {
		// Intialize the SBML reader for RECON1
		JSBMLReader sbmlReader = new JSBMLReader(modelFilePath, "recon1", false);
		container = new Container(sbmlReader);
		Set<String> met = container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
		container.removeMetabolites(met);
		container.setBiomassId(biomassReaction);
		model = (ISteadyStateModel) ContainerConverter.convert(container);

	}
	
	@Test
	public void runTests() throws Exception {
		System.out.println("Model Imported!");
		
		environmentalConditions = new EnvironmentalConditions();
		environmentalConditions.addReactionConstraint("R_EX_glc_LPAREN_e_RPAREN_", new ReactionConstraint(-999999, -20));
		
		for (String cellLine : CELL_LINES) {
			System.out.println(cellLine);
					
			testTranscriptomics(cellLine); System.out.println("Transcriptomics Done!");
			
			System.out.println("\n");
		}
		
		System.out.println(SYSTEM_OUT_PRINT.toString());
	}
	
	private void testTranscriptomics(String cellLine) throws Exception {
 		SYSTEM_OUT_PRINT.append("Transcriptomics_______\n");
		
		// Initialize the Kuster Transcriptomics Data-Set
		transcriptomicsDataReader = new CSVOmicsReader(new Condition(), transcriptomicsFile, OmicsDataType.GENE);
		transcriptomicsDataReader.DELIMITER_INSIDE_FIELDS = ",";
		transcriptomicsDataReader.USER_DELIMITER = ";";
		transcriptomicsDataReader.setIdColumnIndex(0);
		transcriptomicsDataReader.setHasHeader(true);
		
		// Load Cell Line Transcriptomics data
		transcriptomicsDataReader.setValuesColumnIndex(cellLine);
		IOmicsContainer geneScore = transcriptomicsDataReader.load();
		Gene2GeneIntegrator integrator = new Gene2GeneIntegrator(container, Config.FIELD_ID, Config.FIELD_ID);
		
		GeneDataMap expEvidence = (GeneDataMap) integrator.convert(geneScore);
		
		TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(container);
		IOmicsDataMap scoreReac = transformDataMap.transform(expEvidence);
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(TransformDataMapLog.VAR_BASE, new Double(2.0));
		TransformDataMapLog t = new TransformDataMapLog(properties);
		scoreReac = t.transform(scoreReac);

		double percentile25 = 1.955615122;
		double percentile75 = 2.136516801;
		
		Set<String>downRegulatedGenes = new HashSet<String>();
		Set<String>upRegulatedGenes = new HashSet<String>();
		for(Map.Entry<String, Double> entry : scoreReac.getMapValues().entrySet()){
			if(entry.getValue()>=percentile75){
				upRegulatedGenes.add(entry.getKey());
			}else if(entry.getValue()<= percentile25){
				downRegulatedGenes.add(entry.getKey());				
			}
		}
		
		
		
//		Map<String, Double[]> result = runIMATFVA(downRegulatedGenes, upRegulatedGenes);
//		
//		for (String reactionId : result.keySet()) {
//			System.out.println("\t" + reactionId + "\t" + "[" + result.get(reactionId)[0] + "," + result.get(reactionId)[1] + "]");
//		}
		
		SYSTEM_OUT_PRINT.append("______________________\n");
	}

//	private Map<String,Double[]> runIMATFVA( Set<String> downRegulatedGenes, Set<String>upRegulatedGenes ) throws Exception {
//		
//		// Initialize and Run IMATFVA
//		IMATFVA imat = new IMATFVA(model, downRegulatedGenes,upRegulatedGenes, solverType);
//		imat.setEnvironmentalConditions(environmentalConditions);
//		Map<String, Double[]> result = imat.simulateFVA();
//
//		return result;
//	}
	
	@SuppressWarnings("serial")
	public static Set<String> CELL_LINES = new TreeSet<String>(){{
		add("BR_BT549");
//		add("BREAST_HS578T");
//		add("BREAST_MCF7");
//		add("BREAST_MCF7ADR");
//		add("BREAST_MDAMB231");
//		add("BREAST_MDAMB435");
//		add("BREAST_T47D");
//		add("CNS_SF268");
//		add("CNS_SF295");
//		add("CNS_SF539");
//		add("CNS_SNB19");
//		add("CNS_SNB75");
//		add("CNS_U251");
//		add("COLON_COLO205");
//		add("COLON_HCC2998");
//		add("COLON_HCT116");
//		add("COLON_HCT15");
//		add("COLON_HT29");
//		add("COLON_KM12");
//		add("COLON_SW620");
//		add("LEUK_CCRFCEM");
//		add("LEUK_HL60");
//		add("LEUK_K562");
//		add("LEUK_MOLT4");
//		add("LEUK_RPMI8226");
//		add("LEUK_SR");
//		add("MELAN_LOXIMVI");
//		add("MELAN_M14");
//		add("MELAN_MALME3M");
//		add("MELAN_SKMEL2");
//		add("MELAN_SKMEL28");
//		add("MELAN_SKMEL5");
//		add("MELAN_UACC257");
//		add("MELAN_UACC62");
//		add("NSCLC_A549");
//		add("NSCLC_EKVX");
//		add("NSCLC_H226");
//		add("NSCLC_H23");
//		add("NSCLC_H322M");
//		add("NSCLC_H460");
//		add("NSCLC_H522");
//		add("NSCLC_HOP62");
//		add("NSCLC_HOP92");
//		add("OVAR_IGROV1");
//		add("OVAR_OVCAR3");
//		add("OVAR_OVCAR4");
//		add("OVAR_OVCAR5");
//		add("OVAR_OVCAR8");
//		add("OVAR_SKOV3");
//		add("PROSTATE_DU145");
//		add("PROSTATE_PC3");
//		add("RENAL_7860");
//		add("RENAL_A498");
//		add("RENAL_ACHN");
//		add("RENAL_CAKI1");
//		add("RENAL_RXF393");
//		add("RENAL_SN12C");
//		add("RENAL_TK10");
//		add("RENAL_UO31");
	}};
}
