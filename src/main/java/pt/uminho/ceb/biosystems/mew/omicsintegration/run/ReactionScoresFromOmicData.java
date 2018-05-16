package pt.uminho.ceb.biosystems.mew.omicsintegration.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Gene2GeneIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Reac2ReacIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.ITransformOmics;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformDataMapGeneToReac;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformOmicsKeys;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;

public class ReactionScoresFromOmicData {

	public Set<String> cellLines;
	public String baseDir = "/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/Liver/";
	public String coreReacFile;
	public String modReacFile;

	public Container templateContainer;
	public ISteadyStateModel templateModel;
	public EnvironmentalConditions env;

	public Condition condition;

	public HashMap<String, Double> discValues = null;
	public Map<String, Set<String>> additionalConvertion = null;
	public String cellLine;
	public Pattern pattern = Pattern.compile(".*_b$");

	String modelFile;
	String hpaFile;
	String geneExpFile;
	String barcodeFile;
	// String fileMapGenes;
	String confLevelScores;
	String taskFile;
	String fileGeneMap;
	String fileMetaMap;
	String metaFile;
	String resPath;
	String biomassReaction;
	String convertGeneIdsFile;
	String medium, method, omicData;
	double cutOff1, cutOff2, cutOffMerge;
	double[] initCutOff = new double[4];
	int numMinRequiredMerge;

	public ReactionScoresFromOmicData() {

		modelFile = baseDir + "Recon2.xml";
		biomassReaction = "R_biomass_reaction";

		hpaFile = baseDir + "OmicData/hpa_liver_hepatocytes_supportive_genes.csv";
		barcodeFile = baseDir + "OmicData/barcode_hepatocyte_HGU133plus2_cells_v3_genes_mean.csv";
		coreReacFile = baseDir + "OmicData/MBA_HighReactions.txt";
		modReacFile = baseDir + "OmicData/MBA_ModerateReactions.txt";
		convertGeneIdsFile = null;

		// fileMapGenes = baseDir + "OmicData/ESNG_to_SYMBOL.txt";
		resPath = baseDir + "Results/";
		cellLine = "hepatocytes";

		medium = "";
		method = "tINIT";
		omicData = "HPA";
		cutOff1 = 0.0;
		cutOff2 = 0.0;
		cutOffMerge = 0.0;
		numMinRequiredMerge = 2;
		initCutOff[0] = 20.0;
		initCutOff[1] = 15.0;
		initCutOff[2] = 10.0;
		initCutOff[3] = -8.0;

		condition = new Condition();
		condition.setProperty(Config.CONDITION_STAGE, "normal");
		condition.setProperty(Config.CONDITION_TISSUE, "liver");

		// Read CSV with discreteValues
		discValues = new HashMap<String, Double>();
		discValues.put("Negative", -8.0);
		discValues.put("None", -8.0);
		discValues.put("Not detected", -8.0);
		discValues.put("Low", 10.0);
		discValues.put("Moderate", 15.0);
		discValues.put("Medium", 15.0);
		discValues.put("Strong", 20.0);
		discValues.put("High", 20.0);

	}

