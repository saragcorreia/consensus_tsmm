package pt.uminho.ceb.biosystems.mew.omicsintegration.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.ReactionDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class Reac2ReacIntegrator extends AbstractOmicIntegrator<IOmicsContainer, ReactionDataMap> {
	// build an auxiliary structure to avoid the multiple scan of extraMetaInfo
	// in Container during the conversion
	private Map<String, Set<String>> extraInfo;

	public Reac2ReacIntegrator(Container model) {
		super(model, Config.FIELD_ID, Config.FIELD_ID);
	}

	public Reac2ReacIntegrator(Container model, String modelIdField, String omicIdField) {
		super(model, modelIdField, omicIdField);
		if (!(modelIdField.equals(Config.FIELD_ID) || modelIdField.equals(Config.FIELD_NAME)))
			buildExtraInfo();
	}

	public ReactionDataMap convert(IOmicsContainer omicData) {
		return new ReactionDataMap(getConvertedMapValues(omicData), model, omicData.getCondition());
	}

	public Set<String> convertIdToModelFormat(String idOmic) {
		Set<String> newIds = new HashSet<String>();

		if (modelIdField.equals(Config.FIELD_ID)) {
			if (model.getReaction(idOmic) != null)
				newIds.add(idOmic);

		} else if (modelIdField.equals(Config.FIELD_NAME)) {
			Map<String, ReactionCI> reactions = model.getReactions();

			for (ReactionCI r : reactions.values()) {
				if (idOmic.equals(r.getName()))
					newIds.add(r.getId());
			}
		}

		// from extra info fields
		else {
			if (extraInfo.containsKey(idOmic))
				newIds.addAll(extraInfo.get(idOmic));

		}
		return newIds;
	}

	// build extra info structure group by omidIdField
	private void buildExtraInfo() {
		extraInfo = new HashMap<String, Set<String>>();
		 Map<String, String> extraFieldInfo = model.getReactionsExtraInfo().get(omicIdField);
		
		 for(Map.Entry<String, String> entry : extraFieldInfo.entrySet()){
		 if (extraInfo.containsKey(entry.getValue())) {
				extraInfo.get(entry.getValue()).add(entry.getKey());
			} else {
				HashSet<String> idsModel = new HashSet<String>();
				idsModel.add(entry.getKey());
				extraInfo.put(entry.getValue(), idsModel);
			}
		 }
	
	}
}
