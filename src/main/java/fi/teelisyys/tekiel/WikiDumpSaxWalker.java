package fi.teelisyys.tekiel;


import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class WikiDumpSaxWalker {

    private int articleCount = 0;

    public interface WikiArticleProcessor {
        void process(String title, String wikiText);
    }

    private void say(String s, boolean silent) {
        if (!silent) {
            System.out.println(s);
        }

    }

    public int process(WikiArticleProcessor processor, File f, boolean silent)
            throws ParserConfigurationException, SAXException, IOException {

        Instant startInstant = Instant.now();

            say("START AT " + startInstant, silent);


        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler dh = new DefaultHandler() {

            boolean titleOpen = false;
            boolean textOpen = false;

            StringBuilder titleBuilder = new StringBuilder();
            StringBuilder contentBuilder = new StringBuilder();


            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {

                if ("title".equalsIgnoreCase(qName)) {
                    titleOpen = true;
                }

                if (qName.equalsIgnoreCase("text")) {
                    textOpen = true;
                }
            }

            public void endElement(String uri, String localName,
                                   String qName) throws SAXException {

                if ("title".equalsIgnoreCase(qName)) {
                    titleOpen = false;
                }

                if ("text".equalsIgnoreCase(qName)) {
                    textOpen = false;
                    articleCount++;

                    String s = contentBuilder.toString();
                    if (StringUtils.isNotBlank(s)) {
                        processor.process(titleBuilder.toString(), s);
                    }

                    titleBuilder = new StringBuilder();
                    contentBuilder = new StringBuilder();

                }
            }

            public void characters(char ch[], int start, int length) throws SAXException {
                if (titleOpen) {
                    titleBuilder.append(ch, start, length);
                    if (articleCount % 10_000 == 0) {
                        say(articleCount + "\t" + 1_042_379 + "\t"
                                + Duration.between(startInstant, Instant.now())
                                + "\t" + new String(ch, start, length), silent);
                    }
                }

                if (textOpen) {
                    contentBuilder.append(ch, start, length);
                }
            }
        };

        saxParser.parse(f, dh);

        int tmp = articleCount;
        articleCount = 0;
        return tmp;
    }
}