	public void loadModel() {
		try {
			this.templateContainer = new Container(new JSBMLReader(modelFile, "Human", false));
			templateContainer.setBiomassId(biomassReaction);
			templateContainer.removeMetabolites(templateContainer.identifyMetabolitesIdByPattern(pattern));
			templateContainer.putDrainsInReactantsDirection();

			// Utils.adaptContainer(templateContainer, 0.0, 1000);
			templateModel = ContainerConverter.convert(templateContainer);

			if (medium.equals("RPMI1640"))
				this.env = Medium.getRPM1640Recon1(templateContainer);
			else if (medium.equals("RPMI1640"))
				this.env = Medium.getFolgerMedRecon1(templateContainer);
			else
				this.env = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {
			ReactionScoresFromOmicData nc = new ReactionScoresFromOmicData();
			nc.loadModel();
			nc.generateModels();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void generateModels() throws Exception {

		calOverlapOmics();

	}

	public void calOverlapOmics() {
		try {
			// BARCODE
			ReactionDataMap reacScores = getBarcodeScores();
			printReacIds(reacScores, resPath + "barcodeScores.csv");
			// HPA
			ReactionDataMap reacScores2 = getHPAScores();
			printReacIds(reacScores2, resPath + "HPAScores.csv");
			// GENE EXP
			// ReactionDataMap reacScores3 = getGeneExpScores();

			// Sets
			ReactionDataMap reacScores4 = getReactionSetsScores();
			printReacIds(reacScores4, resPath + "setsScores.csv");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void printReacIds(ReactionDataMap values, String fileName) throws Exception {
		FileWriter f = new FileWriter(fileName);
		BufferedWriter b = new BufferedWriter(f);
		for (Map.Entry<String, Double> par : values.getMapValues().entrySet()) {
			b.write(par.getKey() + "," + par.getValue());
			b.newLine();
		}
		b.close();
		f.close();
	}

	private ReactionDataMap getBarcodeScores() throws Exception {
		CSVOmicsReader reader = new CSVOmicsReader(condition, barcodeFile, OmicsDataType.GENE, 0, 1);
		reader.USER_DELIMITER = ",";
		IOmicsContainer geneScores = reader.load();

		Map<String, Set<String>> idsConvertion = readConvertionFile();
		ITransformOmics transform = new TransformOmicsKeys(idsConvertion);
		geneScores = transform.transform(geneScores);

		Gene2GeneIntegrator g2gIntegrator = new Gene2GeneIntegrator(templateContainer, Config.FIELD_NAME,
				Config.FIELD_ID);
		
		GeneDataMap expEvidence = (GeneDataMap) g2gIntegrator.convert(geneScores);
		
		TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(templateContainer);
		ReactionDataMap scoreReac =  (ReactionDataMap) transformDataMap.transform(expEvidence);
		
		return scoreReac;
	}

	private ReactionDataMap getHPAScores() throws Exception {

		CSVOmicsReader reader = new CSVOmicsReader(condition, hpaFile, OmicsDataType.PROTEIN, 0, 1, discValues);

		reader.USER_DELIMITER = ",";
		IOmicsContainer hpa = reader.load();
		if (convertGeneIdsFile != null) {
			Map<String, Set<String>> idsConvertion = readConvertionFile();
			ITransformOmics transform = new TransformOmicsKeys(idsConvertion);
			hpa = transform.transform(hpa);
		}
		
		
		Gene2GeneIntegrator g2gIntegrator = new Gene2GeneIntegrator(templateContainer, Config.FIELD_ID,
				Config.FIELD_ID);
		
		GeneDataMap expEvidence = (GeneDataMap) g2gIntegrator.convert(hpa);
		
		TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(templateContainer);
		ReactionDataMap scoreReac =  (ReactionDataMap) transformDataMap.transform(expEvidence);
		
		return scoreReac;
	}

	private ReactionDataMap getGeneExpScores() throws Exception {
		CSVOmicsReader reader = new CSVOmicsReader(condition, geneExpFile, OmicsDataType.GENE, 0, 1);
		reader.USER_DELIMITER = ";";
		IOmicsContainer geneScores = reader.load();
		
		Gene2GeneIntegrator g2gIntegrator = new Gene2GeneIntegrator(templateContainer, Config.FIELD_ID,
				Config.FIELD_ID);
		
		GeneDataMap expEvidence = (GeneDataMap) g2gIntegrator.convert(geneScores);
		
		TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(templateContainer);
		ReactionDataMap scoreReac =  (ReactionDataMap) transformDataMap.transform(expEvidence);
		

		return scoreReac;
	}

	private ReactionDataMap getReactionSetsScores() throws Exception {
		CSVOmicsReader reader = new CSVOmicsReader(condition, coreReacFile, OmicsDataType.REACTION);
		reader.USER_DELIMITER = ";";
		IOmicsContainer reacScores = reader.load();
		Reac2ReacIntegrator g2rIntegrator = new Reac2ReacIntegrator(templateContainer);

		CSVOmicsReader reader2 = new CSVOmicsReader(condition, modReacFile, OmicsDataType.REACTION);
		IOmicsContainer reacScores2 = reader2.load();
		reader.USER_DELIMITER = ";";
		for (String r : reacScores2.getValues().keySet())
			reacScores.getValues().put(r, 0.5);

		return (ReactionDataMap) g2rIntegrator.convert(reacScores);

	}

	private Map<String, Set<String>> readConvertionFile() throws Exception {

		Map<String, Set<String>> res = new HashMap<String, Set<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader(convertGeneIdsFile))) {
			while (br.ready()) {
				String line = br.readLine().trim();
				String[] tokens = line.split(Config.USER_DELIMITER);
				if (tokens.length == 2) {
					Set<String> val = new HashSet<String>();
					val.add(tokens[1]);
					res.put(tokens[0], val);
				} else {
					// throw new FileFormatException("Convertion IDs file not
					// correct format!");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return res;
	}


}
