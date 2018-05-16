package pt.uminho.ceb.biosystems.mew.omicsintegration.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.CheckTasks;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.TasksReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods.tINITAlgorithm;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class TasksReaderTest {
	private static Container container;
	private static String taskFile;

	@BeforeClass
	public static void initVariables() {
		// Define Data File
		// taskFile =
		// "/Users/Sara/Documents/Projects/PHD_P03_Glioblastoma/MyTASKS.csv";
		// String modelFile =
		// "/Users/Sara/Documents/Metabolic_Models/Human/HMR2.0_with_drains.xml";
		// String modelFile =
		// "/Users/Sara/Documents/Metabolic_Models/Human/HMR2.0.xml";
		taskFile = "/Users/Sara/Documents/Projects/PHD_P03_EBI/ExampleTest/taskFile.txt";
		String modelFile = "/Users/Sara/Documents/Metabolic_Models/TestModel/exp2Drains_cicle.xml";
		Pattern pattern = Pattern.compile(".*x$");
		try {
			container = new Container(new JSBMLReader(modelFile, "human", false));
			if (modelFile.endsWith("HMR2.0.xml"))
				container.removeMetabolites(container.identifyMetabolitesIdByPattern(pattern));

			// container.setBiomassId("R_biomass_components");
			container.setBiomassId("R_V9");

			// put the drains in format X-->
			container.putDrainsInReactantsDirection();
			// container.verifyDepBetweenClass();
			System.out.println("Reactions" + container.getReactions().size());
			System.out.println("Metabolites" + container.getMetabolites().size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	/** General Test for CSVOmicsReader */
	public void testTaskReaderReactionsModelTest() {
		try {
			TasksReader tasks = new TasksReader(container, taskFile, Config.FIELD_NAME);

			tasks.load();
			Map<String, Double> hpa = new HashMap<String, Double>();

			hpa.put("R_V6", 20.0);
			hpa.put("R_V9", 15.0);
			
			ReactionDataMap hpaMap = new ReactionDataMap(hpa, container, null);
			CheckTasks c = new CheckTasks(container, tasks.getTasks(),true, 0.0001, SolverType.CPLEX3);
			Map<String, Set<String>> req = c.getRequiredReactions();

			System.out.println("REQUIRED REACTIONS");
			for (String k : req.keySet()) {
				System.out.println("___________________");
				System.out.println(k);
				for (String reac : req.get(k))
					System.out.println(reac);
			}

//			tINITAlgorithm tinit = new tINITAlgorithm(container, ContainerConverter.convert(container), hpaMap, null,
//					null, req, 0.0);

//			SpecificModelResult res = tinit.generateSpecificModel();
//
//			System.out.println("Final model");
//			for (String k : res.getSpecificModel().getReactions().keySet()) {
//				System.out.println(k);
//			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	/** General Test for CSVOmicsReader */
	public void testTaskReaderReactionsHMR_Persistence() {
		try {
			TasksReader tasks = new TasksReader(container, taskFile, Config.FIELD_NAME);

			tasks.load();
			Map<String, Set<String>> req;
			// req= CheckTasks.getRequiredReactions(container, tasks.getTasks(),
			// true);
			//
			// System.out.println("REQUIRED REACTIONS - CLOSE ALL DRAINS");
			// for (String k : req.keySet()) {
			// System.out.println("___________________");
			// System.out.println(k);
			// for (String reac : req.get(k))
			// System.out.println(reac);
			// }
			CheckTasks c = new CheckTasks(container, tasks.getTasks(), true,0.0001, SolverType.CPLEX3);
			req = c.getRequiredReactions();

			System.out.println("REQUIRED REACTIONS");
			for (String k : req.keySet()) {
				System.out.println("___________________");
				System.out.println(k);
				for (String reac : req.get(k))
					System.out.println(reac);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	// @Test
	/** General Test for CSVOmicsReader */
	public void testTaskReaderReactionsHMR() {
		try {
			TasksReader tasks = new TasksReader(container, taskFile, Config.FIELD_NAME);

			tasks.load();
			Map<String, Set<String>> req;
			// req= CheckTasks.getRequiredReactions(container, tasks.getTasks(),
			// true);
			//
			// System.out.println("REQUIRED REACTIONS - CLOSE ALL DRAINS");
			// for (String k : req.keySet()) {
			// System.out.println("___________________");
			// System.out.println(k);
			// for (String reac : req.get(k))
			// System.out.println(reac);
			// }

			CheckTasks c = new CheckTasks(container, tasks.getTasks(), true,0.0001, SolverType.CPLEX3);
			req = c.getRequiredReactions();

			System.out.println("REQUIRED REACTIONS");
			for (String k : req.keySet()) {
				System.out.println("___________________");
				System.out.println(k);
				for (String reac : req.get(k))
					System.out.println(reac);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
