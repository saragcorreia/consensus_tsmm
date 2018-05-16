package pt.uminho.ceb.biosystems.mew.omicsintegration.integration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.GeneDataMap;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class Gene2GeneIntegrator extends AbstractOmicIntegrator<IOmicsContainer, GeneDataMap> {

	public Gene2GeneIntegrator(Container model) {
		super(model, Config.FIELD_ID, Config.FIELD_ID);
	}

	public Gene2GeneIntegrator(Container model, String modelIdField, String omicIdField) {
		super(model, modelIdField, omicIdField);
	}


	public GeneDataMap convert(IOmicsContainer omicData) {
		return new GeneDataMap(getConvertedMapValues(omicData), model, omicData.getCondition());

	}

	// convert the id in Omic format to Model format must return a list. 
	// Integration by metabolite name for instance must return a list of metabolites (different compartments)
	public Set<String> convertIdToModelFormat(String idOmic) {
		Set<String> newIds = new HashSet<String>();

		if (modelIdField.equals(Config.FIELD_ID)) {
			if (model.getGene(idOmic) != null)
				newIds.add(idOmic);
			
		} else if (modelIdField.equals(Config.FIELD_NAME)) {
			Map<String, GeneCI> genes = model.getGenes();
			for (GeneCI g : genes.values()) {
				if (idOmic.equals(g.getGeneName()))
					newIds.add(g.getGeneId());
			
			}
		}
		return newIds;
	}
}
