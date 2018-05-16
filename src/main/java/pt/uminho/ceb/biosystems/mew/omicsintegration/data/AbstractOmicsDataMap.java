package pt.uminho.ceb.biosystems.mew.omicsintegration.data;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public abstract class AbstractOmicsDataMap implements IOmicsDataMap, Serializable {
	private static final long serialVersionUID = 1L;
	protected Map<String, Double> values;
	protected Container container;
	protected Condition condition;
	
	public AbstractOmicsDataMap(Map<String, Double> values, Container model, Condition cond) {
		this.values = values;
		this.container = model;
		this.condition = cond;
	}

	public AbstractOmicsDataMap() {
		this.values = new HashMap<String, Double>();
		this.container = null;
		this.condition = null;
	}
	
	public abstract OmicsDataType getOmicType();
	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition cond) {
		this.condition = cond;
	}

	public void removeValuesByKey(Set<String> keys) {
		for (String k : keys) {
			values.remove(k);
		}
	}

	public Double getValue(String id) {
		return values.get(id);
	}

	public void setValues(Map<String, Double> values) {
		this.values = values;
	}

	public void setValues(Set<String> values) {
		this.values = new HashMap<String, Double>();
		for (String v : values) {
			this.values.put(v, 1.0);
		}
	}

	public void setValue(String id, Double value) {
		this.values.put(id, value);

	}

	public Container getModelContainer() {
		return container;
	}

	public Map<String, Double> getMapValues() {
		return values;
	}
	
	public void setMapValues(Map <String, Double> values){
		this.values = values;		
	}

	public void setModelContainer(Container model) {
		this.container = model;
	}


}
