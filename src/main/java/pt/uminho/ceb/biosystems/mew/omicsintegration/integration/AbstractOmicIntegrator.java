package pt.uminho.ceb.biosystems.mew.omicsintegration.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.OmicDataConversionException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public abstract class AbstractOmicIntegrator<P extends IOmicsContainer, T extends IOmicsDataMap> implements IOmicIntegrator<P, T> {
	protected Container model;
	protected String modelIdField;
	protected String omicIdField;

	public AbstractOmicIntegrator(Container container, String modelIdField, String omicIdField) {
		this.model = container;
		this.modelIdField = modelIdField;
		this.omicIdField = omicIdField;
	}


	public Container getModelContainer() {
		return model;
	}


	protected Map<String, Double> getConvertedMapValues(P omicData) {
		HashMap<String, Double> values = new HashMap<String, Double>();
		// the ids used in integration is the keys of OmicData
		if (omicIdField.equals(Config.FIELD_ID)) {
			for (Map.Entry<String, Double> e : omicData.getValues().entrySet()) {
				Set<String> idModelList = convertIdToModelFormat(e.getKey());
				for (String idModel : idModelList) {
					// if 2 omic ids can be converted to the same id in model ,
					// the is considered the max value.
					// EX(not real): ENSG001 --- > ENST001 e ENSG002 --- >
					// ENST001 and ENSG001 has value 2 and ENSG002 has values 3
					// the result of convertion will be ENST001 -->3
					if (values.containsKey(idModel))
						values.put(idModel, Math.max(values.get(idModel), e.getValue()));
					else
						values.put(idModel, e.getValue());
				}
			}
		}
		// the ids used in integration are in external Info. Some could
		// haven't the extrainfo field.
		else {
			
			Map<String, String> idsToConvert = omicData.getExtraInfo().get(omicIdField);
			for(String omicId : idsToConvert.keySet()){
				// convert the value associated with the value of extraInfo ... ex :KEGG--> Gene1 --> ADB 
				// if omicFieldIntegration is KEGG, the return is  MAP(ADB)_in_model --> value of Gene1
					Set<String> idModelList = convertIdToModelFormat(idsToConvert.get(omicId));
					for (String idModel : idModelList) {
						if (values.containsKey(idModel))
							values.put(idModel, Math.max(values.get(idModel), omicData.getValue(omicId)));
						else
							values.put(idModel, omicData.getValue(omicId));
					}
			}
		}
		return values;
	}
	
	@Override
	public abstract T convert (P omics) throws OmicDataConversionException;

	public abstract Set<String> convertIdToModelFormat(String id);

}
