package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class CSVOmicReaderTest {
	private static Condition condition;
	private static File hpaRenalCancer;

	@BeforeClass
	public static void initVariables() {
		// Define Data File
		hpaRenalCancer = new File("./test_files/hpa/renalCancer.csv");

		// condition of omic data
		condition = new Condition();
		condition.setProperty(Config.CONDITION_TISSUE, "renal cells");
		condition.setProperty(Config.CONDITION_STAGE, "cancerous");

	}

	@Test
	/** General Test for CSVOmicsReader */
	public void testHPAFiles() {
		try {

			// Invoke CSVOmicReader for Protein measurements in HPA
			CSVOmicsReader reader = new CSVOmicsReader(condition, hpaRenalCancer.getAbsolutePath(), OmicsDataType.PROTEIN);
			reader.DELIMITER_INSIDE_FIELDS = ";";
			reader.USER_DELIMITER = ",";
			reader.load();

			assertTrue(true);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	/** Test for CSVOmicsReader Filter function with matching filter */
	public void testHPAFilesWithFilter1() {
		try {
			// Define Filters to Reader
			Map<Integer, String> filter = new HashMap<Integer, String>();
			filter.put(1, "renal cancer");

			// Invoke CSVOmicReader for Protein measurements in HPA
			CSVOmicsReader reader = new CSVOmicsReader(condition, hpaRenalCancer.getAbsolutePath(), OmicsDataType.PROTEIN);
			reader.DELIMITER_INSIDE_FIELDS = ";";
			reader.USER_DELIMITER = ",";
	

			IOmicsContainer dataContainer = reader.load();

			assertTrue(dataContainer.getValues().size() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	/** Test for CSVOmicsReader Filter function with not matching filter */
	public void testHPAFilesWithFilter2() {
		try {
			// Define Filters to Reader
			Map<Integer, String> filter = new HashMap<Integer, String>();
			filter.put(1, "liver");

			// Invoke CSVOmicReader for Protein measurements in HPA
			CSVOmicsReader reader = new CSVOmicsReader(condition, hpaRenalCancer.getAbsolutePath(), OmicsDataType.PROTEIN);
			reader.DELIMITER_INSIDE_FIELDS = ";";
			reader.USER_DELIMITER = ",";

			IOmicsContainer dataContainer = reader.load();

			assertTrue(dataContainer.getValues().size() == 0);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	/** Test for CSVOmicReader for NCI60 */
	public void testReadingProteomicsFromNCI60() {
		try {
			// File of the Proteomics data-set of the NCI-60
			File nci60Proteomics = new File("./test_files/nci60/nci60_proteome.txt");

			Condition condition = new Condition();
			condition.setProperty(Config.CONDITION_STAGE, "cancerous cell lines");

			CSVOmicsReader reader = new CSVOmicsReader(condition, nci60Proteomics.getAbsolutePath(), OmicsDataType.PROTEIN);
			reader.DELIMITER_INSIDE_FIELDS = ";";
			reader.USER_DELIMITER = "\t";
			reader.setIdColumnIndex(1);
			reader.setValuesColumnIndex(57);
			reader.setHasHeader(true);

			IOmicsContainer dataContainer = reader.load();

			// for (String key : dataContainer.getValues().keySet()) {
			// System.out.println(key + "\t" + dataContainer.getValue(key));
			// }

			assertTrue(dataContainer.getValues().size() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
