package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public class ReactionDataMap extends AbstractOmicsDataMap {

	public ReactionDataMap(Map<String, Double> values, Container model, Condition condition ) {
		super(values, model, condition);
	}

	public ReactionDataMap() {
		super();
	}

	public OmicsDataType getOmicType() {
		return OmicsDataType.REACTION;
	}

	public IOmicsDataMap cloneWithoutMapValues(){
		ReactionDataMap newObj = new ReactionDataMap(new HashMap<String, Double>(), this.container, this.condition);
		return newObj;
	}
}
