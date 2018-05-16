package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.HMDBReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class HMDBReaderTest {

	@Test
	/** Test for HMDBReader */
	public void testHMDBFiles() {
		try {
			// Folder containing the metabolites XML files from HMDB
			File hmdbFilesFolder = new File("./test_files/hmdb/");

			// Define Experiment Conditions
			Condition condition = new Condition();
			condition.setProperty(Config.CONDITION_TISSUE, "liver");

			// Invoke the HMDBReader
			HMDBReader reader = new HMDBReader(condition, hmdbFilesFolder.getAbsolutePath());
			IOmicsContainer dataContainer = reader.load();

			assertTrue(dataContainer.getValues().size() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}
