package pt.uminho.ceb.biosystems.mew.omicsintegration.run;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.core.model.components.IStoichiometricMatrix;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.SteadyStateSimulationResult;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.FBA;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.CheckTasks;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.TasksReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.simplification.ContainerSimplification;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class TestModelandTasks {

	private static Container container;
	private static TasksReader tasks;
	private static double threshold = 0.0001;

	public static void main(String[] args) {
		try {
			TestModelandTasks obj = new TestModelandTasks();
			obj.populateInfo();
			obj.simulateTasksWithRemoveRections(null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printStoichiometricMatrix(ISteadyStateModel model, String path) throws IOException {
		FileWriter fw = new FileWriter(path);
		BufferedWriter bw = new BufferedWriter(fw);

		IStoichiometricMatrix matrix = model.getStoichiometricMatrix();
		String colNames = "";
		for (int j = 0; j < matrix.columns(); j++) {
			colNames += "," + model.getReactionId(j);
		}
		bw.write(colNames.substring(1));
		bw.newLine();
		for (int i = 0; i < matrix.rows(); i++) {
			bw.write(model.getMetaboliteId(i));
			for (int j = 0; j < matrix.columns(); j++) {
				bw.write("," + matrix.getValue(i, j));
			}
			bw.newLine();
		}
		bw.close();
		fw.close();

	}

	/**
	 * @throws Exception
	 */
	/**
	 * @throws Exception
	 */
	/**
	 * @throws Exception
	 */
	public void populateInfo() throws Exception {
		String taskFile = "liver_metabolicfunctions_recon1.csv";	
		String modelFile ="Recon1.xml";
		
		container = new Container(new JSBMLReader(modelFile, "human", false));;
		 System.out.println("Num drains" + container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")).size());
		 container.removeMetabolites(container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")));
		 
		System.out.println("reactions before: " + container.getReactions().size());
		System.out.println("genes after: " + container.getGenes().size());
		System.out.println("species after: " + container.getMetabolites().size());
		
		container = ContainerSimplification.simplifyModel(container, SolverType.CPLEX3, 0.0001);
		
		System.out.println("reactions after: " + container.getReactions().size());
		
		tasks = new TasksReader(container, taskFile, Config.FIELD_ID, false);

		tasks.load();
		System.out.println("END POPULATE INFO");
	}


	private void simulateTasksWithRemoveRections(Set<String> reacs) {
		if (reacs != null)
			container.removeReactions(reacs);

		CheckTasks ct = new CheckTasks(container, tasks.getTasks(), true, threshold, SolverType.CPLEX3);
		
		System.out.println("Number of tasks:" + tasks.getTasks().size());
		Set<String> validT = ct.getValidTaskIds(reacs);
		
		System.out.println("Valid" + validT.size());
		System.out.println(validT);

	}



}
