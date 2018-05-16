package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public interface IOmicsContainer {

	public Map<String, Map<String, String>> getExtraInfo();
	
	public void setExtraInfo(Map<String, Map<String, String>> extraInfo);

	public Double getValue(String id);
	
	public void setValue(String id, Double value);

	public Map<String, Double> getValues();
	
	public void setValues(Map<String, Double> values);
	
	public OmicsDataType getOmicType();
	
	public IOmicsContainer clone();
	
	public void removeById(String key);
	
	// Validar se posso remover 
	public Condition getCondition();
}
