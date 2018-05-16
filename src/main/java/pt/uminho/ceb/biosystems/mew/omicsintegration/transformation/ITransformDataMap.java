package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.lang.reflect.InvocationTargetException;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;


public interface ITransformDataMap {

	public IOmicsDataMap transform(IOmicsDataMap omics) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException ;
}
