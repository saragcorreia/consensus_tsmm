package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction;

public enum OmicsMethods {
	tINIT("tINIT"), MBA("MBA"), mCADRE("mCADRE"), FASTCORE("FASTCORE"), IMAT("IMAT"), GIMME("GIMME"), EFLUX("E-Flux");

	private String name;

	private OmicsMethods(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
