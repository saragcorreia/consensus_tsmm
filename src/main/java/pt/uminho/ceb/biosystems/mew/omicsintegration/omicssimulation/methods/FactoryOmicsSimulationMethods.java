package pt.uminho.ceb.biosystems.mew.omicsintegration.omicssimulation.methods;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.core.model.steadystatemodel.ISteadyStateModel;
import pt.uminho.ceb.biosystems.mew.core.simulation.formulations.abstractions.AbstractSSBasicSimulation;
import pt.uminho.ceb.biosystems.mew.omicsintegration.configuration.IOmicsConfiguration;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TransformException;
import pt.uminho.ceb.biosystems.mew.solvers.lp.LPProblem;

public class FactoryOmicsSimulationMethods {

	static Map<String, Class<? extends AbstractSSBasicSimulation<? extends LPProblem>>> map = new HashMap<>();

	static void putMethod(String id, Class<? extends AbstractSSBasicSimulation<? extends LPProblem>> klass)
			throws NoSuchMethodException {
		try {
			klass.getConstructor(ISteadyStateModel.class, IOmicsConfiguration.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new NoSuchMethodException(
					"Class " + klass.getName() + " don't have contructor with correct parameters!");
		}
		map.put(id, klass);
	}

	static {
		try {
			putMethod("IMAT", IMAT.class);
			putMethod("GIMME", GIMME.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static AbstractSSBasicSimulation<? extends LPProblem> getSimulationMethod(String type,
			ISteadyStateModel model, IOmicsConfiguration configuration)
					throws InstantiationException, IllegalAccessException, IllegalArgumentException,
					InvocationTargetException, NoSuchMethodException, SecurityException {
		return map.get(type.toString()).getConstructor(ISteadyStateModel.class, IOmicsConfiguration.class)
				.newInstance(model, configuration);
	}

}