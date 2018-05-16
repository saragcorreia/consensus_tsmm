package pt.uminho.ceb.biosystems.mew.omicsintegration.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.MetaboliteDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class Meta2MetaIntegrator extends AbstractOmicIntegrator<IOmicsContainer, MetaboliteDataMap> {

	// build an auxiliary structure to avoid the multiple scan of extraMetaInfo
	// in Container during the conversion
	// Ex: if the integration is by omicIdFied = KEGGID on Palsson model, this
	// object will contain <KEGGID, IDModelPalsson>
	private Map<String, Set<String>> extraInfo;

	public Meta2MetaIntegrator(Container container) {
		super(container, Config.FIELD_ID, Config.FIELD_ID);
	}

	public Meta2MetaIntegrator(Container model, String modelIdField, String omicIdField) {
		super(model, modelIdField, omicIdField);
		if (!(omicIdField.equals(Config.FIELD_ID) || omicIdField.equals(Config.FIELD_NAME)))
			buildExtraInfo();

	}

	public MetaboliteDataMap convert(IOmicsContainer omicData) {

		return new MetaboliteDataMap(getConvertedMapValues(omicData), model, omicData.getCondition());
	}

	public Set<String> convertIdToModelFormat(String idOmic) {
		Set<String> newIds = new HashSet<String>();
	

		if (modelIdField.equals(Config.FIELD_ID)) {
			
				if (model.getMetabolite(idOmic) != null)
					newIds.add(idOmic);
			
		} else if (modelIdField.equals(Config.FIELD_NAME)) {
			Map<String, MetaboliteCI> metabolites = model.getMetabolites();
			for (MetaboliteCI m : metabolites.values()) {
				if (idOmic.equals(m.getName()))
					newIds.add(m.getId());
			}
		}
		// from extra info fields
		else {

				if (extraInfo.containsKey(idOmic))
					newIds.addAll(extraInfo.get(idOmic));
			
		}
		return newIds;
	}

	// Convert information assuming that integration will be by KeggId:
	
	//	KEGID ->[M_asp_c->C00049 , M_asp_e ->C00049 ,M_water_c ->[CHEBI->00000]
	// to
	// C00049-> [M_asp_c, M_asp_e]
	private void buildExtraInfo() {
		extraInfo = new HashMap<String, Set<String>>();
		 Map<String, String> extraFieldInfo = model.getMetabolitesExtraInfo().get(omicIdField);
		
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
