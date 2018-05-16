package pt.uminho.ceb.biosystems.mew.omicsintegration.enums;

public enum DataSource {
	HPA ("Human Protein Atlas"),
	HMDB ("Human Metabolome Database"),
//	GSE ("Gene Expression Series"),
//	BARCODE ("Gene Expression Barcode"),
	CSV ("CSV File");
	
	private String name;
	DataSource(String name) {
        this.name = name;
    }
	
    public String getName() { return name; }
	
}
