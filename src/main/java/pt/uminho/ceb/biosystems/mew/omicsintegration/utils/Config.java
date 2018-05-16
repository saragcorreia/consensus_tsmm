package pt.uminho.ceb.biosystems.mew.omicsintegration.utils;

public class Config {

//	public static String workspaceR = "D:/_workspaceR/";
	// public static String workspaceR =
	// "/home/scorreia/OmicsIntegrationTests/_workspaceR/";

//	public static Integer maxMemSizeR = 12288;

	// public static Integer maxMemSizeR = 0;

	public static Double LOWER_BOUND = -1000.0;
	public static Double UPPER_BOUND = 1000.0;

	public static String USER_DELIMITER = ";";
	public static String USER_DELIMITER_INSIDE = ",";

	// name of fields used in the omic and model extra info
	public static String FIELD_ID = "ID";
	public static String FIELD_VALUE = "VALUE";
	public static String FIELD_NAME = "NAME";
	public static String FIELD_KEGG_ID = "KEGG_ID";
	public static String FIELD_CHEBI_ID = "CHEBI_ID";
	public static String FIELD_FORMULA = "FORMULA";

	// fields for sample condition
	public static String CONDITION_TISSUE = "Tissue";
	public static String CONDITION_CELLTYPE = "Cell Type";
	public static String CONDITION_STAGE = "Stage";

}
