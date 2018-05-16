package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public class GeneDataMap extends AbstractOmicsDataMap {

	public GeneDataMap(Map<String, Double> values, Container model, Condition condition) {
		super(values, model, condition);
	}

	public GeneDataMap() {
		super();
	}

	public OmicsDataType getOmicType() {
		return OmicsDataType.GENE;
	}
	
	public IOmicsDataMap cloneWithoutMapValues(){
		GeneDataMap newObj = new GeneDataMap(new HashMap<String, Double>(), this.container, this.condition);
		return newObj;
	}
}
