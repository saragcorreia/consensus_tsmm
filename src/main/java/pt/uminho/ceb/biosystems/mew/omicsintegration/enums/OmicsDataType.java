package pt.uminho.ceb.biosystems.mew.omicsintegration.enums;

public enum OmicsDataType {

	GENE("GENOMICS"), PROTEIN("PROTEOMICS"), METABOLITE("METABOLOMICS"), REACTION("REACTION SCORE");

	private String name;

	private OmicsDataType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
