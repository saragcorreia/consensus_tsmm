package pt.uminho.ceb.biosystems.mew.omicsintegration.enums;

public enum TaskFileStructure {
	COM("COM", 0), 
	ID("ID", 1), 
	DES("DESCRIPTION", 2), 
	FAIL("SHOULD_FAIL", 3), 
	IN("IN", 4), 
	IN_LB("IN_LB", 5), 
	IN_UB("IN_UB", 6), 
	OUT("OUT", 7), 
	OUT_LB("OUT_LB", 8), 
	OUT_UB("OUT_UB", 9), 
	EQU("EQU", 10), 
	EQU_LB("EQU_LB", 11), 
	EQU_UB("EQU_UB", 12),
	OBJ_REAC("OBJ_REAC", 13);

	private int index;
	private String name;

	TaskFileStructure(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

}
