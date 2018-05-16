package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TransformMissingPropertiesException;

public class TransformDataMapValuesToRange implements ITransformDataMap {

	// SGC
	// independentemente se os valore são continuos ou discretos hé uma
	// conversão de valores para o intervalo dado pelo lb e ub.
	// nos dados discreatos a grandeza entre intervalos é mantida .. 0,1,2,10 ->
	// 0, 0.1,0.2, 1
	// Problema : quando os dados não têm todos os valores discretos possiveis
	// ex: 2,10 ---> 0,1

	private double lb, ub;
	// private boolean isDiscreate;
	public static String VAR_LB = "LB";
	public static String VAR_UB = "UB";

	public static Map<String, String> parameters = new HashMap<String, String>();

	static {
		parameters.put(VAR_LB, "Lower Bound");
		parameters.put(VAR_UB, "Upper Bound");
	};

	public TransformDataMapValuesToRange(Map<String, Object> properties) throws TransformMissingPropertiesException {
		try {
			this.lb = (Double) properties.get(VAR_LB);
			this.ub = (Double) properties.get(VAR_UB);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TransformMissingPropertiesException("Propertie not found for transformation!");
		}
	}

	@Override
	public IOmicsDataMap transform(IOmicsDataMap omics) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		double minVal = Double.MAX_VALUE, maxVal = Double.MIN_VALUE;

		// create a new object
		IOmicsDataMap newOmics = omics.cloneWithoutMapValues();

		for (String k : omics.getMapValues().keySet()) {
			double val = omics.getValue(k);
			if (val > maxVal)
				maxVal = val;
			if (val < minVal)
				minVal = val;
		}
		for (Map.Entry<String, Double> entry : omics.getMapValues().entrySet()) {
			double newVal = Double.NaN;
			if (!entry.getValue().isNaN()) {
				if (entry.getValue() == 0.0)
					newVal = 0.0;
				else if (entry.getValue() == minVal)
					newVal = lb;
				else
					newVal = ((entry.getValue() - minVal) * (ub - lb)) / (maxVal - minVal) + lb;
			}
			newOmics.setValue(entry.getKey(), newVal);
		}
		return newOmics;
	}

}
