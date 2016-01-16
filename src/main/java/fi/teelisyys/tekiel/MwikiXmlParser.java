package fi.teelisyys.tekiel;


import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class MwikiXmlParser {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        if (args.length == 3) {
            String command = args[0];
            String input = args[1];
            String output = args[2];

            if ("plaintext".equalsIgnoreCase(command)) {
                ConvertWikiDumpToPlainText.processXmlFile(input, output);
            } else if ("links".equalsIgnoreCase(command)) {
                InterWikiLinkLister.processXmlFile(input, output);
            }


        } else {
            System.out.println("USAGE:\njava -jar target/wikidump-processing-tool-0.0.1-SNAPSHOT-jar-with-dependencies.jar COMMAND INPUT_FILE OUTPUT_OPTION");
        }
    }
}
