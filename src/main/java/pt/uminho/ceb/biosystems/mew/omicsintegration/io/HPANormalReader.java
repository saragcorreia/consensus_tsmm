package pt.uminho.ceb.biosystems.mew.omicsintegration.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.OmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.FileFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class HPANormalReader implements IOmicsReader {

	public String USER_DELIMITER = ";";
	public String DELIMITER_INSIDE_FIELDS = ",";

	private Condition condition;
	private String fileName;
	private OmicsDataType type;

	/** Convert qualitative values to quantitative values */
	private Map<String, Double> discreteValues;
	/** Filter the lines that in columns<Integer> have this values<String> */
	private Map<Integer, String> filterValues;

//	private boolean isDiscretValues = false;
//	private double[] possibleValues;

	private int idColumnIndex = 0;
	private int valuesColumnIndex = 2;
	private int numPacientsColumnIndex = 3;

	private boolean hasHeader = false;

	public HPANormalReader(Condition condition, String fileName) {
		this.condition = condition;
		this.fileName = fileName;
		this.type = OmicsDataType.PROTEIN;

	}

	public HPANormalReader(Condition condition, String fileName, Map<String, Double> discreteValues) {
		this(condition, fileName);
		this.discreteValues = discreteValues;
//
//		// set info about discrete Values
//		if (discreteValues != null) {
//			isDiscretValues = true;
//			Set<Double> setValues = new HashSet<Double>();
//			for (Double d : discreteValues.values())
//				setValues.add(d);
//			possibleValues = new double[setValues.size()];
//			int i = 0;
//			for (double d : setValues) {
//				possibleValues[i] = d;
//				i++;
//			}
//		}

	}

	public HPANormalReader(Condition condition, String fileName, Map<String, Double> discreteValues,
			Map<Integer, String> filterValues) {
		this(condition, fileName, discreteValues);
		this.filterValues = filterValues;
	}

	public IOmicsContainer load() throws FileNotFoundException, IOException, FileFormatException {

		OmicsContainer container = new OmicsContainer(condition, type);
		Map<String, String[]> aux = new HashMap<String, String[]>();

		int numberColumns = 0;

		try (
			BufferedReader br = new BufferedReader(new FileReader(fileName))){
			String line;

			// If File has Columns Headers load and count them
			if (hasHeader) {
				line = br.readLine();
				String[] columnsHeaders = line.split(USER_DELIMITER);
				numberColumns = columnsHeaders.length;
			}
			String[] fieldsQ, fields;
			while ((line = br.readLine()) != null) {
				if (!line.equals("")) { // allow empty rows
					fieldsQ = line.split(USER_DELIMITER);
					if (numberColumns == 0) {
						numberColumns = fieldsQ.length;
					} else if (numberColumns != fieldsQ.length) {
						throw new FileFormatException("Missing columns in line: " + line);
					}

					// remove "" and white spaces around the values
					fields = new String[fieldsQ.length];
					for (int i = 0; i < fields.length; i++)
						fields[i] = removeQuotes(fieldsQ[i]);

					if (isValidLine(fields)) {
						String geneId = fields[idColumnIndex];
						// build the auxiliary structure to control that final
						// level of each gene has the maxim number of patients
						if (aux.containsKey(geneId)) {
							double oldVal = new Double(aux.get(geneId)[0]);
							double newVal = new Double(fields[numPacientsColumnIndex]);
							if (oldVal < newVal) {
								aux.get(geneId)[0] = fields[numPacientsColumnIndex];
								aux.get(geneId)[1] = discreteValues.get(fields[valuesColumnIndex]).toString();
							}
						} else {
							String[] duplo = new String[2];
							duplo[0] = fields[numPacientsColumnIndex];
							duplo[1] = discreteValues.get(fields[valuesColumnIndex]).toString();
							aux.put(geneId, duplo);
						}
					}
				}
			}
			Map<String, Double> mapValues = calcValues(aux);
			container.setValues(mapValues);
		}
		return container;

	}

	// remove the number of patients that have the max observed level
	private Map<String, Double> calcValues(Map<String, String[]> aux) {
		Map<String, Double> retValues = new HashMap<String, Double>();
		for (Map.Entry<String, String[]> e : aux.entrySet())
			retValues.put(e.getKey(), new Double(e.getValue()[1]));
		return retValues;
	}

	/**
	 * Verify if the line of data satisfies the filters in filterValues
	 * <Index,Value Required>
	 */
	private boolean isValidLine(String[] fields) {
		boolean isValid = true;

		if (filterValues == null) {
			return isValid;
		}

		if (fields[idColumnIndex].trim().equals("")) {
			return false;
		}

		for (Map.Entry<Integer, String> entity : filterValues.entrySet()) {
			String[] values;

			if (DELIMITER_INSIDE_FIELDS != null && fields.length > entity.getKey()) {
				values = fields[entity.getKey()].split(DELIMITER_INSIDE_FIELDS);
			} else {
				values = new String[] { fields[entity.getKey()] };
			}

			boolean isEntityValid = false;
			for (String value : values) {
				if (value.equalsIgnoreCase(entity.getValue())) {
					isEntityValid = true;
					break;
				}
			}

			isValid = isValid && isEntityValid;
		}

		return isValid;
	}

	private static String removeQuotes(String str) {
		str = str.replaceFirst("^\\s*[\"\']*\\s*", "");
		str = str.replaceFirst("\\s*[\"\']*\\s*$", "");
		return str;
	}

	// Getters and Setters
	public Map<String, Double> getDiscreteValues() {
		return discreteValues;
	}

	public void setDiscreteValues(Map<String, Double> discreteValues) {
		this.discreteValues = discreteValues;
	}

	public Map<Integer, String> getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(Map<Integer, String> filterValues) {
		this.filterValues = filterValues;
	}

	public int getIdColumnIndex() {
		return idColumnIndex;
	}

	public void setIdColumnIndex(int idColumnIndex) {
		this.idColumnIndex = idColumnIndex;
	}

	public int getValuesColumnIndex() {
		return valuesColumnIndex;
	}

	public void setValuesColumnIndex(int valuesColumnIndex) {
		this.valuesColumnIndex = valuesColumnIndex;
	}

	public boolean isHasHeader() {
		return hasHeader;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	public static void main(String[] args) {
		Condition condition = new Condition();
		condition.setProperty(Config.CONDITION_TISSUE, "brain");
		condition.setProperty(Config.CONDITION_STAGE, "cancer");

		// Read CSV with discreteValues
		HashMap<String, Double> discValues = new HashMap<String, Double>();
		discValues.put("Negative", -8.0);
		discValues.put("None", -8.0);
		discValues.put("Weak", 10.0);
		discValues.put("Low", 10.0);
		discValues.put("Moderate", 15.0);
		discValues.put("Medium", 15.0);
		discValues.put("Strong", 20.0);
		discValues.put("High", 20.0);

		// filter for the HPA lines

		HashMap<Integer, String> filter = new HashMap<Integer, String>();
		filter.put(1, "glioma");

		// Invoke reader for Protein measurements in HPA file
		HPANormalReader reader = new HPANormalReader(condition, "D:/OmicsData/HPA/hpa_cancer.csv", discValues, filter);

		reader.DELIMITER_INSIDE_FIELDS = null;
		reader.USER_DELIMITER = ",";

		try {
			IOmicsContainer dataContainer = reader.load();
			System.out.println("Data container:" + dataContainer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
