package pt.uminho.ceb.biosystems.mew.omicsintegration.enums;

public enum GeneExpPlatform {
	AFFY ("Affymetrix", "/Rscripts/Affy.R"),
	INIT ("Affy using INIT cal", "/Rscripts/GSE7307_INIT_approach.R");
	
	private String script;
	private String name;
	
	GeneExpPlatform(String name, String script) {
        this.name = name;
		this.script = script;        
    }
	public String getName(){return name;}
	
    public String getScript() { return script;}
    
}
