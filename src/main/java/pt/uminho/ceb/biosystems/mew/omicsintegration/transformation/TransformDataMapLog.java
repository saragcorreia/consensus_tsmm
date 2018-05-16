package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TransformMissingPropertiesException;

public class TransformDataMapLog  implements ITransformDataMap{

	private double constant;
	public static String VAR_BASE = "BASE";
	
	public static Map<String, String> parameters = new HashMap<String, String>();
	static {
		parameters.put(VAR_BASE, "Log Base");
	};

	public  TransformDataMapLog(Map<String, Object> properties) throws TransformMissingPropertiesException{
		try{
		this.constant = Math.log((Double)properties.get(VAR_BASE));
	}catch(Exception e){
		e.printStackTrace();
		throw new TransformMissingPropertiesException("Propertie not found for transformation!");
	}
	}

	
	
	@Override
	public IOmicsDataMap transform(IOmicsDataMap omics) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// create a new object
		IOmicsDataMap newOmics = omics.cloneWithoutMapValues();

		
		for(Map.Entry<String, Double> entry : omics.getMapValues().entrySet()){
			double newVal = logb(entry.getValue());
			newOmics.setValue(entry.getKey(), newVal);
		}
		return newOmics;
	}
	
	
//	One can easily calculate the logarithm of any base using the following simple equation:
//	log_b x = log_k(x) / log_k(b)
//	Where log_k is the function that returns the base-k logarithm of a number, and it can be any real number.
		
	private  double logb( double value){
		return Math.log(value) /constant;
	}

	
	
}
