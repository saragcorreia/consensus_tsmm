package pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;
import org.sbml.jsbml.xml.parsers.MathMLStaxParser;
import org.sbml.jsbml.xml.parsers.SBMLCoreParser;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;

public class JSBMLReader implements IContainerBuilder{

	   static {
//	    	Logger.getLogger(JSBML.class).setLevel(Level.OFF);
	    	Logger.getLogger(SBMLCoreParser.class).setLevel(Level.ERROR);
			Logger.getLogger(MathMLStaxParser.class).setLevel(Level.OFF);
			Logger.getLogger(SBMLCoreParser.class).setLevel(Level.ERROR);
	    }
	private static final long serialVersionUID = 1L;
	private Model jsbmlmodel;
	private String biomassId;
	private String organismName;

	protected HashMap<String, ReactionConstraintCI> defaultEC = null;
	protected HashMap<String, CompartmentCI> compartmentList = null;
	protected HashMap<String, MetaboliteCI> metaboliteList = null;
	protected HashMap<String, ReactionCI> reactionList = null;
	protected HashMap<String, GeneCI> genes = null;
	protected Map<String, Map<String, String>> metabolitesExtraInfo = null;
	protected Map<String, Map<String, String>> reactionsExtraInfo = null;
	protected HashMap<String, String> mapMetaboliteIdCompartment = null;
	protected SBMLDocument document;

	protected ArrayList<String> warnings;
	protected boolean checkConsistency;
	protected boolean isPalsson = false;
	protected boolean hasFormula = false;

	private boolean irrevForm = false;

	public JSBMLReader(String filePath, String organismName) throws FileNotFoundException, XMLStreamException,
			ErrorsException {
		this(new FileInputStream(filePath), organismName, true);
	}

	public JSBMLReader(String filePath, String organismName, boolean checkConsistency) throws FileNotFoundException,
			XMLStreamException, ErrorsException {
		this(new FileInputStream(filePath), organismName, checkConsistency);
	}

	public JSBMLReader(InputStream data, String organismName, boolean checkConsistency) throws XMLStreamException,
			ErrorsException {
		this.checkConsistency = checkConsistency;
		SBMLReader reader = new SBMLReader();
		document = reader.readSBMLFromStream(data);
		this.getJSBMLModel();
		this.organismName = organismName;
		warnings = new ArrayList<String>();

		populateInformation();
	}

	public JSBMLReader(String filePath, String organismName, boolean checkConsistency, boolean irrevForm)
			throws FileNotFoundException, XMLStreamException, ErrorsException {
		this(new FileInputStream(filePath), organismName, checkConsistency);
		this.irrevForm = irrevForm;
	}

