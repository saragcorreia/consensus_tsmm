package pt.uminho.ceb.biosystems.mew.omicsintegration.run;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.core.model.components.EnvironmentalConditions;
import pt.uminho.ceb.biosystems.mew.core.model.components.ReactionConstraint;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.components.FluxValueMap;
import pt.uminho.ceb.biosystems.mew.core.simulation.fva.FBAFluxVariabilityAnalysis;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Medium;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Utils;
import pt.uminho.ceb.biosystems.mew.solvers.SolverType;

public class CompareHumanModels {

	public Container container1;
	public Container container2;
	public int comparisonType;

	public CompareHumanModels(String sbmlModel1) throws FileNotFoundException, IOException, XMLStreamException,
			ErrorsException {

		this.container1 = new Container(new JSBMLReader(sbmlModel1, "Human", false));
		// container1.putDrainsInReactantsDirection();
	}

	public CompareHumanModels(String sbmlModel1, String sbmlModel2, int compType) throws FileNotFoundException,
			IOException, XMLStreamException, ErrorsException {

		this.container1 = new Container(new JSBMLReader(sbmlModel1, "Human", false));
		this.container2 = new Container(new JSBMLReader(sbmlModel2, "Human", false));
		this.comparisonType = compType;

	}

	public Container getContainer1() {
		return container1;
	}

	public Container getContainer2() {
		return container2;
	}

