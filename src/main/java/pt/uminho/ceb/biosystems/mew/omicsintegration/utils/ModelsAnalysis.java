package pt.uminho.ceb.biosystems.mew.omicsintegration.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.othersProj.JSBMLReader;

public class ModelsAnalysis {

	// FileName --> set of metabolites
	protected Map<String, Set<String>> metabolites;
	// FileName --> set of reactions
	protected Map<String, Set<String>> reactions;
	// order used in the intersections
	protected List<String> tissueOrder;

	public ModelsAnalysis(Set<String> sbmlFiles, String path) throws Exception {
		metabolites = new HashMap<String, Set<String>>();
		reactions = new HashMap<String, Set<String>>();
		tissueOrder = new ArrayList<String>();
		for (String sbml : sbmlFiles) {
			String file = path + sbml;
			Container cont = new Container(new JSBMLReader(file, "Human", false, true));
			metabolites.put(sbml, cont.getMetabolites().keySet());
			reactions.put(sbml, cont.getReactions().keySet());
			System.out.println(sbml + " --> " + cont.getMetabolites().keySet().size() + " - "
					+ cont.getReactions().keySet().size() + " D:" + cont.getDrains().size());
			tissueOrder.add(sbml);
		}
	}

	// return the number of reactions present in all files
	public int getIntersection() {
		int res = 0;
		String tissue = tissueOrder.get(0);
		Set<String> interReactions = new HashSet<String>(reactions.get(tissue));
		for (int i = 1; i < tissueOrder.size(); i++) {
			String tissue2 = tissueOrder.get(i);
			interReactions.retainAll(reactions.get(tissue2));

		}
		res = interReactions.size();
		return res;
	}

	// return the pair intersections of reactions and metabolites of a list of
	// tissues-models... t1/t2 t1/t3 t2/t3
	public int[][] getIntersections() {
		int n = tissueOrder.size();
		int numCombinations = getFact(n) / (2 * getFact(n - 2));

		int[][] res = new int[2][numCombinations];
		int ind = 0;
		for (int i = 0; i < tissueOrder.size(); i++) {
			String tissue1 = tissueOrder.get(i);
			for (int j = i + 1; j < tissueOrder.size(); j++) {
				String tissue2 = tissueOrder.get(j);
				Set<String> interMetabolites = new HashSet<String>(metabolites.get(tissue1));
				interMetabolites.retainAll(metabolites.get(tissue2));
				res[0][ind] = interMetabolites.size();

				Set<String> interReactions = new HashSet<String>(reactions.get(tissue1));
				interReactions.retainAll(reactions.get(tissue2));
				res[1][ind] = interReactions.size();

				System.out.println(tissue1 + " --" + tissue2 + " M:" + interMetabolites.size() + " R:"
						+ interReactions.size());

				ind++;
			}
		}
		return res;
	}

	// get reactions that are present in a given number of Models
	public Set<String> getReactionsInIntersetion(int numberOfModels) {
		Set<String> res = new HashSet<String>();
		int value;
		Map<String, Integer> occurrences = new HashMap<String, Integer>();
		for (String model : reactions.keySet()) {
			for (String r : reactions.get(model)) {
				if (occurrences.containsKey(r))
					value = occurrences.get(r);
				else
					value = 1;
				occurrences.put(r, value);
			}
		}
		for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
			if (entry.getValue() == numberOfModels)
				res.add(entry.getKey());
		}
		return res;
	}

	// print the results of intersections
	public String printIntersections() {
		String res = "tissue /tissue: Metabolite - Reactions";
		int[][] intersections = getIntersections();
		int ind = 0;
		for (int i = 0; i < tissueOrder.size(); i++) {
			String tissue1 = tissueOrder.get(i);
			for (int j = i + 1; j < tissueOrder.size(); j++) {
				String tissue2 = tissueOrder.get(j);
				res += tissue1 + " / " + tissue2 + " : " + intersections[0][ind] + " - " + intersections[1][ind] + "\n";
				ind++;
			}
		}
		return res;
	}

	private static int getFact(int n) {
		int f = 1;
		for (int i = n; i >= 1; i--) {
			f *= i;
		}
		return f;
	}

	public static void main(String[] args) {
		String path = "/Users/Sara/Documents/Projects/PHD_P01_Reconst_Approaches/Results/CompareMethods/INIT/";
		Set<String> sbmlFiles = new HashSet<String>();
		sbmlFiles.add("A549.xml");
		sbmlFiles.add("HL60.xml");
		sbmlFiles.add("K562.xml");
		sbmlFiles.add("MCF7.xml");
		sbmlFiles.add("MOLT4.xml");
		sbmlFiles.add("PC3.xml");
		sbmlFiles.add("RPMI8226.xml");

		try {
			// ModelsAnalysis.validateMate();
			ModelsAnalysis ma = new ModelsAnalysis(sbmlFiles, path);
			System.out.println(ma.getIntersection());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
