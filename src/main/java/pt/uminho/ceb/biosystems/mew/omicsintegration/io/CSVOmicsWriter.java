package pt.uminho.ceb.biosystems.mew.omicsintegration.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class CSVOmicsWriter implements IOmicsWriter{
	private IOmicsContainer omics;
	
	public CSVOmicsWriter(IOmicsContainer omics){
		this.omics=omics;
	}

	@Override
	public void saveOmicsData(String fileName) throws IOException {
			try (BufferedWriter b = new BufferedWriter(new FileWriter(fileName))) {
				for (Map.Entry<String, Double> e : omics.getValues().entrySet()) {
					b.write(e.getKey() + Config.USER_DELIMITER + e.getValue());
					b.write(Config.USER_DELIMITER);
					for(String propertie : omics.getExtraInfo().keySet()){
					
					if (omics.getExtraInfo().get(propertie).containsKey(e.getKey())) {
							b.write(propertie + ": " + omics.getExtraInfo().get(propertie).get(e.getKey())+ Config.USER_DELIMITER_INSIDE);
						}
					}
					b.newLine();
				}
			}
	}
		
}
	