	public static void main(String[] args) {
		// MAC machine
		String baseDir = "/Users/Sara/Documents/";
		String hmrFile = baseDir + "Metabolic_Models/Human/HMR2.0_with_drains.xml";
		String recon1File = baseDir + "Metabolic_Models/Human/Recon1_with_drains.xml";
		String recon2File = baseDir + "Metabolic_Models/Human/Recon2.xml";
		Pattern pattern = Pattern.compile(".*_b$");
		CompareHumanModels nc;
		try {
			// Liver
			pattern = Pattern.compile(".*_s_b$");
			nc = new CompareHumanModels(
					"/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/Liver/HepatonetSimulations/hepatonet1_with_drains.v3.xml");
			nc.getContainer1().removeMetabolites(nc.getContainer1().identifyMetabolitesIdByPattern(pattern));
			nc.getModelStatistics();

			// validate ATP
			EnvironmentalConditions env = Medium.getCloseAllDrains(nc.container1);
			env.put("R_EX_HC00040_s_", new ReactionConstraint(-0.0322581, 0.0322581));// glucose
			// env.put("R_EX_HC00226_s_", new ReactionConstraint(-1000.0,
			// 0.0));// Palmitate
			env.put("R_EX_HC00017_s_", new ReactionConstraint(-0.193548, -0.193548));// O2
			env.put("R_EX_HC00021_s_", new ReactionConstraint(0.193, 0.194));// CO2
			env.put("R_EX_HC00011_s_", new ReactionConstraint(1.193, 1.194)); // H2O
			// env.put("r1116", new ReactionConstraint(0.0, 10.0));// ATP
			env.put("R_EX_HC00018_s_", new ReactionConstraint(-1.0, 0.0)); // ADP
			env.put("R_EX_HC00019_s_", new ReactionConstraint(-1.0, 0.0)); // PI
			nc.printDrainsFBA(Utils.simulate(nc.getContainer1(), "R_EX_HC00012_s_", env).getFluxValues());

			// getFVA(nc.getContainer1().getReactions().keySet(),
			// ContainerConverter.convert(nc.getContainer1()), ""
			// +
			// "/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/Liver/HepatonetSimulations/fva.txt",
			// null);

			// // HMR
			// nc = new CompareHumanModels(hmrFile);
			// nc.getContainer1().setBiomassId("R_biomass_components");
			// System.out.println("\n\n HMR");
			// nc.getModelStatistics();

			// Simulations
			// nc.printSimulations(false);
			// getFVAStatistics(ContainerConverter.convert(nc.getContainer1()),
			// null);

			// // Recon1
			// nc = new CompareHumanModels(recon1File);
			// nc.getContainer1().removeMetabolites(nc.getContainer1().identifyMetabolitesIdByPattern(pattern));
			// nc.getContainer1().setBiomassId("R_BIOMASS");
			// Utils.adaptContainer(nc.getContainer1(), 0.0, 1000);
			// System.out.println("\n\n Recon1");
			// nc.getModelStatistics();
			//
			// // Simulations
			// nc.printSimulations(true);
			// nc.effectUptakeLimitation();
			// getFVAStatistics(ContainerConverter.convert(nc.getContainer1()),
			// null);
			//
			// // Recon2
			// nc = new CompareHumanModels(recon2File);
			// nc.getContainer1().setBiomassId("R_biomass_reaction");
			// Utils.adaptContainer(nc.getContainer1(), 0.0, 1000);
			// System.out.println("\n\n Recon2");
			// nc.getModelStatistics();
			// //
			// nc.printSimulations(true);
			// nc.effectUptakeLimitation();
			// System.out.println("Recon2");
			// getFVAStatistics(ContainerConverter.convert(nc.getContainer1()),
			// null);

			// nc = new CompareHumanModels(recon1File, recon2File, 1);
			// nc.getContainer1().removeMetabolites(nc.getContainer1().identifyMetabolitesIdByPattern(pattern));
			// System.out.println("N� de metabolito intersetados" +
			// nc.intersectMetabolites().keySet().size());
			// System.out.println("N� de rea��es intersetadas" +
			// nc.intersectReactions().keySet().size());
			//
			// nc = new CompareHumanModels(recon1File, hmrFile, 2);
			// nc.getContainer1().removeMetabolites(nc.getContainer1().identifyMetabolitesIdByPattern(pattern));
			// System.out.println("N� de metabolito intersetados" +
			// nc.intersectMetabolites().keySet().size());
			// System.out.println("N� de rea��es intersetadas" +
			// nc.intersectReactions().keySet().size());

			// nc = new CompareHumanModels(recon2File, hmrFile, 3);
			// System.out.println("N� de metabolito intersetados" +
			// nc.intersectMetabolites().keySet().size());
			// System.out.println("N� de rea��es intersetadas" +
			// nc.intersectReactions().keySet().size());

			// percursores
			// nc.hasBiomassCompoundFlux(BiomassEquation.getDrainsPercursorBiomassRecon1(),
			// Medium.getRPM1640Recon1(nc.getContainer1()));
			// nc.hasBiomassCompoundFlux(BiomassEquation.getDrainsPercursorBiomassRecon1(),
			// Medium.getFolgerMedRecon1(nc.getContainer1()));
			//
			// nc.hasBiomassCompoundFlux(BiomassEquation.getDrainsPercursorBiomassRecon2(),
			// Medium.getRPM1640Recon1(nc.getContainer1()));
			// nc.hasBiomassCompoundFlux(BiomassEquation.getDrainsPercursorBiomassRecon2(),
			// Medium.getFolgerMedRecon1(nc.getContainer1()));

			// print drains
			// nc.printDrainsFBA("R_biomass_reaction",
			// Medium.getRPM1640Recon1(nc.getContainer1()));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void printSimulations(boolean isRecon) throws Exception {
		String biomassReac = container1.getBiomassId();
		EnvironmentalConditions folgerMed = isRecon ? Medium.getFolgerMedRecon1(container1) : Medium
				.getFolgerMedHMR2(container1);
		EnvironmentalConditions rpmMed = isRecon ? Medium.getRPM1640Recon1(container1) : Medium
				.getRPM1640HMR2(container1);

		System.out.println("Biomass open:");
		printDrainsFBA(Utils.simulate(container1, biomassReac, null).getFluxValues());
		System.out.println("Biomass rpm1640:");
		printDrainsFBA(Utils.simulate(container1, biomassReac, rpmMed).getFluxValues());
		System.out.println("Biomass Folger:");
		printDrainsFBA(Utils.simulate(container1, biomassReac, folgerMed).getFluxValues());
		System.out.println("Biomass all close:");
		printDrainsFBA(Utils.simulate(container1, biomassReac, Medium.getCloseAllDrains(container1)).getFluxValues());
	}

	public void effectMediumUptakeLimitation(EnvironmentalConditions env) throws Exception {

		ArrayList<Double> factores = new ArrayList<Double>();
		factores.add(0.01);
		factores.add(0.1);
		factores.add(1.0);
		factores.add(10.0);
		factores.add(100.0);

		for (Map.Entry<String, ReactionConstraint> cons : env.entrySet()) {

			if (cons.getValue().getUpperLimit() == 0.0) {
				double oldValue = cons.getValue().getLowerLimit();
				String biomassValues = "";
				for (double f : factores) {
					cons.getValue().setLowerLimit(oldValue * f);
					biomassValues += Utils.simulate(container1, container1.getBiomassId(), env).getFluxValues()
							.getValue(container1.getBiomassId())
							+ ";";

				}
				System.out.println(cons.getKey() + ";" + biomassValues);

				cons.getValue().setLowerLimit(oldValue);

			}
		}
	}

	public void effectUptakeLimitation() throws Exception {
		EnvironmentalConditions folgerMed = Medium.getFolgerMedRecon1(container1);
		EnvironmentalConditions rpmMed = Medium.getRPM1640Recon1(container1);
		System.out.println("RPM MED");
		effectMediumUptakeLimitation(rpmMed);

		System.out.println("Folger MED");
		effectMediumUptakeLimitation(folgerMed);

	}

	public void printDrainsFBA(FluxValueMap fluxes) throws Exception {
		System.out.println("Uptake / Export");
		for (Map.Entry<String, Double> f : fluxes.entrySet()) {
			if (f.getValue() < 0.0 && container1.getReaction(f.getKey()).isDrain()) {
				System.out.println(f.getKey() + "-->" + f.getValue());
			}
			if (f.getValue() > 0.0 && container1.getReaction(f.getKey()).isDrain()) {
				System.out.println(f.getKey() + "-->" + f.getValue());
			}
		}
	}

	public void hasBiomassCompoundFlux(Set<String> reacs, EnvironmentalConditions env) throws Exception {
		Map<String, Set<String>> listReactions = new HashMap<String, Set<String>>();

		for (String r : reacs) {
			listReactions.put(r, new HashSet<String>());
			System.out.println(r);
			if (container1.getReactions().keySet().contains(r)) {
				Map<String, Double> fluxes = Utils.simulate(container1, r, env).getFluxValues();
				System.out.println(r + " flux model:" + fluxes.get(r));
				for (Map.Entry<String, Double> entry : fluxes.entrySet()) {
					if (entry.getValue() != 0.0 && !container1.getReactions().containsKey(entry.getKey()))
						listReactions.get(r).add(entry.getKey());
				}
			}
		}
	}

	public void getModelStatistics() {
		System.out.println("N� rea��es" + container1.getReactions().size());
		System.out.println("N� metabolites" + container1.getMetabolites().size());
		Set<String> metas = new HashSet<String>();

		if (container1.getModelName().equals("Recon2") || container1.getModelName().equals("Recon1")) {
			for (Map.Entry<String, MetaboliteCI> entry : container1.getMetabolites().entrySet()) {
				metas.add(entry.getKey().substring(0, entry.getKey().length() - 2));
			}
		} else {
			for (Map.Entry<String, MetaboliteCI> entry : container1.getMetabolites().entrySet()) {
				metas.add(entry.getKey().substring(0, entry.getKey().length() - 1));
			}
		}
		System.out.println("N� unique metabolites" + metas.size());
		System.out.println("N� genes" + container1.getGenes().size());
		System.out.println("N� drains" + container1.identifyDrains().size());
		System.out.println("N� dead ends" + container1.identifyDeadEnds(true).size());
	}

	public static void getFVAStatistics(ISteadyStateModel model, EnvironmentalConditions env) throws Exception {

		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, env, null, SolverType.CPLEX);
		System.out.println("N� de fluxos zero" + fva.identifyFVAZeroFluxes().getZeroValueFluxes().size());

	}

