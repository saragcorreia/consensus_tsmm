package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods;

import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;

public interface ISpecificModelReconstruction <T extends SpecificModelResult> {
	public T generateSpecificModel() throws Exception;
	public String getObjectiveFunctionToString();
}