	/**
	 * This method checks the consistency from the JSBML file and gets the JSBML
	 * model
	 * 
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws ErrorsException
	 */
	private void getJSBMLModel() throws ErrorsException {

		if (checkConsistency) {

			document.setConsistencyChecks(CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
			document.setConsistencyChecks(CHECK_CATEGORY.MODELING_PRACTICE, false);
			document.setConsistencyChecks(CHECK_CATEGORY.UNITS_CONSISTENCY, false);
			document.setConsistencyChecks(CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
			document.setConsistencyChecks(CHECK_CATEGORY.SBO_CONSISTENCY, false);
			document.setConsistencyChecks(CHECK_CATEGORY.OVERDETERMINED_MODEL, false);
			document.setConsistencyChecks(CHECK_CATEGORY.MATHML_CONSISTENCY, false);

			document.checkConsistency();
			System.out.println("CHECK ENDING!!!");
		}

		boolean hasErrors = false;

		for (int i = 0; i < document.getListOfErrors().getNumErrors() && !hasErrors; i++) {
			SBMLError e = document.getError(i);
			if (e.getSeverity().equals("Error"))
				hasErrors = true;
		}

		if (hasErrors) {
			throw new ErrorsException(document);
		}

		Model model = document.getModel();
		this.jsbmlmodel = model;
	}

	/**
	 * @return The model name
	 */
	public String getModelName() {
		return jsbmlmodel.getName();
	}

	/**
	 * @return The model notes
	 */
	public String getNotes() {

		try {
			return jsbmlmodel.getNotesString();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @return The organism name
	 */
	public String getOrganismName() {
		return organismName;
	}

	/**
	 * @param organismName
	 *            The organism name to be set
	 */
	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	/**
	 * @return The model version
	 */
	public Integer getVersion() {
		return Integer.valueOf((int) jsbmlmodel.getVersion());
	}

	/**
	 * This method populates the structures with the information from the JSBML
	 * file
	 * 
	 * @throws Exception
	 */
	private void populateInformation() {

		compartmentList = new HashMap<String, CompartmentCI>();
		genes = new HashMap<String, GeneCI>();
		metaboliteList = new HashMap<String, MetaboliteCI>();
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		reactionList = new HashMap<String, ReactionCI>();
		mapMetaboliteIdCompartment = new HashMap<String, String>();
		metabolitesExtraInfo = new HashMap<String, Map<String, String>>(); // SGC:
																			// Vila�a
																			// porque
																			// n�o
																			// estavam
																			// inicializadas
																			// ?
		reactionsExtraInfo = new HashMap<String, Map<String, String>>();
		readCompartments();
		readMetabolites();
		readReactions();
	}

	/**
	 * This method verifies if the metaboliteID has the metabolite formula
	 * built-in
	 */
	private void verifyFormula(int index) {
		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
		// for(int i=0; i<sbmlspecies.size(); i++){
		Species species = sbmlspecies.get(index);
		String name = species.getName();
		if (name.matches("^[Mm]_.*_[a-zA-Z0-9]*$")) { // if starts with M, the
														// '_', then something,
														// then '_' and finally
														// the formula
			isPalsson = true;
			hasFormula = true;
		} else {
			String notes = "";
			try {
				notes = species.getNotesString();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (notes != "" && notes.contains("FORMULA")) // if the formula are
															// at the species
															// notes
				hasFormula = true;
		}
		// }
	}

	/**
	 * This method reads the compartments from the SBML file
	 */
	public void readCompartments() {
		ListOf<Compartment> sbmllistofcomps = jsbmlmodel.getListOfCompartments();
		for (int i = 0; i < sbmllistofcomps.size(); i++) {
			Compartment comp = sbmllistofcomps.get(i);
			CompartmentCI ogcomp = new CompartmentCI(comp.getId(), comp.getName(), comp.getOutside());
			compartmentList.put(comp.getId(), ogcomp);
		}
	}

	/**
	 * This method reads the metabolites from the SBML file
	 */
	@SuppressWarnings("deprecation")
	public void readMetabolites() {
		// //IHUMAN metabolic model patterns
		Pattern patternKeggId = Pattern.compile("kegg.compound[:/](.*?)\"");
		Pattern patternChebiId = Pattern.compile("CHEBI:(.*?)\"");

		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
		for (int i = 0; i < sbmlspecies.size(); i++) {
			isPalsson = false;
			hasFormula = false;
			verifyFormula(i);

			Species species = sbmlspecies.get(i);
			String idInModel = species.getId();
			// IHUMAN metabolic model - GENEs
			boolean NielsenModel = jsbmlmodel.getId().equals("iHuman2207") || jsbmlmodel.getId().equals("HMRdatabase");
			if (NielsenModel && idInModel.startsWith("E_")) {
				genes.put(idInModel, new GeneCI(idInModel, species.getName()));

			} // END IHUMAN metabolic model
			else {

				String nameInModel = species.getName();
				String compartmentId = species.getCompartment();

				compartmentList.get(compartmentId).addMetaboliteInCompartment(idInModel);

				mapMetaboliteIdCompartment.put(idInModel, compartmentId);
				MetaboliteCI ogspecies = new MetaboliteCI(idInModel, nameInModel);
				ogspecies.setCharge(species.getCharge());

				String formula = "";

				if (isPalsson) { // formula is in species name
					int index = nameInModel.lastIndexOf("_");
					formula = nameInModel.substring(index + 1);
				} else {
					if (hasFormula) { // formula is in species notes
						String notes = "";
						try {
							notes = species.getNotesString();
						} catch (XMLStreamException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (notes != "") {
							Pattern pattern = Pattern.compile("(<html:p>|<p>)FORMULA:(.*?)(</html:p>|</p>)");
							Matcher matcher = pattern.matcher(notes);

							if (matcher.find())
								formula = matcher.group(2).trim();
						}
					}
				}
				if (formula != "")
					ogspecies.setFormula(formula);

				// IHUMAN metabolic model
				String annotation = "";
				try {
					annotation = species.getAnnotationString();
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (annotation != "") {
					Matcher matcher = patternKeggId.matcher(annotation);
					if (matcher.find()) {
						String keggId = matcher.group(1).trim();
						
						if(metabolitesExtraInfo.containsKey(Config.FIELD_KEGG_ID)){

							metabolitesExtraInfo.get(Config.FIELD_KEGG_ID).put(ogspecies.getId(), keggId);
						}
						else{
							Map<String, String> r = new HashMap<String, String>();
							r.put(ogspecies.getId(), keggId);
							metabolitesExtraInfo.put(Config.FIELD_KEGG_ID, r);
						}
					}
					matcher = patternChebiId.matcher(annotation);
					if (matcher.find()) {
						String chebi = matcher.group(1).trim();
						
						if(metabolitesExtraInfo.containsKey(Config.FIELD_CHEBI_ID)){

							metabolitesExtraInfo.get(Config.FIELD_CHEBI_ID).put(ogspecies.getId(), chebi);
						}
						else{
							Map<String, String> r = new HashMap<String, String>();
							r.put(ogspecies.getId(), chebi);
							metabolitesExtraInfo.put(Config.FIELD_CHEBI_ID, r);
						}
					}
				}
				// END IHUMAN metabolic model
				metaboliteList.put(idInModel, ogspecies);
			}
		}
	}

	/**
	 * This method reads the reactions from the SBML file
	 * 
	 * @throws ParseException
	 */
	public void readReactions() {
		Set<String> speciesInReactions = new TreeSet<String>();
		long maxMetabInReaction = 0;
		ListOf<Reaction> sbmlreactions = jsbmlmodel.getListOfReactions();
		for (int i = 0; i < sbmlreactions.size(); i++) {

			Reaction sbmlreaction = sbmlreactions.get(i);
			String reactionId = sbmlreaction.getId();

			ListOf<SpeciesReference> products = sbmlreaction.getListOfProducts();
			ListOf<SpeciesReference> reactants = sbmlreaction.getListOfReactants();

			/** add mappings for products */
			Map<String, StoichiometryValueCI> productsCI = addMapping(products, reactionId, speciesInReactions);

			/** add mappings for reactants */
			Map<String, StoichiometryValueCI> reactantsCI = addMapping(reactants, reactionId, speciesInReactions);

			boolean isReversible = irrevForm ? false : sbmlreaction.getReversible();

			maxMetabInReaction = kinetic(sbmlreaction, isReversible, reactantsCI, productsCI, maxMetabInReaction);

			ReactionCI ogreaction = new ReactionCI(sbmlreaction.getId(), sbmlreaction.getName(), isReversible,
					reactantsCI, productsCI);

			if (products.size() == 0 || reactants.size() == 0) {
				ogreaction.setType(ReactionTypeEnum.Drain);
			} else {
				ogreaction.setType(ReactionTypeEnum.Internal);
			}

			try {
				parserNotes(sbmlreaction.getNotesString(), ogreaction, sbmlreaction);
			} catch (Exception e) {
				warnings.add("Problem in reaction " + ogreaction.getId() + ": " + e.getMessage());
			}

			reactionList.put(reactionId, ogreaction);

			// SGC -- to remove
			// //Create the container with only irreversible reactions
			// if(irrevForm && isReversible){
			// isReversible = false;
			// ReactionCI ogreactionRev = new ReactionCI(reactionId+"_Rev",
			// sbmlreaction.getName(), false,
			// productsCI,reactantsCI);
			// ogreactionRev.setType(ogreaction.getType());
			// defaultEC.put(reactionId+"_Rev", new
			// ReactionConstraintCI(defaultEC.get(reactionId).getLowerLimit(),
			// defaultEC.get(reactionId).getUpperLimit()));
			// reactionList.put(reactionId+"_Rev", ogreactionRev);
			// }

		}

		reactionList.get(biomassId).setType(ReactionTypeEnum.Biomass);
		removeSpeciesNonAssociatedToReactions(speciesInReactions);
	}

	//
	// LIST OF MODIFIERS GENE RULE
	private String getGeneRule(Reaction sbmlReaction) {
		StringBuilder geneRule = new StringBuilder();
		int numberOfModififers = sbmlReaction.getListOfModifiers().size();
		for (int i = 0; i < numberOfModififers; i++) {
			String[] name = sbmlReaction.getModifier(i).getSpeciesInstance().getName().trim().split(":");

			geneRule.append("(");

			for (int j = 0; j < name.length; j++) {
				geneRule.append(name[j]);

				if ((j + 1) < name.length)
					geneRule.append(" and ");
			}

			geneRule.append(")");

			if ((i + 1) < numberOfModififers)
				geneRule.append(" or ");
		}

		String geneReactionAssociation = geneRule.toString();
		geneReactionAssociation = geneReactionAssociation.replaceAll("_", "");
		// geneReactionAssociation =
		// geneReactionAssociation.replaceAll("\u00A0"," ");
		// geneReactionAssociation =
		// geneReactionAssociation.replaceAll("^(\\d+)$","g$1");
		// geneReactionAssociation =
		// geneReactionAssociation.replaceAll("([ (])(\\d+)([ )])","$1g$2$3");

//		 System.out.println("JSBMLReader: Gene Rule: " + geneReactionAssociation);

		return geneReactionAssociation;
	}

	/**
	 * This method adds mapping to reactants and products
	 * 
	 * @param list
	 *            List of reactants or products
	 * @param reactionId
	 *            The reaction ID
	 * @param speciesInReactions
	 *            A set with all the metabolites that participate in some
	 *            reaction
	 * @return The mapping of reactants or products
	 */
	public Map<String, StoichiometryValueCI> addMapping(ListOf<SpeciesReference> list, String reactionId,
			Set<String> speciesInReactions) {
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
		for (int l = 0; l < list.size(); l++) {
			SpeciesReference ref = (SpeciesReference) list.get(l);
			String idInModel = ref.getSpecies();
			result.put(idInModel, new StoichiometryValueCI(idInModel, ref.getStoichiometry(),
					mapMetaboliteIdCompartment.get(idInModel)));
			System.out.println(idInModel);
			metaboliteList.get(idInModel).addReaction(reactionId);
			speciesInReactions.add(idInModel);
		}

		return result;
	}

	/**
	 * This method handles with the kinetic law of the reaction, if it exists
	 * 
	 * @param sbmlreaction
	 *            The reaction
	 * @param isReversible
	 *            The reaction reversibility
	 * @param reactantsCI
	 *            The reactants
	 * @param productsCI
	 *            The products
	 */
	public long kinetic(Reaction sbmlreaction, boolean isReversible, Map<String, StoichiometryValueCI> reactantsCI,
			Map<String, StoichiometryValueCI> productsCI, long maxMetabInReaction) {
		KineticLaw kineticlaw = sbmlreaction.getKineticLaw();

		double lower = -100000;
		double upper = 100000;

		boolean haskinetic = false;
		if (kineticlaw != null) {
			ListOf<LocalParameter> params = kineticlaw.getListOfLocalParameters();// getListOfParameters();

			if (params != null && params.size() > 0) {
				for (int j = 0; j < params.size(); j++) {
					LocalParameter p = params.get(j);
					if (p.getId().equalsIgnoreCase("LOWER_BOUND") || p.getName().equalsIgnoreCase("LOWER_BOUND")) {
						lower = p.getValue();
						if (Double.isInfinite(lower)) {
							lower = 1000;
						}

						haskinetic = true;
					} else if (p.getId().equalsIgnoreCase("UPPER_BOUND") || p.getName().equalsIgnoreCase("UPPER_BOUND")) {
						upper = p.getValue();
						if (Double.isInfinite(upper)) {
							upper = 1000;
						}

						haskinetic = true;
					}
				}
			}
		}

		if (!isReversible && !haskinetic) {
			lower = 0.0;
		}

		long nMetabolitesInReaction = reactantsCI.size() + productsCI.size();
		if (nMetabolitesInReaction > maxMetabInReaction) {
			maxMetabInReaction = nMetabolitesInReaction;
			biomassId = sbmlreaction.getId();
		}

		if (haskinetic)
			defaultEC.put(sbmlreaction.getId(), new ReactionConstraintCI(lower, upper));

		return maxMetabInReaction;
	}

	/**
	 * This method removes the species that aren't associated with any reaction
	 * 
	 * @param speciesInReactions
	 *            A set with all the species that are associated to one ore more
	 *            reactions
	 */
	private void removeSpeciesNonAssociatedToReactions(Set<String> speciesInReactions) {

		List<String> toRemove = new ArrayList<String>();

		for (String metId : metaboliteList.keySet())
			if (!speciesInReactions.contains(metId))
				toRemove.add(metId);

		for (String metId : toRemove) {
			metaboliteList.remove(metId);
		}
	}

	/**
	 * This method returns the external compartment
	 * 
	 * @return A String (the external compartment)
	 */
	public String getExternalCompartment() {
		String toReturn = "";

		for (String compId : compartmentList.keySet()) {

			CompartmentCI comp = compartmentList.get(compId);
			if (comp.getOutside() == null || comp.getOutside().equals("")) {
				toReturn = compId;
				break;
			}
		}

		return toReturn;
	}

	/**
	 * This method parses the notes
	 * 
	 * @param notes
	 *            A String with the notes
	 * @param ogreaction
	 *            A ReactionCI object
	 * @throws InvalidBooleanRuleException
	 * @throws ParseException
	 * @throws utilities.math.language.mathboolean.parser.ParseException
	 */
	private void parserNotes(String notes, ReactionCI ogreaction, Reaction sbmlReaction)
			throws InvalidBooleanRuleException {

		ogreaction.setEc_number(getProteinClass(notes));

		if (jsbmlmodel.getId().equals("MODELID_222668")) {
			// iND750i
			ogreaction.setGeneRule(getGeneRuleThroughLocusiND750(notes));

		} else if (jsbmlmodel.getId().equals("MODELID_2795088") || jsbmlmodel.getId().equals("MODEL1109130000_NATHAN")) {
			// Recon1
			ogreaction.setGeneRule(getGeneRuleThroughLocus(notes));

		} else if (jsbmlmodel.getId().equals("MODEL1109130000")) {
			// Recon2
			ogreaction.setGeneRule(getGeneRule(sbmlReaction));

		} else if (jsbmlmodel.getId().equals("MODEL1311110001")) {
			// Recon2.1
			ogreaction.setGeneRule(getGeneRule(sbmlReaction));

		} else if (jsbmlmodel.getId().equals("iHuman2207") || jsbmlmodel.getId().equals("HMRdatabase")) {
			// gene association
			HashSet<String> geneList = new HashSet<String>();
			String geneRule = "";
			for (ModifierSpeciesReference g : sbmlReaction.getListOfModifiers()) {
				String geneID = g.getSpecies();
				geneList.add(geneID);
				geneRule += geneID + " or ";
			}
			geneRule = geneRule.substring(0, geneRule.length() - 4);
			ogreaction.setGenesIDs(geneList);
			ogreaction.setGeneRule(geneRule);
		} else {
			ogreaction.setGeneRule(getGeneRule(notes));

		}

		ogreaction.setSubsystem(getSubstystem(notes));

		for (String geneId : ogreaction.getGenesIDs()) {

			if (!genes.containsKey(geneId)) {
				genes.put(geneId, new GeneCI(geneId, null));
			}
		}

	}

	private String getGeneRuleThroughLocus(String notes) {

		try {
			String[] notesLines = notes.split("\n");

			List<String> locuses = new ArrayList<String>();
			String geneAnnotation = null;

			for (String line : notesLines) {
				if (line.trim().startsWith("<html:p>LOCUS:")) {
					locuses.add(line);
				} else if (line.trim().startsWith("<html:p>GENE ASSOCIATION:") ||line.trim().startsWith("<html:p>GENE_ASSOCIATION:")) {
					geneAnnotation = line;
				}
			}

			if (geneAnnotation != null) {

				for (String locus : locuses) {
					locus = locus.replace("<html:p>", "");
					locus = locus.replace("#</html:p>", "");

					String[] parts = locus.split("#");

					// Locus
					String locusNumber = parts[0].split(":")[1].replace("_", "");
					// Transcript
					String transcriptNumber = parts[1].split(":")[1];
					// Abbreviation
					String abbreviation = parts[2].split(":")[1];

					System.out.println(abbreviation);
					abbreviation = abbreviation.replace(".", "");
					abbreviation = abbreviation.replace("-", "");
					abbreviation = abbreviation.replace("/", "");

					geneAnnotation = geneAnnotation.replace((locusNumber + "." + transcriptNumber), abbreviation);
				}

				geneAnnotation = geneAnnotation.replace("<html:p>GENE ASSOCIATION: ", "");
				geneAnnotation = geneAnnotation.replace("<html:p>GENE_ASSOCIATION: ", "");
				geneAnnotation = geneAnnotation.replace("</html:p>", "");

				return geneAnnotation.trim();
			} else {
				return "";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private String getGeneRuleThroughLocusiND750(String notes) {
		try {
			String[] notesLines = notes.split("\n");

			List<String> locuses = new ArrayList<String>();
			String geneAnnotation = null;

			for (String line : notesLines) {
				if (line.trim().startsWith("<html:p>LOCUS:")) {
					locuses.add(line);
				} else if (line.trim().startsWith("<html:p>GENE ASSOCIATION:")) {
					geneAnnotation = line;
				}
			}

			if (geneAnnotation != null) {

				for (String locus : locuses) {
					locus = locus.replace("<html:p>", "");
					locus = locus.replace("#</html:p>", "");

					String[] parts = locus.split("#");

					// Locus
					String locusName = parts[0].split(":")[1];

					// Abbreviation
					if (parts.length > 1 && parts[1].split(":")[0].startsWith("ABBREVIATION")) {
						String abbreviation = parts[1].split(":")[1];
						geneAnnotation = geneAnnotation.replace(locusName, abbreviation);
					}
				}

				geneAnnotation = geneAnnotation.replace("<html:p>GENE ASSOCIATION: ", "");
				geneAnnotation = geneAnnotation.replace("</html:p>", "");

				return geneAnnotation;
			} else {
				return "";
			}

		} catch (Exception e) {
			System.out.println(notes.split("\n")[1]);
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * This method gets the gene rule from a String
	 * 
	 * @param notes
	 *            The String with the gene rule
	 * @return A String with the gene rule parsed
	 */
	private String getGeneRule(String notes) {

		Pattern pattern = Pattern.compile("(<html:p>|<p>)GENE[ _]ASSOCIATION:(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String geneReactionAssociation = null;
		if (matcher.find())
			geneReactionAssociation = matcher.group(2).trim();

		// Strange characters found in some models
		if (geneReactionAssociation != null) {
			// System.out.println("Gene Rule: ." + geneReactionAssociation+".");
			geneReactionAssociation = geneReactionAssociation.trim();
			geneReactionAssociation = geneReactionAssociation.replaceAll("\u00A0", " ");
			geneReactionAssociation = geneReactionAssociation.replaceAll("^(\\d+)$", "g$1");
			try {
				geneReactionAssociation = geneReactionAssociation.replaceAll("([ (])(\\d+)([ )])", "$1g$2$3");
			} catch (Exception e) {
			}

			// System.out.println("Gene Rule: " + geneReactionAssociation);
			// System.out.println();
		}
		return geneReactionAssociation;
	}

	/**
	 * This method gets the protein rule from a String
	 * 
	 * @param notes
	 *            The String with the protein rule
	 * @return A String with the protein rule parsed
	 */
	@SuppressWarnings("unused")
	private String getProteinRule(String notes) {

		Pattern pattern = Pattern.compile("(<html:p>|<p>)PROTEIN[ _]ASSOCIATION:(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String proteinReactionAssociation = null;
		if (matcher.find())
			proteinReactionAssociation = matcher.group(2).trim();

		return (proteinReactionAssociation);
	}

	/**
	 * This method gets the protein class from a String
	 * 
	 * @param notes
	 *            The String with the protein class
	 * @return A String with the protein class parsed
	 */
	private String getProteinClass(String notes) {

		Pattern pattern = Pattern.compile("(<html:p>|<p>)PROTEIN[ _]CLASS:(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String proteinClass = null;
		if (matcher.find())
			proteinClass = matcher.group(2).trim();

		return proteinClass;
	}

	/**
	 * This method gets the reaction subsystem from a String
	 * 
	 * @param notes
	 *            The String with the reaction subsystem
	 * @return A String with the reaction subsystem parsed
	 */
	private String getSubstystem(String notes) {
		Pattern pattern = Pattern.compile("(<html:p>|<p>)SUBSYSTEM:(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String subsytem = null;
		if (matcher.find())
			subsytem = matcher.group(2).trim();
		return subsytem;
	}

	/**
	 * @return The biomass ID
	 */
	public String getBiomassId() {
		return biomassId;
	}

	/**
	 * @return A map with the environmental conditions
	 */
	@Override
	public HashMap<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	/**
	 * @return A map with the compartments
	 */
	@Override
	public HashMap<String, CompartmentCI> getCompartments() {
		return compartmentList;
	}

	/**
	 * @return A map with the metabolites
	 */
	@Override
	public HashMap<String, MetaboliteCI> getMetabolites() {
		return metaboliteList;
	}

	/**
	 * @return A map with the reactions
	 */
	@Override
	public HashMap<String, ReactionCI> getReactions() {
		return reactionList;
	}

	/**
	 * @return A map with the genes
	 */
	@Override
	public Map<String, GeneCI> getGenes() {
		return genes;
	}

	/**
	 * @return
	 */
	public HashMap<String, String> getMetaboliteIdToSpecieTermId() {
		return null;
	}

	/**
	 * @return The external compartment ID
	 */
	@Override
	public String getExternalCompartmentId() {
		String ret = null;
		String altrn = null;
		for (CompartmentCI comp : compartmentList.values()) {
			if (comp.getOutside() == null || comp.getOutside().equals("")) {
				ret = comp.getId();
				break;
			}

			if (comp.getId().startsWith("e") || comp.getId().startsWith("E"))
				altrn = comp.getId();
		}

		if (ret == null)
			ret = altrn;
		return ret;
	}

	/**
	 * @return A map with the metabolites extra info
	 */
	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return metabolitesExtraInfo;
	}

	/**
	 * @return A map with the reactions extra info
	 */
	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return reactionsExtraInfo;
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public boolean isIrrevForm() {
		return irrevForm;
	}

	public void setIrrevForm(boolean irrevForm) {
		this.irrevForm = irrevForm;
	}
}