	// return the map of metabolites. the comparison take in account the
	// compartment
	// comp Type 1 - Recon1, Recon2
	// 2 - Recon1 HMR
	// 3 - Recon2 HMR
	public Map<String, String> intersectMetabolites() {
		Map<String, String> res = new HashMap<String, String>();
		for (String compId : container1.getCompartments().keySet()) {
			String compId2 = translateCompartments(compId);
			for (String metaId : container1.getCompartment(compId).getMetabolitesInCompartmentID()) {
				for (String metaId2 : container2.getCompartment(compId2).getMetabolitesInCompartmentID()) {
					if (isTheSameMeta(metaId, metaId2)) {
						res.put(metaId, metaId2);
					}
				}
			}
		}
		return res;

	}

	public Map<String, String> intersectReactions() {
		Map<String, String> ret = new HashMap<String, String>();

		for (ReactionCI react : container1.getReactions().values()) {
			if (!react.isDrain()) {
				for (ReactionCI react2 : container2.getReactions().values()) {
					if (hasSameCompounds(react, react2)) {
						ret.put(react.getId(), react2.getId());
						// System.out.println(react.getId() + "," +
						// react2.getId());
					}
				}
			}
		}
		return ret;
	}

	public Map<String, String> mergeDrains() {
		Map<String, String> res = new HashMap<String, String>();
		Map<String, String> aux1 = new HashMap<String, String>(); // nameMeta
																	// -->
																	// idReac
		Map<String, String> aux2 = new HashMap<String, String>();// nameMeta -->
																	// idReac
		for (String r : container1.identifyDrains()) {
			for (String m : container1.getReaction(r).getMetaboliteSetIds()) {
				String name = container1.getMetabolite(m).getName().toLowerCase();
				aux1.put(name, r);
			}
		}
		for (String r : container2.identifyDrains()) {
			for (String m : container2.getReaction(r).getMetaboliteSetIds()) {
				String name = container2.getMetabolite(m).getName().toLowerCase();
				aux2.put(name, r);
			}
		}
		for (Map.Entry<String, String> entry : aux1.entrySet()) {
			if (aux2.containsKey(entry.getKey())) {
				res.put(entry.getValue(), aux2.get(entry.getKey()));
			}
		}
		return res;
	}

