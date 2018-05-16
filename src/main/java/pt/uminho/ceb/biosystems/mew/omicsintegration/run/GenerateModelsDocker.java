package pt.uminho.ceb.biosystems.mew.omicsintegration.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.EquationFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.FileFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.GenerationModelException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TaskException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TaskFileFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Gene2GeneIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Reac2ReacIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.MetabolicTask;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.TasksReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.FastCoreConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.MBAConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.mCADREConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.configuration.tINITConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods.FastCoreAlgorithm;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods.MBAAlgorithm;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods.mCADREAlgorithm;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods.tINITAlgorithm;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.ITransformOmics;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformDataMapGeneToReac;
import pt.uminho.ceb.biosystems.mew.omicsintegration.transformation.TransformOmicsKeys;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class GenerateModelsDocker {
	public Container templateContainer;
	public ISteadyStateModel templateModel;
	public EnvironmentalConditions env;

	public Condition condition;

	public HashMap<String, Double> discValues = null;
	public Map<String, Set<String>> additionalConvertion = null;
	public String cellLine;
	public Pattern pattern = Pattern.compile(".*_b$");
	SolverType solver = SolverType.GLPK;
	// SolverType solver = SolverType.CPLEX3;
	String modelFile;
	String hpaFile;
	String geneExpFile;
	String barcodeFile;
	String fileMapGenes;
	String confLevelScores;
	String taskFile;
	String fileGeneMap;
	String fileMetaMap;
	String metaFile;
	String coreReacFile;
	String modReacFile;
	String resPath;
	String biomassReaction;
	String convertGeneIdsFile;
	String medium, method, omicData;
	String basedirMBA, coreReacsFinalMBA;
	double cutOff1, cutOff2;
	double[] initCutOff = new double[4];
	int[] mbaNumModels = new int[2];

	public static void main(String[] args) {
		//
		try {
			GenerateModelsDocker nc = new GenerateModelsDocker();
			nc.initVariables(args[0]); // change 1 to docker 
			
			if (args.length > 2) {	
				nc.mbaNumModels[0] = new Integer(args[1]);
				nc.mbaNumModels[1] = new Integer(args[1]) + 1;
				System.out.println("MBA Interval" + nc.mbaNumModels[0] + "--" + nc.mbaNumModels[1]);
			}
			nc.loadModel();
//			if (args[0].equals("-g")) {
//				nc.generateModels();
//			} else if (args[0].equals("-f")) {
//				nc.generateSingleMBAModel();
//			}
				
	
			
				
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GenerateModelsDocker() {
		// CplexParamConfiguration.setWarningStream(null);
		condition = new Condition();
		condition.setProperty(Config.CONDITION_STAGE, "normal");
		condition.setProperty(Config.CONDITION_TISSUE, "cell line");

		// Read CSV with discreteValues
		discValues = new HashMap<String, Double>();
		discValues.put("Not detected", -8.0);
		discValues.put("Low", 10.0);
		discValues.put("Moderate", 15.0);
		discValues.put("Medium", 15.0);
		discValues.put("Strong", 20.0);
		discValues.put("High", 20.0);
	}

	public void initVariables(String fileName) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String current = new java.io.File(".").getCanonicalPath();
		System.out.println("Current dir:" + current);
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);
		FileReader f = new FileReader(fileName);
		BufferedReader b = new BufferedReader(f);
		while (b.ready()) {
			String l = b.readLine();
			if (!l.startsWith("#") && !l.equals("")) {
				String[] line = l.split("=");
				map.put(line[0].trim(), line[1].trim());
			}
		}
		b.close();
		f.close();

		modelFile = map.get("ModelSBMLFile");
		hpaFile = map.get("HPAFile");
		geneExpFile = map.get("GeneExpGFile");

		barcodeFile = map.get("BarcodeFile");
		coreReacFile = map.get("CHFile");
		modReacFile = map.get("CMFile");
		confLevelScores = map.get("ConfLevelScores");
		confLevelScores = confLevelScores.equals("null") ? null : confLevelScores;
		taskFile = map.get("TaskFile");
		taskFile = taskFile.equals("null") ? null : taskFile;
		resPath = map.get("ResultsPath");
		cellLine = map.get("CellLine");
		biomassReaction = map.get("BiomassReaction");
		convertGeneIdsFile = map.get("convertGeneIdsFile");
		convertGeneIdsFile = convertGeneIdsFile.equals("null") ? null : convertGeneIdsFile;
		medium = map.get("Medium");
		method = map.get("Method");
		omicData = map.get("OmicData");
		cutOff1 = new Double(map.get("CutOff1"));
		cutOff2 = new Double(map.get("CutOff2"));

		String[] val = (map.get("tINITCuttOff")).split(";");
		for (int i = 0; i < val.length; i++) {
			initCutOff[i] = new Double(val[i]);
		}
	
		basedirMBA = map.get("basedirMBA");
		coreReacsFinalMBA = map.get("coreReacsFinalMBA");
		String solverId = map.get("solver");
		
		solver = solverId.equals("CPLEX")?SolverType.CPLEX3: SolverType.GLPK;

	}

	public void loadModel() {
		try {
			this.templateContainer = new Container(new JSBMLReader(modelFile, "human", false));
			if (!biomassReaction.equals("null"))
				templateContainer.setBiomassId(biomassReaction);

			templateContainer.removeMetabolites(templateContainer.identifyMetabolitesIdByPattern(pattern));

			templateContainer.putDrainsInReactantsDirection();
			templateModel = ContainerConverter.convert(templateContainer);

			if (medium.equals("RPMI1640"))
				this.env = Medium.getRPM1640Recon1(templateContainer);
			else if (medium.equals("FOLGERMed"))
				this.env = Medium.getFolgerMedRecon1(templateContainer);
			else
				this.env = null;

			templateModel.setBiomassFlux("R_Biomass_Ecoli_core_w_GAM");
			FBA fba = new FBA(templateModel);
			fba.setSolverType(solver);
			fba.setIsMaximization(true);
//			fba.setEnvironmentalConditions(Medium.getFolgerMedRecon1(templateContainer));
			System.out.println("RESULT FBA Recon1");
			System.out.println("RES " + fba.simulate().getFluxValues().get("R_Biomass_Ecoli_core_w_GAM"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateModels() throws Exception {
		// CplexParamConfiguration.setDoubleParam("TiLim", 72000.0);
		// Read the metabolic model
		SpecificModelResult cellLineRes = null;

		if (method.equals("MBA")) {
			cellLineRes = generateModelMBA();
		} else if (method.equals("mCADRE")) {
			cellLineRes = generateModelmCADRE();
		} else if (method.equals("tINIT")) {
			cellLineRes = generateModeltINIT();
		} else if (method.equals("FASTCORE")) {
			cellLineRes = generateModelFastCore();
		} else {
			throw new GenerationModelException("Method not available!");
		}
		Container containerTissue = cellLineRes.getSpecificModel();

		System.out.println("N reaction on template-model:"
				+ cellLineRes.getConfiguration().getTemplateContainer().getReactions().size());
		System.out.println("N reaction on tissue-model:" + containerTissue.getReactions().size());
		System.out.println("N metabolites on tissue-model:" + containerTissue.getMetabolites().size());

	}

	public SpecificModelResult generateModelFastCore() throws Exception {
		SpecificModelResult res = null;
		ReactionDataMap reacScores = null;
		if (omicData.equals("Barcode")) {
			reacScores = getBarcodeScores();
		} else if (omicData.equals("HPA")) {
			reacScores = getHPAScores();
		} else if (omicData.equals("GeneExp")) {
			reacScores = getGeneExpScores();
		} else if (omicData.equals("Sets")) {
			reacScores = getReactionSetsScores();
		}

		Map<String, Double> valuesCore = new HashMap<String, Double>();
		for (Map.Entry<String, Double> entry : reacScores.getMapValues().entrySet()) {
			if (entry.getValue() >= cutOff1) {
				valuesCore.put(entry.getKey(), 1.0);
			}
		}

		ReactionDataMap coreReactions = new ReactionDataMap(valuesCore, templateContainer, condition);
		System.out.println("N core reactions" + coreReactions.getMapValues().size());

		FastCoreConfiguration configuration = new FastCoreConfiguration(templateContainer, solver);
		configuration.setCoreSet(coreReactions);

		FastCoreAlgorithm fc = new FastCoreAlgorithm(configuration);
		res = fc.generateSpecificModel();

		writeReactionIds(resPath + cellLine + "/FASTCORE_" + omicData + ".txt", res);
		return res;
	}

	public SpecificModelResult generateModeltINIT() throws Exception {

		TasksReader tasks = null;
		// load tasks
		if (taskFile != null) {
			tasks = new TasksReader(templateContainer, taskFile, Config.FIELD_ID);
			tasks.load();
		}
		ReactionDataMap reacScores = null;

		if (omicData.equals("Barcode")) {
			reacScores = getBarcodeScores();
			for (Map.Entry<String, Double> entry : reacScores.getMapValues().entrySet()) {
				if (entry.getValue() >= initCutOff[0])
					entry.setValue(20.0);
				else if (entry.getValue() >= initCutOff[1])
					entry.setValue(15.0);
				else if (entry.getValue() >= initCutOff[2])
					entry.setValue(10.0);
				else if (entry.getValue() >= initCutOff[3])
					entry.setValue(-8.0);
			}
		} else if (omicData.equals("HPA")) {
			reacScores = getHPAScores();
		} else if (omicData.equals("GeneExp")) {
			reacScores = getGeneExpScores();
			for (Map.Entry<String, Double> entry : reacScores.getMapValues().entrySet()) {
				if (entry.getValue() >= initCutOff[0])
					entry.setValue(20.0);
				else if (entry.getValue() >= initCutOff[1])
					entry.setValue(15.0);
				else if (entry.getValue() >= initCutOff[2])
					entry.setValue(10.0);
				else if (entry.getValue() >= initCutOff[3])
					entry.setValue(-8.0);
			}
		} else if (omicData.equals("Sets")) {
			reacScores = getReactionSetsScores();
			for (Map.Entry<String, Double> entry : reacScores.getMapValues().entrySet()) {
				if (entry.getValue() >= initCutOff[0])
					entry.setValue(20.0);
				else if (entry.getValue() >= initCutOff[1])
					entry.setValue(15.0);
			}
		}

		System.out.println("Start tinit");

		tINITConfiguration configuration = new tINITConfiguration(templateContainer, solver);
		configuration.setReacScores(reacScores);
		configuration.setMetaWeight(0.0);
		configuration.setTasks(tasks.getTasks());

		tINITAlgorithm tinit = new tINITAlgorithm(configuration);
		System.out.println("generate specific model");
		SpecificModelResult res = tinit.generateSpecificModel();
		System.out.println("N reacs" + res.getSpecificModel().getReactions().size());
		System.out.println("END Generate model");
		writeReactionIds(resPath + cellLine + "/tINIT_" + omicData + ".txt", res);

		return res;

	}

	public SpecificModelResult generateModelMBA() throws Exception {
		SpecificModelResult res = null;
		ReactionDataMap reacScores = null;

		if (omicData.equals("Barcode")) {
			reacScores = getBarcodeScores();
		} else if (omicData.equals("HPA")) {
			reacScores = getHPAScores();
		} else if (omicData.equals("GeneExp")) {
			reacScores = getGeneExpScores();
		} else if (omicData.equals("Sets")) {
			reacScores = getReactionSetsScores();
		}

		Map<String, Double> valuesH = new HashMap<String, Double>();
		ReactionDataMap highReactions = new ReactionDataMap(valuesH, templateContainer, condition);

		Map<String, Double> valuesM = new HashMap<String, Double>();
		ReactionDataMap moderateReactions = new ReactionDataMap(valuesM, templateContainer, condition);

		for (Map.Entry<String, Double> entry : reacScores.getMapValues().entrySet()) {
			if (entry.getValue() >= cutOff1) {
				highReactions.setValue(entry.getKey(), 1.0);
			} else if (entry.getValue() >= cutOff2) {
				moderateReactions.setValue(entry.getKey(), 1.0);
			}
		}
		System.out.println("N HR" + highReactions.getMapValues().size());
		System.out.println("N MR" + moderateReactions.getMapValues().size());

		MBAConfiguration configuration = new MBAConfiguration(templateContainer, solver);
		configuration.setCoreSet(highReactions);
		configuration.setModerateSet(moderateReactions);

		for (int i = mbaNumModels[0]; i < mbaNumModels[1]; i++) {
			System.out.println("MBA " + i);
			MBAAlgorithm mba = new MBAAlgorithm(configuration);

			res = mba.generateSpecificModel();
			writeReactionIds(resPath + cellLine + "/MBA_" + omicData + "_v" + i + ".txt", res);
		}

		return res;

	}

	public SpecificModelResult generateModelmCADRE() {
		SpecificModelResult res = null;
		ReactionDataMap reacScores = null;

		try {
			if (omicData.equals("Barcode")) {
				reacScores = getBarcodeScores();
			} else if (omicData.equals("HPA")) {
				reacScores = getHPAScores();
			} else if (omicData.equals("GeneExp")) {
				reacScores = getGeneExpScores();
			} else if (omicData.equals("Sets")) {
				reacScores = getReactionSetsScores();
			}

			// remove connections cross currency metabolites
			Set<String> ignoreMeta = new HashSet<String>();
			// if (this.modelFile.contains("Recon1")) {
			System.out.println("Recon1 key metabolites");

			// Recon1
			ignoreMeta.add("H2O");
			ignoreMeta.add("H+");// H+
			ignoreMeta.add("Nicotinamide adenine dinucleotide"); // nad
			ignoreMeta.add("Nicotinamide adenine dinucleotide - reduced"); // nadh
			ignoreMeta.add("Nicotinamide adenine dinucleotide phosphate"); // nadp
			ignoreMeta.add("Nicotinamide adenine dinucleotide phosphate - reduced");// nadph
			ignoreMeta.add("ATP");
			ignoreMeta.add("ADP");
			
			// confidence levels
			ReactionDataMap confLevels = new ReactionDataMap();
			if (confLevelScores != null) {
				CSVOmicsReader reader = new CSVOmicsReader(condition, confLevelScores, OmicsDataType.REACTION, 0, 1);
				reader.USER_DELIMITER = ",";
				IOmicsContainer omicComtainer = reader.load();

				Reac2ReacIntegrator r2rIntegrator = new Reac2ReacIntegrator(templateContainer, Config.FIELD_ID,
						Config.FIELD_ID);

				confLevels = r2rIntegrator.convert(omicComtainer);
			} else {
				for (String r : templateContainer.getReactions().keySet())
					confLevels.getMapValues().put(r, 0.0);
			}
			mCADREConfiguration config = new mCADREConfiguration(templateContainer, solver);
			config.setReactionScores(reacScores);
			config.setConfidenceLevel(confLevels);
			config.setIgnoreMeta(ignoreMeta);
			// config.setMetabolicTasks(getKeyMetabolitesTask());

			config.setCutOff(cutOff1);

			// set Medium envConditions
			config.setEnvironmentalConditions(env);

			mCADREAlgorithm alg = new mCADREAlgorithm(config);

			res = alg.generateSpecificModel();
			writeReactionIds(resPath + cellLine + "/mCADRE_" + omicData + ".txt", res);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;

	}

	private static void writeReactionIds(String fileName, SpecificModelResult res) throws IOException {
		FileWriter f = new FileWriter(fileName);

		BufferedWriter b = new BufferedWriter(f);

		for (String reac : res.getSpecificModel().getReactions().keySet()) {
			b.write(reac);
			b.newLine();
		}

		System.out.println("N reactions" + res.getSpecificModel().getReactions().size());
		System.out.println("---END----");

		b.close();
		f.close();

	}

	private static void writeSetIds(String fileName, Set<String> res) throws IOException {
		FileWriter f = new FileWriter(fileName);

		BufferedWriter b = new BufferedWriter(f);

		for (String reac : res) {
			b.write(reac);
			b.newLine();
		}
		b.close();
		f.close();

	}

	private ReactionDataMap getBarcodeScores() throws Exception {
		CSVOmicsReader reader = new CSVOmicsReader(condition, barcodeFile, OmicsDataType.GENE, 0, 1);
		reader.USER_DELIMITER = ",";
		IOmicsContainer geneScores = reader.load();

		if (convertGeneIdsFile != null) {
			Map<String, Set<String>> idsConvertion = readConvertionFile();
			ITransformOmics transform = new TransformOmicsKeys(idsConvertion);
			geneScores = transform.transform(geneScores);
		}

		Gene2GeneIntegrator g2gIntegrator = new Gene2GeneIntegrator(templateContainer, Config.FIELD_ID,
				Config.FIELD_ID);

		GeneDataMap expEvidence = (GeneDataMap) g2gIntegrator.convert(geneScores);

		TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(templateContainer);
		ReactionDataMap scoreReac = (ReactionDataMap) transformDataMap.transform(expEvidence);

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
		ReactionDataMap scoreReac = (ReactionDataMap) transformDataMap.transform(expEvidence);

		return scoreReac;
	}

	private ReactionDataMap getGeneExpScores() throws Exception {
		CSVOmicsReader reader = new CSVOmicsReader(condition, geneExpFile, OmicsDataType.GENE, 0, 1);
		reader.USER_DELIMITER = ",";
		IOmicsContainer geneScores = reader.load();

		Gene2GeneIntegrator g2gIntegrator = new Gene2GeneIntegrator(templateContainer, Config.FIELD_ID,
				Config.FIELD_ID);

		GeneDataMap expEvidence = (GeneDataMap) g2gIntegrator.convert(geneScores);

		TransformDataMapGeneToReac transformDataMap = new TransformDataMapGeneToReac(templateContainer);
		ReactionDataMap scoreReac = (ReactionDataMap) transformDataMap.transform(expEvidence);

		return scoreReac;
	}

	private ReactionDataMap getReactionSetsScores() throws Exception {
		CSVOmicsReader reader = new CSVOmicsReader(condition, coreReacFile, OmicsDataType.REACTION);
		reader.USER_DELIMITER = ";";
		IOmicsContainer reacScores = reader.load();
		Reac2ReacIntegrator r2rIntegrator = new Reac2ReacIntegrator(templateContainer);

		CSVOmicsReader reader2 = new CSVOmicsReader(condition, modReacFile, OmicsDataType.REACTION);
		IOmicsContainer reacScores2 = reader2.load();
		reader.USER_DELIMITER = ";";
		for (String r : reacScores2.getValues().keySet())
			reacScores.getValues().put(r, 0.5);

		return (ReactionDataMap) r2rIntegrator.convert(reacScores);

	}

	public Set<MetabolicTask> getKeyMetabolitesTask2() {
		Set<MetabolicTask> tasks = new HashSet<MetabolicTask>();
		// load tasks
		if (taskFile != null) {
			TasksReader tasksReader = new TasksReader(templateContainer, taskFile, Config.FIELD_ID);
			try {
				tasksReader.load();
				tasks.addAll(tasksReader.getTasks());
			} catch (IOException | TaskFileFormatException | TaskException | EquationFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return tasks;
	}

	public Set<MetabolicTask> getKeyMetabolitesTask() {
		Set<MetabolicTask> tasks = new HashSet<MetabolicTask>();
		MetabolicTask task = new MetabolicTask("KeyMetabolite");
		System.out.println(templateModel.getId());
		System.out.println(templateContainer.getModelName());

		System.out.println("RECON1 key metabolites");
		// Recon1
		task.setInternalMetaConstraint("M_3pg_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_accoa_m", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_akg_m", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_e4p_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_f6p_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_g3p_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_g6p_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_oaa_m", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_pep_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_pyr_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_r5p_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_succoa_m", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_ala_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_arg_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_asn_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_asp_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_gln_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_glu_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_gly_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_pro_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_ser_DASH_L_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_atp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_ctp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_datp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_dctp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_dgtp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_dttp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_gtp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_utp_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_pmtcoa_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_chsterol_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_tag_hs_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_dag_hs_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_mag_hs_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_crm_hs_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_pa_hs_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_pe_hs_c", new Pair<Double, Double>(1.0, 1.0));
		task.setInternalMetaConstraint("M_ps_hs_c", new Pair<Double, Double>(1.0, 1.0));
		tasks.add(task);

		return tasks;
	}

	private void generateSingleMBAModel() {
		try {

			String[] filesReactions;

			// get files with model reactions
			File f = new File(basedirMBA);
			filesReactions = f.list();

			// core Reactions
			Set<String> coreReactions = new HashSet<String>();
			FileReader fr = new FileReader(coreReacsFinalMBA);
			BufferedReader br = new BufferedReader(fr);

			while (br.ready()) {
				coreReactions.add(br.readLine());
			}

			Container container = new Container(new JSBMLReader(modelFile, "Human", false));
			container.removeMetabolites(container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")));
			container.putDrainsInReactantsDirection();
			ISteadyStateModel model = ContainerConverter.convert(container);

			Set<String> reacs = MBAAlgorithm.getFinalModel(model, coreReactions, basedirMBA, filesReactions, solver);
			writeSetIds(resPath + cellLine + "/FINAL_MBA_" + omicData + ".txt", reacs);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Map<String, Set<String>> readConvertionFile() throws Exception {

		Map<String, Set<String>> res = new HashMap<String, Set<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader(convertGeneIdsFile))) {
			while (br.ready()) {
				String line = br.readLine().trim();
				String[] tokens = line.split(Config.USER_DELIMITER);
				if (tokens.length == 2) {
					String[] tokens2 = tokens[1].trim().split(Config.USER_DELIMITER_INSIDE);
					Set<String> val = new HashSet<String>();
					for (int i = 0; i < tokens2.length; i++) {
						val.add(tokens2[i]);
					}
					res.put(tokens[0], val);
				} else {
					throw new FileFormatException("Convertion IDs file not correct format!");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

		return res;
	}
	
	@Test
	public void test(){
		
		
	}
}