package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public interface IOmicsDataMap {

	public Map<String, Double> getMapValues();
	
	public void setMapValues(Map<String, Double> values);

	public void setValue(String id, Double value);

	public Double getValue(String id);

	public OmicsDataType getOmicType();
	
	public IOmicsDataMap cloneWithoutMapValues();

//	public Container getModelContainer();
//	public void setModelContainer(Container container);
//
	public Condition getCondition();
	public void setCondition(Condition condition);

}
