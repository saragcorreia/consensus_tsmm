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
import pt.uminho.ceb.biosystems.mew.omicsintegration.integration.Meta2MetaIntegrator;
import pt.uminho.ceb.biosystems.mew.omicsintegration.io.HMDBReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class Meta2MetaIntegratorTest {
	private static Condition condition;
	private static File hmdbFile;
	private static Container model;
	private static IOmicsContainer dataContainer;

	@BeforeClass
	public static void initVariables() {
		// condition of omic data
		condition = new Condition();
		condition.setProperty(Config.CONDITION_TISSUE, "liver");
		condition.setProperty(Config.CONDITION_STAGE, "normal");

		hmdbFile = new File("./test_files/hmdb/");

		// Invoke HMDBReader for the read the metabolites information
		HMDBReader reader = new HMDBReader(condition, hmdbFile.getAbsolutePath());

		// load Human model and the Omic container
		try {
			dataContainer = reader.load();
			assertEquals("Number of metabolites: 3", dataContainer.getValues().size(), 3);
			
			model = new Container(new JSBMLReader("./test_files/models/iHuman2207_withdrains.xml", "human"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void integrationByIDTest() {
		try {
			// convert the ids to the id metabolite used in the model using the ID field for the conversion
			Meta2MetaIntegrator integrator = new Meta2MetaIntegrator(model);
			IOmicsDataMap map = integrator.convert(dataContainer);
			
			assertEquals("Number of match metabolites: 1 ", map.getMapValues().size(), 1);

			 // confirm the ids values
			 assertTrue(map.getMapValues().containsKey("M_m3362"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void integrationByNameTest() {
		try {
			// convert the ids to the id metabolite used in the model using the Name field for the conversion
			Meta2MetaIntegrator integrator = new Meta2MetaIntegrator(model, Config.FIELD_NAME, Config.FIELD_NAME);
			IOmicsDataMap map = integrator.convert(dataContainer);
			
			// files have only 2 metabolites but with association compartments are 6.
			assertEquals("Number of match metabolites: 6 ", map.getMapValues().size(), 6);

			 // confirm the ids values
			 assertTrue(map.getMapValues().containsKey("M_m3362"));
			 assertTrue(map.getMapValues().containsKey("M_m3363"));
			 assertTrue(map.getMapValues().containsKey("M_m3364"));
			 assertTrue(map.getMapValues().containsKey("M_m18"));
			 assertTrue(map.getMapValues().containsKey("M_m19"));
			 assertTrue(map.getMapValues().containsKey("M_m20"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void integrationByExtraInfoTest() {
		try {
			// convert the ids to the id metabolite used in the model using the Keeg id field for the conversion
			Meta2MetaIntegrator integrator = new Meta2MetaIntegrator(model, Config.FIELD_KEGG_ID, Config.FIELD_KEGG_ID);
			IOmicsDataMap map = integrator.convert(dataContainer);
			
			// files have only 1 metabolite 
			assertEquals("Number of match metabolites: 1 ", map.getMapValues().size(), 1);

			 // confirm the ids values
			 assertTrue(map.getMapValues().containsKey("M_m2378"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}	
	
}
