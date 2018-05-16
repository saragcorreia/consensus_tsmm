package pt.uminho.ceb.biosystems.mew.omicsintegration.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;

public class OmicsContainer implements IOmicsContainer {

	private Condition condition;
	private OmicsDataType type;
	private Map<String, Double> values;
	private Map<String, Map<String, String>> extraInfo; // KEGG -->GeneId
														// -->VALUE

	public OmicsContainer(Condition condition, OmicsDataType type) {
		this.condition = condition;
		this.type = type;
		this.values = new HashMap<String, Double>();
		this.extraInfo = new HashMap<String, Map<String, String>>();
	}

	public void setExtraInfoProperty(String id, String property, String value) {
		if (extraInfo.containsKey(property))
			extraInfo.get(property).put(id, value);
		else {
			Map<String, String> p = new HashMap<String, String>();
			p.put(id, value);
			extraInfo.put(property, p);
		}
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public void removeValues(Set<String> keys) {
		values.keySet().removeAll(keys);
	}

	@Override
	public Map<String, Map<String, String>> getExtraInfo() {
		return extraInfo;
	}

	@Override
	public void setExtraInfo(Map<String, Map<String, String>> extraInfo) {
		this.extraInfo = extraInfo;
	}

	@Override
	public Double getValue(String id) {
		return values.get(id);
	}

	@Override
	public void setValue(String id, Double value) {
		values.put(id, value);
	}

	@Override
	public Map<String, Double> getValues() {
		return values;
	}

	@Override
	public void setValues(Map<String, Double> values) {
		this.values = values;
	}

	@Override
	public OmicsDataType getOmicType() {
		return type;
	}

	@Override
	public IOmicsContainer clone() {
		OmicsContainer newOmics = new OmicsContainer(this.condition, this.getOmicType());
		newOmics.values = new HashMap<String, Double>(values);
		newOmics.extraInfo = new HashMap<String, Map<String, String>>();

		for (Map.Entry<String, Map<String, String>> entry : extraInfo.entrySet()) {
			newOmics.extraInfo.put(entry.getKey(), new HashMap<String, String>(entry.getValue()));
		}
		return newOmics;
	}

	@Override
	public void removeById(String key) {
		values.remove(key);
		for (String propertie : extraInfo.keySet()) {
			extraInfo.get(propertie).remove(key);
		}

	}

}
