package fi.teelisyys.tekiel;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;

public class InterWikiLinkLister {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        if (args.length == 2) {
            processXmlFile(args[0], args[1]);
        } else {
            throw new IllegalArgumentException();
        }
        processXmlFile(args[0], args[1]);
    }

    public static void processXmlFile(String inputFilePath, String outputPrefix) throws ParserConfigurationException, SAXException, IOException {
        Instant startInstant = Instant.now();

        File outputFile = new File(outputPrefix + "-" + Instant.now().toString().replaceAll(":", ""));
        File inputFile = new File(inputFilePath);


        long articleCount = new WikiDumpSaxWalker().process((title, wikiText) -> WikiTextUtils.iterate(
                wikiText,
                WikiTextUtils.LINK_PATTERN,
                (m) -> {
                    try {
                        FileUtils.write(
                                outputFile,
                                MessageFormat.format("{0};{1};{2}",
                                        m.group("stem"),
                                        m.group("alt"),
                                        m.group("suffix")
                                ).replaceAll("\\n", " ") + "\n", true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }), inputFile, false);


        Instant end = Instant.now();
        System.out.printf("END AT %s%n", end);
        System.out.printf("PROCESSED %d ARTICLES IN %s%n", articleCount, Duration.between(startInstant, end));
    }
}
