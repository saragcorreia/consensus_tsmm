package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class TransformOmicsFilter implements ITransformOmics {
	private Map<String, String> filters;
	private Pattern p;
	private Matcher m;

	public TransformOmicsFilter(Map<String, String> filters) {
		this.filters = filters;
	}

	@Override
	public IOmicsContainer transform(IOmicsContainer omics) {
		IOmicsContainer newOmics = omics.clone();

		for (String field : filters.keySet()) {
			p = Pattern.compile(filters.get(field));
			if (field.equals(Config.FIELD_ID)) {
				filterById(newOmics);
			} else if (field.equals(Config.FIELD_VALUE)) {
				filterByValue(newOmics);
			} else {
				filterByExtraInfo(newOmics, field);
			}
		}
		return newOmics;
	}

	private void filterById(IOmicsContainer omics) {
		Set<String> ids = omics.getValues().keySet();
		Set<String> toRemove = new HashSet<String>();
		for (String id : ids) {
			m = p.matcher(id);
			if (!m.matches()) {
				toRemove.add(id);
			}
		}
		for(String key: toRemove)
		omics.removeById(key);
	}

	private void filterByValue(IOmicsContainer omics) {
		Set<String> ids = omics.getValues().keySet();
		Set<String> toRemove = new HashSet<String>();
		for (String id : ids) {
			Double value = omics.getValue(id);
			m = p.matcher(value.toString());
			if (!m.matches()) {
				toRemove.add(id);
			}
		}
		for(String key: toRemove)
		omics.removeById(key);
	}

	private void filterByExtraInfo(IOmicsContainer omics, String field) {
		Map<String, String> map = omics.getExtraInfo().get(field);
		Set<String> idsToRemove = new HashSet<String>();
		
		for (String id : map.keySet()) {
			m = p.matcher(map.get(id));
			if (!m.matches())
				idsToRemove.add(id);
		}
		
		for(String id : idsToRemove){
			omics.getValues().remove(id);
			for(String propertie: omics.getExtraInfo().keySet()){
				omics.getExtraInfo().get(propertie).remove(id);
			}
		}
		
	}

}
