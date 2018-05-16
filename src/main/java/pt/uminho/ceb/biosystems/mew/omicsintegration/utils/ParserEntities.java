package pt.uminho.ceb.biosystems.mew.omicsintegration.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.EquationFormatException;

public class ParserEntities {

	private static Pattern patttern = Pattern.compile("(.*)?\\[(.*)\\]");
	private static String name = "[\\w\\d\\.\\:\\(\\)\\-\\,]+";
	private static String stoic = "\\(?(\\d+(?:\\.\\d+)?(?:[eE][\\+\\-]?\\d+)?)?\\s*\\)?";
	private static String compartment = "\\[([\\w\\d\\.\\:]+)\\]";
	private static Pattern patternEquation = Pattern.compile("^\\s*" + "([\\d\\w\\+\\.\\*\\(\\)\\[\\]\\:\\s\\-\\,]+?)"
			+ "(<)?((?:--?|==?)>)" + "([\\d\\w\\+\\.\\*\\(\\)\\[\\]\\:\\s\\-\\,]*)\\s*$");
	private static Pattern patternCompound = Pattern.compile("^\\s*(" + name + ")\\s*(?:" + compartment + ")?\\s*$");
	private static Pattern patternCompoundEq = Pattern.compile("^\\s*(?:" + stoic + "\\s+)?\\s*(?:\\*?\\s+)?(" + name
			+ ")\\s*(?:" + compartment + ")?\\s*$");

	private static String splitByPlus = "(\\s*\\+\\s*)+";

	// return 0- metabolite 1 - compartment
	public static String[] parseMetawithCompartment(String text) {
		String[] result = new String[2];
		Matcher matcher = patternCompound.matcher(text);
		if (matcher.find() && matcher.group(1) != null) {
			result[0] = matcher.group(1);
			if (matcher.group(2) != null)
				result[1] = matcher.group(2);
		}
		return result;
	}

	// returns 0-coef; 1-name; 2-compartment
	public static String[] parseCompoundInEqn(String text) throws EquationFormatException {
		String[] res = new String[3];
		Matcher matcher = patternCompoundEq.matcher(text);
		if (matcher.find() && matcher.group(2) != null) {
			res[0] = (matcher.group(1) != null && !matcher.group(1).equals("")) ? matcher.group(1) : "1.0";
			// if the compound id make match in the model use
			res[1] = matcher.group(2);
			if (matcher.group(3) != null)
				res[2] = matcher.group(3);
		} else {
			throw new EquationFormatException("FORMAT ERROR: Expected (stoichiometry) metabolite_id + ... -> metabolite_id + ...\n");
		}
		return res;
	}

	// returns 0 - R ou I 1- Reactantes 2- Products
	public static String[] getElementsInEquation(String text) throws EquationFormatException {
		String[] result = new String[3];
		Matcher matcher = patternEquation.matcher(text);

		if (matcher.find()) {
			result[0] = matcher.group(1);
			result[1] = (matcher.group(4) == null) ? "" : matcher.group(4);
			result[2] = (matcher.group(2) != null) ? "R" : "I";
		} else {
			// System.out.println("\n\n\nequation " + equation);
			throw new EquationFormatException(
					"FORMAT ERROR: Equation isn't correctly defined (use -->, =>, <==>, <=>, -> or <->) Expected met1 + (stoich) met2 + ... --> met3 \n");
		}

		return result;
	}

	// return an array with the compounds present in a side of a reaction
	public static String[] splitReagentsOrProducts(String text) {
		return text.split(splitByPlus);
	}

	public static String joinMetaAndCompartment(String metabolite, String compartment) {
		return metabolite + "[" + compartment + "]";
	}
}
