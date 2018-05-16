package pt.uminho.ceb.biosystems.mew.omicsintegration.io;

import java.io.FileNotFoundException;
import java.io.IOException;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.FileFormatException;


public interface IOmicsReader {
	
	public IOmicsContainer load() throws  FileNotFoundException, IOException, FileFormatException;
	
}
