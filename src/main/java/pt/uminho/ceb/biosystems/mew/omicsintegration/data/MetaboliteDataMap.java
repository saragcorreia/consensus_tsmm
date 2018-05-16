package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public class MetaboliteDataMap extends AbstractOmicsDataMap {

	public MetaboliteDataMap(Map<String, Double> values, Container model, Condition condition) {
		super(values, model, condition);
	}

	public MetaboliteDataMap() {
		super();
	}

	public OmicsDataType getOmicType() {
		return OmicsDataType.METABOLITE;
	}
	public IOmicsDataMap cloneWithoutMapValues(){
		MetaboliteDataMap newObj = new MetaboliteDataMap(new HashMap<String, Double>(), this.container, this.condition);
		return newObj;
	}
}