	public static Map<String, double[]> getFVA(Set<String> reactions, ISteadyStateModel model, String resFile,
			EnvironmentalConditions env) throws Exception {
		Map<String, double[]> result = new HashMap<String, double[]>();

		FBAFluxVariabilityAnalysis fva = new FBAFluxVariabilityAnalysis(model, env, null, SolverType.CPLEX);

		// FBAFluxVariabilityAnalysis fva = new
		// FBAFluxVariabilityAnalysis(model, null, null, SolverType.CPLEX);
		FileWriter f = new FileWriter(resFile);
		BufferedWriter b = new BufferedWriter(f);

		for (String reac : reactions) {
			double[] limits = fva.limitsFlux(reac, 0.1);
			b.write(reac + ">" + limits[0] + ">" + limits[1]);
			b.newLine();
			result.put(reac, limits);
		}
		b.close();
		f.close();
		return result;
	}

	// ////////////////////////////////////
	// PRINTs
	// ////////////////////////////////////

	public void printDrains(Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());

		}
	}

	public void printDrains(Container c) {
		for (String r : c.identifyDrains()) {
			String line = r + "\t";
			for (String m : c.getReaction(r).getMetaboliteSetIds()) {
				line = line + m + "\t" + c.getMetabolite(m).getName();
			}
			System.out.println(line);
		}

	}

	// ////////////////////////////////////
	// PRIVATE METHODS
	// ////////////////////////////////////

	// the method don't validate if the stoichiometry is the same, it looks only
	// for the used metabolites.
	private boolean hasSameCompounds(ReactionCI reac1, ReactionCI reac2) {
		boolean res = false;

		if (equalSetMetas(reac1, reac2, true, true) && equalSetMetas(reac1, reac2, true, false))
			res = true;
		else if (equalSetMetas(reac1, reac2, false, true) && equalSetMetas(reac1, reac2, false, false))
			res = true;
		return res;
	}

	// test if two set of metabolites are equals. Don't test strange sets like :
	// m1, m1, m2 VS m1, m2, m3 . In this case the result is equal
	// is SameDir compare Reag1 / Reag2 or Prod1/Prod2
	// isProducts the set of Reaction1 to compare is Product
	private boolean equalSetMetas(ReactionCI r1, ReactionCI r2, boolean isSameDir, boolean isProducts) {
		Map<String, StoichiometryValueCI> set1, set2;
		boolean equal = true;
		if (isProducts)
			set1 = r1.getProducts();
		else
			set1 = r1.getReactants();
		if ((isSameDir && isProducts) || (!isProducts && !isSameDir))
			set2 = r2.getProducts();
		else
			set2 = r2.getReactants();
		if (set1.size() != set2.size())
			return !equal;

		for (String prod1 : set1.keySet()) {
			boolean prodEq = false;
			for (String prod2 : set2.keySet()) {
				if (isTheSameMeta(prod1, prod2)
						&& translateCompartments(set1.get(prod1).getCompartmentId()).equals(
								set2.get(prod2).getCompartmentId())) {
					prodEq = true;
					break;
				}
				if (!prodEq)
					return !equal;
			}
		}
		return equal;
	}

	// compare name, formula, KeggId and Chebi is one of this properties are
	// equal.. the metabolite are the same
	private boolean isTheSameMeta(String m1, String m2) {
		MetaboliteCI meta1 = container1.getMetabolite(m1);
		MetaboliteCI meta2 = container2.getMetabolite(m2);
		boolean res = false;
		if (meta1.getName().equalsIgnoreCase(meta2.getName()))
			res = true;
		else if (meta1.getFormula() != null && meta2.getFormula() != null
				&& meta1.getFormula().equals(meta2.getFormula()) && !meta1.getFormula().equals(""))
			res = true;
		else {
			Map<String, String> extra1 = container1.getMetabolitesExtraInfo().get(m1);
			Map<String, String> extra2 = container2.getMetabolitesExtraInfo().get(m2);
			if (extra1 != null && extra2 != null) {
				if (extra1.containsKey(Config.FIELD_KEGG_ID) && extra2.containsKey(Config.FIELD_KEGG_ID)
						&& extra1.get(Config.FIELD_KEGG_ID).equals(extra2.get(Config.FIELD_KEGG_ID))) {
					res = true;
				}
				if (extra1.containsKey(Config.FIELD_CHEBI_ID) && extra2.containsKey(Config.FIELD_CHEBI_ID)
						&& extra1.get(Config.FIELD_CHEBI_ID).equals(extra2.get(Config.FIELD_CHEBI_ID))) {
					res = true;
				}
			}
		}
		return res;
	}

	private String translateCompartments(String comp) {
		String newComp = "";
		comp = comp.replace("C_", "");
		if (comparisonType == 1) {
			newComp = comp;
		} else {
			if (comp.equals("e"))
				newComp = "C_s";
			else if (comp.equals("x"))
				newComp = "C_p";
			else
				newComp = "C_" + comp;
		}
		return newComp;
	}

}
