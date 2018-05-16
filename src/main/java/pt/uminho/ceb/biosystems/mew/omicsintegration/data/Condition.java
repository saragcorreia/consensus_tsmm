package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.HashMap;
import java.util.Map;

/** Contains properties and conditions of the experimental data-set */
public class Condition {
	private Map<String,String> properties;
	
	public Condition(){
		properties = new HashMap<String,String>();		
	}
	
	public void setProperty(String id, String value){
		properties.put(id, value);
	}
	
	public String getPropretieValue(String id){
		return properties.get(id);
	}
	
	public boolean hasProperty(String property){
		return (properties.containsKey(property) && properties.get(property)!=null);
	}	
	public Map<String, String> getProperties(){
		return properties;
	}
}
