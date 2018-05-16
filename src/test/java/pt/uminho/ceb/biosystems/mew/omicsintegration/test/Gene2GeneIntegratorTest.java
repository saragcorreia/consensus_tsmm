package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.DataSource;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Gene2GeneIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class Gene2GeneIntegratorTest {

	public static Condition condition;
	public static File hpaFileENSG;
	public static File hpaFileENST;
	public static HashMap<String, Double> discValues;
	public static Map<Integer, String> filter;
	public static Container model;

	@BeforeClass
	public static void initVariables() {
		// Omic data files using ENST and ENSG ids, the model use ENST ids. When
		// gene ids on omic data file are in different format from model, the
		// additional conversion must be used.
		hpaFileENST = new File("./test_files/hpa/normal_tissue-tests-ENST.csv");
		hpaFileENSG = new File("./test_files/hpa/normal_tissue-tests.csv");

		// condition of omic data
		condition = new Condition();
		condition.setProperty(Config.CONDITION_TISSUE, "liver");
		condition.setProperty(Config.CONDITION_STAGE, "normal");

		// Read CSV with discreteValues
		discValues = new HashMap<String, Double>();
		discValues.put("Negative", -8.0);
		discValues.put("None", -8.0);
		discValues.put("Weak", 10.0);
		discValues.put("Low", 10.0);
		discValues.put("Moderate", 15.0);
		discValues.put("Medium", 15.0);
		discValues.put("Strong", 20.0);
		discValues.put("High", 20.0);

		// filter for the HPA lines
		filter = new HashMap<Integer, String>();
		filter.put(1, "liver");
		filter.put(2, "hepatocytes");

		// load Human model
		try {
			model = new Container(
					new JSBMLReader("./test_files/models/iHuman2207_withdrains.xml", "human", false, true));
			System.out.println("end load model");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void simpleConvertionTest() {
		try {
			// Invoke CSVOmicReader for Protein measurements in HPA file
			CSVOmicsReader reader = new CSVOmicsReader(condition, hpaFileENST.getAbsolutePath(), OmicsDataType.PROTEIN, 0, 3);

			reader.DELIMITER_INSIDE_FIELDS = ",";
			reader.USER_DELIMITER = ";";
			IOmicsContainer dataContainer = reader.load();

			// valitate the filter of omic data
			assertEquals("Filter must return 5 lines of hpaFile data", dataContainer.getValues().size(), 6);

			Gene2GeneIntegrator integrator = new Gene2GeneIntegrator(model, Config.FIELD_NAME,
					Config.FIELD_ID);

			IOmicsDataMap map = integrator.convert(dataContainer);

			// convert the ids to the ENST format and gene ids that are present
			// in the model
			assertEquals("Number of genes present in the model", map.getMapValues().size(), 2);

			// confirm the ids values
			assertTrue(map.getMapValues().containsKey("E_1"));
			assertTrue(map.getMapValues().containsKey("E_10"));

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

//	@Test
//	public void useAdditionalConvertionTest() {
//		try {
//
//			// Invoke CSVOmicReader for Protein measurements in HPA
//			CSVOmicsReader reader = new CSVOmicsReader(condition, hpaFileENSG.getAbsolutePath(), OmicsDataType.PROTEIN,
//					DataSource.HPA, 0, 3, discValues, null, filter);
//
//			reader.DELIMITER_INSIDE_FIELDS = ",";
//			reader.USER_DELIMITER = ";";
//			IOmicsContainer dataContainer = reader.load();
//
//			// valitate the filter of omic data
//			assertEquals("Filter must return 2 lines of hpaFile data", dataContainer.getValues().size(), 3);
//
//			assertTrue("Import Gene ENSG00000176891", dataContainer.getValues().containsKey("ENSG00000176891"));
//			assertTrue("Import Gene ENSG00000000005", dataContainer.getValues().containsKey("ENSG00000000005"));
//			assertTrue("Import Gene ENSG00000323274", dataContainer.getValues().containsKey("ENSG00000323274"));
//
//			Map<String, Set<String>> additionalConvertion = new HashMap<String, Set<String>>();
//			Set<String> enst = new HashSet<String>();
//			enst.add("ENST00000323274");
//			enst.add("ENST00000579128");
//			enst.add("ENST00000323250");
//			enst.add("ENST00000323224");
//			enst.add("ENST00000584122");
//			enst.add("ENST00000581920");
//			additionalConvertion.put("ENSG00000176891", enst);
//			Set<String> enst2 = new HashSet<String>();
//			enst.add("ENST00000437788");
//			additionalConvertion.put("ENSG00000000005", enst2);
//
//			Gene2GeneIntegrator integrator = new Gene2GeneIntegrator(model, dataContainer, Config.FIELD_NAME,
//					Config.FIELD_ID, additionalConvertion);
//
//			IOmicsDataMap map = integrator.convert();
//
//			// convert the ids to the ENST format and gene ids that are present
//			// in the model
//			assertEquals("Number of genes present in the model", map.getMapValues().size(), 2);
//
//			// confirm the ids values
//			assertTrue(map.getMapValues().containsKey("E_1"));
//			assertTrue(map.getMapValues().containsKey("E_38"));
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertTrue(false);
//		}
//	}

}
