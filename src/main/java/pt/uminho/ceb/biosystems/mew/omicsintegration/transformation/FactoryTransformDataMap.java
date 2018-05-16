package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.TransformException;

public class FactoryTransformDataMap{
	
	static Map<String, Class<? extends ITransformDataMap>> map = new HashMap<>();
	
	static void putTransformData(String id, Class<? extends ITransformDataMap> klass) throws TransformException{
		try {
			klass.getConstructor(Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new TransformException("Class " +klass.getName()+" don't have contructor with correct parameters!");
		}
		map.put(id, klass);
	}
	
	static{
		try {
			putTransformData("GENE2REAC", TransformDataMapGeneToReac.class);
			putTransformData("LOG", TransformDataMapLog.class);
			putTransformData("RANGE", TransformDataMapValuesToRange.class);
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ITransformDataMap getTransformation(String type, Map<String, Object > properties ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{	
		return map.get(type.toString()).getConstructor(Map.class).newInstance(properties);
	}
	

}
