package pt.uminho.ceb.biosystems.mew.omicsintegration.metabolictasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.TaskFileStructure;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.EquationFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TaskException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TaskFileFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.ParserEntities;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class TasksReader {
	public String USER_DELIMITER = ";";
	public String USER_DELIMITER_INSIDE = ",";

	private Container container;
	private File file;
	private Set<MetabolicTask> tasks;
	private String modelMatchField;
	private boolean isReacIds;


	public TasksReader(Container container, File file,
			String modelMatchField, boolean isReacIds) {
		this.container = container;
		this.file = file;
		this.tasks = new HashSet<MetabolicTask>();
		this.modelMatchField = modelMatchField;
		this.isReacIds = isReacIds;
	}
	public TasksReader(Container container, String fileName,
			String modelMatchField, boolean isReacIds) {
		this.container = container;
		this.file = new File(fileName);
		this.tasks = new HashSet<MetabolicTask>();
		this.modelMatchField = modelMatchField;
		this.isReacIds = isReacIds;
	}

	public TasksReader(Container container, String fileName, String modelMatchField) {
		this(container, fileName, modelMatchField, false);
	}

	public void load() throws FileNotFoundException, IOException, TaskFileFormatException, TaskException, EquationFormatException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			String[] fields;
			String headerFile = br.readLine();

			if (!validateHeader(headerFile)) {
				throw new TaskFileFormatException(
						"Header field names or order are not correct!");
			}
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if (!line.equals("") && !line.startsWith("#")) { // allow empty
																	// rows
					fields = line.split(USER_DELIMITER);

					MetabolicTask task = new MetabolicTask(
							fields[TaskFileStructure.ID.getIndex()]);

					if (fields[TaskFileStructure.FAIL.getIndex()]
							.startsWith("T"))
						task.setFail(true);
					if (isReacIds) {
						// insert input constraints
						insertReactionConstraint(fields, task, false);
						// input output constrains
						insertReactionConstraint(fields, task, true);
						if (fields.length >= TaskFileStructure.EQU_UB
								.getIndex()
								&& !fields[TaskFileStructure.EQU.getIndex()]
										.equals(""))
							insertNewEquation(fields, task);

						// insert Objective reaction
						if (fields.length >= TaskFileStructure.OBJ_REAC
								.getIndex()
								&& !fields[TaskFileStructure.OBJ_REAC
										.getIndex()].equals(""))
							task.setObjectiveReaction(fields[TaskFileStructure.OBJ_REAC
									.getIndex()]);

					} else {
						// insert input constraints
						insertMetabolitesConstraints(fields, task, false);
						// input output constrains
						insertMetabolitesConstraints(fields, task, true);
						if (fields.length >= TaskFileStructure.EQU_UB
								.getIndex()
								&& !fields[TaskFileStructure.EQU.getIndex()]
										.equals(""))
							insertNewEquation(fields, task);

						// insert Objective reaction
						if (fields.length > TaskFileStructure.OBJ_REAC
								.getIndex()
								&& !fields[TaskFileStructure.OBJ_REAC
										.getIndex()].equals("")) {

							String idMetaInTask = fields[TaskFileStructure.OBJ_REAC
									.getIndex()];
							if (idMetaInTask.startsWith("-")) {
								idMetaInTask = idMetaInTask.substring(1).trim();
								task.setMaximization(false);
							}

							// get the correct id
							if (modelMatchField.equals(Config.FIELD_NAME)) {
								idMetaInTask = getMetaIdInModel(idMetaInTask);
							}
							//SGC: MISSIN other fields such as KEGG_ID
							task.setObjectiveMetabolite(idMetaInTask);
						}
					}
					tasks.add(task);
				}
			}
		}
	}

	private void insertMetabolitesConstraints(String[] fields,
			MetabolicTask task, boolean isOut) throws TaskFileFormatException {
		String[] metabolites, lb, ub;
		String idReaction, idMetaInTask = "";
		// compartment;
		double lower, upper;
		if (isOut) {
			metabolites = fields[TaskFileStructure.OUT.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			lb = fields[TaskFileStructure.OUT_LB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			ub = fields[TaskFileStructure.OUT_UB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
		} else {
			metabolites = fields[TaskFileStructure.IN.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			lb = fields[TaskFileStructure.IN_LB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			ub = fields[TaskFileStructure.IN_UB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
		}

		Map<String, String> metaToDrains = null;
		try {
			metaToDrains = container.getMetaboliteToDrain();
		} catch (Exception e) {
			throw new TaskFileFormatException(
					"Container not have drains associated with metabolites");
		}

		// System.out.println(">>>" + fields[2] + "length " +
		// metabolites.length);

		for (int i = 0; i < metabolites.length; i++) {
			// the metabolite names in the CSV file must have . instead,
			// when the name is 2,3-blablabla
			idMetaInTask = metabolites[i].replace(".", ",");
			if (idMetaInTask.equals(""))
				return;
			lower = new Double(lb[i]);
			upper = new Double(ub[i]);
			// compartment = container.getExternalCompartmentId();

			// notation MetaName[compartment] or MetaName (when this name is
			// unique for specie
			if (modelMatchField.equals(Config.FIELD_NAME)) {
				idMetaInTask = getMetaIdInModel(idMetaInTask);
			}
			//SGC: MISSIN other fields such as KEGG_ID
			
			// if drains already exist for this metabolite ok, else create
			// one
			if (metaToDrains.keySet().contains(idMetaInTask)) {
				idReaction = metaToDrains.get(idMetaInTask);
				task.getReacConstraints().addReactionConstraint(idReaction,
						new ReactionConstraint(lower, upper));
			} else {
				// internal metabolite
				if (container.getMetaboliteCompartments(idMetaInTask).size() == 1) {
					task.setInternalMetaConstraint(idMetaInTask,
							new Pair<Double, Double>(lower, upper));
				} else {

					// task.setInternalMetaConstraint(ParserEntities.joinMetaAndCompartment(idMetaInTask,
					// compartment),
					// new Pair<Double, Double>(lower, upper));
					System.out
							.println("[TaskReader 166]- metabolite don't exit in model! "
									+ idMetaInTask
									+ " "
									+ container.getMetaboliteCompartments(
											idMetaInTask).toString());
				}
			}
		}
	}

	private String getMetaIdInModel(String idMetaInTask)
			throws TaskFileFormatException {
		Pattern patttern = Pattern.compile("(.*)?\\[(.*)\\]");
		String metaIdInModel = null;

		Matcher m = patttern.matcher(idMetaInTask);
		if (m.matches()) {
			String nameMeta = m.group(1);
			String compartment = m.group(2);
			// get metaId
			if (!container.getCompartments().keySet().contains(compartment))
				throw new TaskFileFormatException("Compartment " + compartment
						+ "do not exist in metabolic model!");
			metaIdInModel = getMetaIdFromName(
					container.getCompartment(compartment)
							.getMetabolitesInCompartmentID(), nameMeta);
		}
		// the metabolite name is unique
		else {
			metaIdInModel = getMetaIdFromName(container.getMetabolites()
					.keySet(), idMetaInTask);

		}
		if (metaIdInModel == null)
			throw new TaskFileFormatException("Metabolite " + idMetaInTask
					+ "do not exist in metabolic model!");

		return metaIdInModel;
	}

	private String getMetaIdFromName(Set<String> setMetabolitesIds,
			String nameMeta) {
		for (String metId : setMetabolitesIds) {
			if (container.getMetabolite(metId).getName().equals(nameMeta))
				return metId;
		}
		return null;

	}

	private void insertReactionConstraint(String[] fields, MetabolicTask task,
			boolean isOut) throws TaskFileFormatException {
		String[] reactions, lb, ub;
		String idReacInTask;
		double lower, upper;
		if (isOut) {
			reactions = fields[TaskFileStructure.OUT.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			lb = fields[TaskFileStructure.OUT_LB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			ub = fields[TaskFileStructure.OUT_UB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
		} else {
			reactions = fields[TaskFileStructure.IN.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			lb = fields[TaskFileStructure.IN_LB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
			ub = fields[TaskFileStructure.IN_UB.getIndex()]
					.split(USER_DELIMITER_INSIDE);
		}
		// missing lb or ub for reactions used in the task
		if (reactions.length != lb.length || reactions.length != ub.length) {
			throw new TaskFileFormatException(
					"Missing LB or UB for reacton in task " + fields[1]);
		}
		for (int i = 0; i < reactions.length; i++) {
			idReacInTask = reactions[i];
			if (idReacInTask.equals(""))
				return;
			lower = new Double(lb[i]);
			upper = new Double(ub[i]);

			// convert the Reaction name to ID
			if (modelMatchField.equals(Config.FIELD_NAME)) {
				boolean existReac = false;
				for (String id : container.getReactions().keySet()) {
					if (container.getReaction(id).getName()
							.equals(idReacInTask)) {
						idReacInTask = id;
						existReac = true;
						break;
					}
				}
				if (!existReac)
					throw new TaskFileFormatException("Reaction "
							+ idReacInTask + "do not exist in metabolic model!");
			}
			task.getReacConstraints().addReactionConstraint(idReacInTask,
					new ReactionConstraint(lower, upper));
		}
	}

	private void insertNewEquation(String[] fields, MetabolicTask task) throws TaskException, EquationFormatException{
		String[] res = new String[3];
		String[] equations = fields[TaskFileStructure.EQU.getIndex()]
				.split(Config.USER_DELIMITER_INSIDE);
		String[] lb = fields[TaskFileStructure.EQU_LB.getIndex()]
				.split(USER_DELIMITER_INSIDE);
		String[] ub = fields[TaskFileStructure.EQU_UB.getIndex()]
				.split(USER_DELIMITER_INSIDE);

		for (int i = 0; i < equations.length; i++) {
			String reacId = "R_EXTRA_" + i + "_"
					+ fields[TaskFileStructure.ID.getIndex()];
			res = ParserEntities.getElementsInEquation(equations[i]);
			// Build the reaction object
			ReactionCI reaction = new ReactionCI(reacId, reacId,
					res[2].equals("R"), getReactantsOrProducts(res[0], reacId),
					getReactantsOrProducts(res[1], reacId));

			String existReac = reactionExistInContainer(reaction);
			if (existReac != null) {
				reaction.setId(existReac);
			} else {
				task.getAddicionalReactions().add(reaction);
			}
			task.getReacConstraints()
					.addReactionConstraint(
							reacId,
							new ReactionConstraint(new Double(lb[i]),
									new Double(ub[i])));

			System.out.println("NEW EQUATION "+reaction.getId() + " LB:" + lb[i] + " UB:"
					+ ub[i]);
		}

	}

	// Auxiliary methods
	private String reactionExistInContainer(ReactionCI reaction) {
		String res = null;
		String reactionId = reaction.getId();
		Map<String, ReactionCI> reactions = container.getReactions();

		if (reactions.containsKey(reactionId))
			res = reactionId;
		else {
			for (ReactionCI containerReaction : reactions.values()) {
				boolean sameReaction = containerReaction.hasSameStoichiometry(
						reaction, true, false);
				if (sameReaction)
					res = containerReaction.getId();
			}
		}
		return res;

	}

	private Map<String, StoichiometryValueCI> getReactantsOrProducts(
			String reactionPart, String reactionId)throws TaskException,EquationFormatException  {
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
		// System.out.println("reactionPart :" + reactionPart);
		// if the reaction is of type "meta[xpo] -->
		if (!reactionPart.trim().equals("")) {
			// Splits the reactionPart by '+', obtaining all the metabolites and
			// stoichiometric value, if any. allows met1 + + met2
			String[] rOrP = ParserEntities
					.splitReagentsOrProducts(reactionPart);
			for (int i = 0; i < rOrP.length; i++) {
				String[] rOrPParsed = ParserEntities
						.parseCompoundInEqn(rOrP[i]);
				String metabolite = rOrPParsed[1];
				String compartment = rOrPParsed[2];
				if (compartment == null
						&& container.getMetaboliteCompartments(metabolite)
								.size() == 1) {
					compartment = container
							.getMetaboliteCompartments(metabolite).iterator()
							.next();
				}
				if (!container.getCompartments().containsKey(compartment))
					throw new TaskException("The compartment " + compartment
							+ " for metabolite " + metabolite
							+ ", is not defined in the model. ");

				Double d = new Double(rOrPParsed[0]);
				if (modelMatchField.equals(Config.FIELD_NAME)) {
					for (String metId : container.getCompartment(compartment)
							.getMetabolitesInCompartmentID()) {
						if (container.getMetabolite(metId).getName()
								.equals(metabolite)) {
							metabolite = metId;
							break;
						}
					}
				}
				result.put(metabolite, new StoichiometryValueCI(metabolite, d,
						compartment));
			}
		}
		return result;
	}

	// the header of file must have the filedsNames with this name and order:
	private boolean validateHeader(String headerFile) {
		boolean res = true;
		String[] header = headerFile.split(USER_DELIMITER);
		int i = 0;
		while (res && i < header.length) {
			res = res
					&& TaskFileStructure.values()[i].getName()
							.equals(header[i]);
			i++;
		}
		return res;
	}

	public Set<MetabolicTask> getTasks() {
		return tasks;
	}

	public void setTasks(Set<MetabolicTask> tasks) {
		this.tasks = tasks;
	}

	// public static void main(String[] args) {
	// Container cont;
	// try {
	// cont = new Container(
	// new JSBMLReader(
	// "/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/Liver/HepatonetSimulations/hepatonet1_with_drains.v3.xml",
	// "human", false));
	//
	// TasksReader tasks = new TasksReader(
	// cont,
	// "/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/Liver/HepatonetSimulations/metabolicfunctions.csv",
	// Config.FIELD_NAME, false);
	// tasks.load();
	//
	// CheckTasks ct = new CheckTasks(cont, tasks.getTasks());
	// ct.getValidTaskIds(true);
	//
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
}
