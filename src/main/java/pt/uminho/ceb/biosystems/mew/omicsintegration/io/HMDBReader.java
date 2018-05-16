package pt.uminho.ceb.biosystems.mew.omicsintegration.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.omicsintegration.data.Condition;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.IOmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.data.OmicsContainer;
import pt.uminho.ceb.biosystems.mew.omicsintegration.enums.OmicsDataType;
import pt.uminho.ceb.biosystems.mew.omicsintegration.exceptions.FileFormatException;
import pt.uminho.ceb.biosystems.mew.omicsintegration.utils.Config;

public class HMDBReader implements IOmicsReader {
	private Condition cond;
	private String dirFiles;

	public HMDBReader(Condition cond, String dirFiles) {
		this.cond = cond;
		this.dirFiles = dirFiles;
	}

	public IOmicsContainer load() throws FileNotFoundException, IOException, FileFormatException {

		OmicsContainer container = new OmicsContainer(cond, OmicsDataType.METABOLITE);

		try {
			File folder = new File(dirFiles);
			File[] listOfFiles = folder.listFiles();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			for (int i = 0; i < listOfFiles.length; i++) {
				// If file name starts with . (invisible files in UNIX
				// plataform) or has a format different than xml is not
				// supported
				if (!(listOfFiles[i].getName().startsWith(".") && !listOfFiles[i].getName().endsWith(".xml"))) {

					Document doc = dBuilder.parse(listOfFiles[i]);
					doc.getDocumentElement().normalize();

					NodeList nNodeTissue = ((Element) doc.getElementsByTagName("tissue_locations").item(0))
							.getElementsByTagName("tissue");

					HashSet<String> tissues = new HashSet<String>();
					for (int j = 0; j < nNodeTissue.getLength(); j++) {
						if (nNodeTissue.item(j).getFirstChild() != null)
							tissues.add(nNodeTissue.item(j).getFirstChild().getNodeValue().toLowerCase());
					}

					if (tissues.size() > 0
							&& (tissues.contains(cond.getPropretieValue(Config.CONDITION_TISSUE)) || tissues
									.contains("all tissues"))) {

						Node nNodeId = doc.getElementsByTagName("accession").item(0);
						Node nNodeName = doc.getElementsByTagName("name").item(0);
						Node nNodeFormula = doc.getElementsByTagName("chemical_formula").item(0).getFirstChild();
						Node nNodeKEGG = doc.getElementsByTagName("kegg_id").item(0).getFirstChild();
						Node nNodeCHEBI = doc.getElementsByTagName("chebi_id").item(0).getFirstChild();

						String id = nNodeId.getFirstChild().getNodeValue();
						String name;

						if (nNodeName == null) {
							name = ((Element) doc.getElementsByTagName("common_name").item(0)).getFirstChild()
									.getNodeValue();
						} else {
							name = nNodeName.getFirstChild().getNodeValue();
						}

						String formula = (nNodeFormula == null) ? "" : nNodeFormula.getNodeValue();
						String keggId = (nNodeKEGG == null) ? "" : nNodeKEGG.getNodeValue();
						String chebiId = (nNodeCHEBI == null) ? "" : nNodeCHEBI.getNodeValue();

						container.setValue(id, 1.0);
						container.setExtraInfoProperty(id, Config.FIELD_KEGG_ID, keggId);
						container.setExtraInfoProperty(id, Config.FIELD_CHEBI_ID, chebiId);
						container.setExtraInfoProperty(id, Config.FIELD_FORMULA, formula);
						container.setExtraInfoProperty(id, Config.FIELD_NAME, name);
					}
				}

			}
		}catch(ParserConfigurationException | SAXException e){
			throw new FileFormatException(e.getMessage());
		}
		return container;
	}
}
