package pt.uminho.ceb.biosystems.mew.omicsintegration.enums;

public enum Operation {
	LOG2 ("Log base 2", "log2"),
	SCALE ("Normalize Data", "scale");
	
	private String name;
	private String function;
	
	Operation(String name, String function) {
        this.name = name;
		this.function = function;
    }
	public String getName(){return name;}
	
    public String getFunction() { return function;}

}
