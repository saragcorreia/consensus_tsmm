package pt.uminho.ceb.biosystems.mew.omicsintegration.transformation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;

public class TransformOmicsKeys implements ITransformOmics {

	private Map<String, Set<String>> additionalConversion;

	public TransformOmicsKeys(Map<String, Set<String>> convertionKeys) {
		this.additionalConversion = convertionKeys;
	}

	@Override
	public IOmicsContainer transform(IOmicsContainer omics) {

		Set<String> ids = new HashSet<String>(omics.getValues().keySet());

		for (String id : ids) {
			Set<String> newkeys = additionalConversion.get(id);
			if (newkeys != null) {
				for (String nk : newkeys) {
					omics.setValue(nk, omics.getValue(id));
				}
			}
			omics.getValues().remove(id);// remove the original omic id

			for (String propertie : omics.getExtraInfo().keySet()) {
				if (omics.getExtraInfo().get(propertie).containsKey(id)) {
					String value = omics.getExtraInfo().get(propertie).get(id);
					for (String nk : newkeys) {
						omics.getExtraInfo().get(propertie).put(nk, value);
					}
					// remove the original omic id
					omics.getExtraInfo().get(propertie).remove(id);
				}
			}
		}
		return omics;
	}
}
