package pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.methods;

import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.core.model.converters.ContainerConverter;
import pt.uminho.ceb.biosystems.mew.core.model.exceptions.InvalidSteadyStateModelException;
import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.omicsreconstruction.SpecificModelResult;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public abstract class AbstractReconstructionAlgorithm implements ISpecificModelReconstruction<SpecificModelResult>{
	protected ISteadyStateModel model;

	protected IOmicsConfiguration configuration;

	public AbstractReconstructionAlgorithm(IOmicsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public void buidSteadyStateModel() throws InvalidSteadyStateModelException {
		model = ContainerConverter.convert(configuration.getTemplateContainer());
	}
	
	public void buidSteadyStateModel(Container container) throws InvalidSteadyStateModelException {
		model = ContainerConverter.convert(container);
	}

	// Abstract methods
	public abstract SpecificModelResult generateSpecificModel() throws Exception;
	
	public String getObjectiveFunctionToString(){
		return configuration.getMethod().getName();
	}

	protected SpecificModelResult buildTissueSpecificModel(
			Set<String> reactions) {
		Container specificModel = configuration.getTemplateContainer().clone();
		specificModel.removeReactions(CollectionUtils.getSetDiferenceValues(
				configuration.getTemplateContainer().getReactions().keySet(), reactions));

		return new SpecificModelResult(specificModel, configuration);
	}

	// gets and sets

	public IOmicsConfiguration getConfiguration() {
		return configuration;
	}
	
}
