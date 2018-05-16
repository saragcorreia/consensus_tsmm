package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.DataSource;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Reac2ReacIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.CSVOmicsReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class Reac2ReacIntegratorTest {
	private static Condition condition;
	private static File reacPresentFile;
	private static Container model;
	private static IOmicsContainer dataContainer;

	@BeforeClass
	public static void initVariables() {
		// condition of omic data
		condition = new Condition();
		condition.setProperty(Config.CONDITION_TISSUE, "liver");
		condition.setProperty(Config.CONDITION_STAGE, "normal");

		reacPresentFile = new File("./test_files/presentReac.txt");

		// Invoke CSVReader for the read the reactions that are flux >0
		CSVOmicsReader reader = new CSVOmicsReader(condition, reacPresentFile.getAbsolutePath(), OmicsDataType.REACTION);

		// load Human model and the Omic container
		try {
			reader.setHasHeader(true);
			dataContainer = reader.load();
			assertEquals("Number of reactions Ids: 7", dataContainer.getValues().size(), 7);

			model = new Container(new JSBMLReader("./test_files/models/iHuman2207_withdrains.xml", "human"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void integrationByIDTest() {
		try {
			// convert the ids to the reaction used in the model using the ID
			// field for the conversion
			Reac2ReacIntegrator integrator = new Reac2ReacIntegrator(model);
			IOmicsDataMap map = integrator.convert(dataContainer);

			assertEquals("Number of match recations 4.", map.getMapValues().size(), 4);

			// confirm the ids values
			assertTrue(map.getMapValues().containsKey("R_5081"));
			assertTrue(map.getMapValues().containsKey("R_5082"));
			assertTrue(map.getMapValues().containsKey("R_5085"));
			assertTrue(map.getMapValues().containsKey("R_5086"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void integrationByNameTest() {
		try {
			// convert the ids to the reaction id used in the model using the
			// Name field for the conversion
			Reac2ReacIntegrator integrator = new Reac2ReacIntegrator(model, Config.FIELD_NAME,
					Config.FIELD_ID);
			IOmicsDataMap map = integrator.convert(dataContainer);

			assertEquals("Number of match reactions 1.", map.getMapValues().size(), 1);

			// confirm the ids values
			assertTrue(map.getMapValues().containsKey("R_5085"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
