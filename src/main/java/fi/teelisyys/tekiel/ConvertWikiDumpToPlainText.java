package fi.teelisyys.tekiel;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class ConvertWikiDumpToPlainText {

    private static File file = null;
    private static long articleCount = 0;


    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        if (args.length == 2) {
            processXmlFile(args[0], args[1]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void processXmlFile(String inputFile, String outputFilePrefix) {
        try {
            Instant startInstant = Instant.now();
            System.out.println("START AT " + startInstant);
            File f = new File(inputFile);

            new WikiDumpSaxWalker().process((title, wikiText) -> {
                articleCount++;
                try {
                    if (file == null || FileUtils.sizeOf(file) > 10_000_000) {
                        touch(outputFilePrefix);
                    }

                    FileUtils.write(file, title, "utf-8", true);
                    FileUtils.write(file, WikiTextUtils.stripWikiMarkup(wikiText), "utf-8", true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, f, false);

            Instant end = Instant.now();
            System.out.println("END AT " + end);
            System.out.println("PROCESSED " + articleCount + " FILES IN " + Duration.between(startInstant, end));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static void touch(String outputFilePrefix) throws IOException {
        file = new File(outputFilePrefix + "-" + articleCount);
        if (!file.exists()) {
            FileUtils.touch(file);
        }
    }
}

