package pt.uminho.ceb.biosystems.mew.omicsintegration.consensus;

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
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.CheckTasks;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.MetabolicTask;
import pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks.TasksReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.simplification.ContainerSimplification;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class FinalModelBasedOnCombinations {
	public static double threshold = 0.0001;
	String fileName;
	String fileName2;
	String modelRecon;
	String taskFile;
	String fileRes;
	Map<Integer, Set<String>> testMissingTasks;
	Map<Integer, Set<String>> testKOReactions;
	Map<Integer, Container> allContainers;

	public FinalModelBasedOnCombinations(String tasksNotSatisfy, String difReacs, String templatemodel, String tasks,
			String resFile) {
		this.fileName = tasksNotSatisfy;
		this.fileName2 = difReacs;
		this.modelRecon = templatemodel;
		this.taskFile = tasks;
		this.fileRes = resFile;

		this.testMissingTasks = new HashMap<Integer, Set<String>>();
		this.testKOReactions = new HashMap<Integer, Set<String>>();
		this.allContainers = new HashMap<Integer, Container>();

	}

	public Container getPerfectContainer() throws Exception {
		Set<String> toRemove = new HashSet<String>();

		// test for each combination of models what set of reaction can be
		// removed without impact the satisfied tasks

		Container container = null;
		for (int i = 0; i < testMissingTasks.size(); i++) {

			container = allContainers.get(i);
			System.out.println("container size : " + container.getReactions().size());
			// remove reaction with no flux in the original container
			container = ContainerSimplification.simplifyModel(container, SolverType.CPLEX3, threshold);
			System.out.println("container size : " + container.getReactions().size());

			TasksReader tasks = new TasksReader(container, taskFile, Config.FIELD_ID, false);
			tasks.load();

			Set<MetabolicTask> tasksToTest = getTasksToTest(tasks.getTasks(), i);
			Set<String> KOreaction = testKOReactions.get(i);

			KOreaction.addAll(toRemove);
			System.out.println("KO ALl: " + KOreaction.size());

			KOreaction.retainAll(container.getReactions().keySet()); // test only the KO that are present in model
			System.out.println("KO in model: " + KOreaction.size());

			toRemove = new HashSet<String>();
			for (String ko : KOreaction) {

				if (allValidTasks(container, ko, tasksToTest)) {
					container.removeReaction(ko);
					toRemove.add(ko);
				}
			}

			String s = "";
			for (String r : toRemove) {
				s = s + r;
			}
			System.out.println("to remove iter " + i + "-->" + s);

		}
		return container;

	}

	private Set<MetabolicTask> getTasksToTest(Set<MetabolicTask> allTasks, int i) {
		Set<MetabolicTask> tasksToTest = new HashSet<MetabolicTask>();
		for (MetabolicTask t : allTasks) {
			if (testMissingTasks.get(i).contains(t.getId())) {
				tasksToTest.add(t);
				if (tasksToTest.size() == testMissingTasks.get(i).size())
					break;
			}
		}
		return tasksToTest;

	}

	private boolean allValidTasks(Container container, String ko, Set<MetabolicTask> tasks)
			throws InvalidSteadyStateModelException, Exception {
		CheckTasks ct = new CheckTasks(container, tasks, true, threshold, Medium.getFolgerMedRecon1(container),
				SolverType.CPLEX3); // Attention: close / not close drains

		HashSet<String> koSet = new HashSet<String>();
		koSet.add(ko);
		return ct.isAllValidWhithKO(koSet);
	}

	private void populateInfo() {
		// carregar informaçao presente em 2 ficheiros. 1 com a lista de
		// reacções que são removidas entre combinação de modelos e outro com a
		// lista de tasks que deixams de ser válidas
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while (br.ready()) {
				line = br.readLine();
				String[] tks = line.split(";");
				String[] tks2 = tks[1].split(",");
				Set<String> val = new HashSet<String>();
				for (int i = 0; i < tks2.length; i++)
					val.add(tks2[i].trim());

				testMissingTasks.put(new Integer(tks[0]), val);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// second file
		try (BufferedReader br = new BufferedReader(new FileReader(fileName2))) {
			String line;
			while (br.ready()) {
				line = br.readLine();
				String[] tks = line.split(";");
				String[] tks2 = tks[1].split(",");
				Set<String> val = new HashSet<String>();
				for (int i = 0; i < tks2.length; i++)
					val.add(tks2[i].trim());

				testKOReactions.put(new Integer(tks[0]), val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//load partial models
		String base ="Consensus/pModels_Hepatocytes/";
		allContainers.put(0, loadContainer(base+"/comb5.xml"));
		allContainers.put(1, loadContainer(base+"/comb4.xml"));
		allContainers.put(2, loadContainer(base+"/comb3.xml"));
		allContainers.put(3, loadContainer(base+"/comb2.xml"));
		allContainers.put(4, loadContainer(base+"/comb1.xml"));
	}

	public Container loadContainer(String fileName) {
		Container container = null;
		try {
			container = new Container(new JSBMLReader(fileName, "human", false));
			container.removeMetabolites(container.identifyMetabolitesIdByPattern(Pattern.compile(".*_b$")));
			container.putDrainsInReactantsDirection();

			System.out.println("Model size" + container.getReactions().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;

	}

	public static void main(String[] args) {
		try {
			String fileName = "Consensus/hepatocytes_tasksNotSatisfy.txt";
			String fileName2 = "Consensus/hepatocytes_difReactions.txt";
			String modelRecon = "TemplateModels/Recon1.xml";
			String taskFile = "Tasks/liver_metabolicfunctions_recon1.csv";
			String fileRes = "ConsensusReactions.txt";

			FinalModelBasedOnCombinations t = new FinalModelBasedOnCombinations(fileName, fileName2, modelRecon,
					taskFile, fileRes);
			t.populateInfo();
			Container newCont = t.getPerfectContainer();

			System.out.println("FINAL MODEL");

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileRes))) {
				for (String r : newCont.getReactions().keySet()) {
					bw.write(r);
					bw.newLine();
				}
			}

			System.out.println("container size: " + newCont.getReactions().size());

			System.out.println(newCont.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
