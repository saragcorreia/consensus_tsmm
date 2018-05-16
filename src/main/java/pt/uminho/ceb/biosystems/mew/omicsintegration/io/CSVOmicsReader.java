package pt.uminho.ceb.biosystems.mew.omicsintegration.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.OmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.FileFormatException;

public class CSVOmicsReader implements IOmicsReader {

	public String USER_DELIMITER = ",";
	public String DELIMITER_INSIDE_FIELDS = ";";

	private Condition condition;
	private String fileName;
	private OmicsDataType type;

	private List<String> headers;
	private int idColumnIndex = 0;
	private int valuesColumnIndex = -1;
	/** Extra information for data integration methods */
	private Set<Integer> extraFields;

	
	/** Convert qualitative values to quantitative values */
	private Map<String, Double> discreteValues;
	/** Filter the lines that in columns<Integer> have this values<String> */


	private String valuesColumnHeader = null;
	private boolean hasHeader = false;
	
	public CSVOmicsReader(){	
	}
	
	public CSVOmicsReader(Condition condition, String fileName, OmicsDataType type, int idColumnIndex,
			int valuesColumnIndex) {
		this.condition = condition;
		this.fileName = fileName;
		this.type = type;
		this.idColumnIndex = idColumnIndex;
		this.valuesColumnIndex = valuesColumnIndex;
	}

	public CSVOmicsReader(Condition condition, String datasetFilePath, OmicsDataType type) {
		this(condition, datasetFilePath, type, 0, -1);
	}

	public CSVOmicsReader(Condition condition, String fileName, OmicsDataType type,
			int idColumnIndex) {
		this(condition, fileName, type, idColumnIndex, -1);
	}

	public CSVOmicsReader(Condition condition, String fileName, OmicsDataType type, int idColumnIndex,
			int valuesColumnIndex, Map<String, Double> discreteValues) {
		this(condition, fileName, type, idColumnIndex, valuesColumnIndex);
		this.discreteValues = discreteValues;
	}

	public CSVOmicsReader(Condition condition, String fileName, OmicsDataType type, int idColumnIndex,
			int valuesColumnIndex, Set<Integer> extraFields, Map<String, Double> discreteValues) {
		this(condition, fileName, type, idColumnIndex, valuesColumnIndex, discreteValues);
		this.extraFields = extraFields;
	}

	public IOmicsContainer load() throws FileNotFoundException, IOException, FileFormatException {

		OmicsContainer container = new OmicsContainer(condition, type);
		int numberColumns = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;

			// If File has Columns Headers load and count them
			if (hasHeader) {
				line = br.readLine();

				String[] columnsHeaders = line.split(USER_DELIMITER);
				this.headers = new ArrayList<String>();

				numberColumns = columnsHeaders.length;
				for (int i = 0; i < numberColumns; i++) {
					this.headers.add(columnsHeaders[i]);
				}
			}

			// Define Column Values by Header Name
			if (valuesColumnHeader != null) {
				int columnIndex = getHeaderIndex(valuesColumnHeader);
				if (columnIndex == -1) {
					throw new FileFormatException("No column found with header " + valuesColumnHeader);
				}
				this.valuesColumnIndex = columnIndex;
			}

			while ((line = br.readLine()) != null) {
				if (!line.equals("")) { // allow empty rows
					String[] fieldsQ = line.split(USER_DELIMITER);
					if (numberColumns == 0) {
						numberColumns = fieldsQ.length;
					} else if (numberColumns != fieldsQ.length) {
						throw new FileFormatException("Missing columns in line: " + line);
					}

					// remove "" and white spaces around the values
					String[] fields = new String[fieldsQ.length];
					for (int i = 0; i < fields.length; i++)
						fields[i] = removeQuotes(fieldsQ[i]);

					//if (isValidLine(fields)) {
						if (valuesColumnIndex == -1) {
							container.setValue(fields[idColumnIndex], 1.0);
						} else {
							if (discreteValues == null) {
//								System.out.println(line);
								if (!fields[valuesColumnIndex].equals("NA"))
									container.setValue(fields[idColumnIndex], new Double(fields[valuesColumnIndex]));

							} else {
								if(discreteValues.get(fields[valuesColumnIndex])!=null)
									container.setValue(fields[idColumnIndex],
										discreteValues.get(fields[valuesColumnIndex]));
								else
									throw new FileFormatException("Problem in the mapping for the discreate values! level:" + fields[valuesColumnIndex]);
							}
						}

						if (extraFields != null) {
							 Iterator<Integer> it =  extraFields.iterator();
							while(it.hasNext()){
								int index = it.next();
								container.setExtraInfoProperty(fields[idColumnIndex], headers.get(index), fields[index]);
							}
						}
					//}
				}
			}
		}
		return container;
	}

	/**
	 * Verify if the line of data satisfies the filters in filterValues
	 * <Index,Value Required>
	 */
//	private boolean isValidLine(String[] fields) {
//		boolean isValid = true;
//
//		if (filterValues == null) {
//			return isValid;
//		}
//
//		if (fields[idColumnIndex].trim().equals("")) {
//			return false;
//		}
//
//		for (Map.Entry<Integer, String> entity : filterValues.entrySet()) {
//			String[] values;
//
//			if (DELIMITER_INSIDE_FIELDS != null && fields.length > entity.getKey()) {
//				values = fields[entity.getKey()].split(DELIMITER_INSIDE_FIELDS);
//			} else {
//				values = new String[] { fields[entity.getKey()] };
//			}
//
//			boolean isEntityValid = false;
//			for (String value : values) {
//				if (value.equalsIgnoreCase(entity.getValue())) {
//					isEntityValid = true;
//					break;
//				}
//			}
//
//			isValid = isValid && isEntityValid;
//		}
//
//		return isValid;
//	}

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

	public Set<Integer> getExtraFields() {
		return extraFields;
	}

	public void setExtraFields(Set<Integer> extraFields) {
		this.extraFields = extraFields;
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

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public int getHeaderIndex(String header) {
		if (isHasHeader())
			for (int i = 0; i < headers.size(); i++)
				if (headers.get(i).contains(header))
					return i;

		return -1;
	}

	public void setValuesColumnIndex(String valuesColumnHeaderId) throws Exception {
		this.valuesColumnHeader = valuesColumnHeaderId;
	}

}
