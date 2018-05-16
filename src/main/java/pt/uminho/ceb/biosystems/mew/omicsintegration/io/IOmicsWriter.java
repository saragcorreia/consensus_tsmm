package pt.uminho.ceb.biosystems.mew.omicsintegration.io;

import java.io.IOException;

public interface IOmicsWriter {
	public void saveOmicsData(String file) throws IOException;

}
