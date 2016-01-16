package fi.teelisyys.tekiel;


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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WikiMetaDataDumpSaxWalker {

    private int revisionCount = 0;
    private Instant lastUpdate = Instant.EPOCH;

    public interface WikiRevisionMetadataProcessor {
        void process(String articleTitle, String revisionTimestamp, String contributorUsername, String contributorIp);
    }

    private void say(String s, boolean silent) {
        if (!silent) {
            System.out.println(s);
        }
    }

    public int process(WikiRevisionMetadataProcessor processor, File f, boolean silent)
            throws ParserConfigurationException, SAXException, IOException {

        Duration MINUTE = Duration.ofMinutes(1);

        Instant startInstant = Instant.now();
        lastUpdate = startInstant;


        String[] interestingTagsArray = {"revision", "contributor", "username", "ip", "title", "timestamp", "page"};
        Set<String> interestingTags = new HashSet<>(interestingTagsArray.length);
        Collections.addAll(interestingTags, interestingTagsArray);

        say("START AT " + startInstant, silent);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler dh = new DefaultHandler() {

            Set<String> openTags = new HashSet<>();

            StringBuilder titleBuilder = new StringBuilder();
            StringBuilder usernameBuilder = new StringBuilder();
            StringBuilder contributorIpBuilder = new StringBuilder();
            StringBuilder timestampBuilder = new StringBuilder();


            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {
                if (interestingTags.contains(qName)) {
                    openTags.add(qName);
                }
            }

            public void endElement(String uri, String localName,
                                   String qName) throws SAXException {
                if ("revision".equalsIgnoreCase(qName)) {

                    revisionCount++;
                    String title = titleBuilder.toString();
                    String username = usernameBuilder.toString();
                    String contributorIp = contributorIpBuilder.toString();
                    String timestamp = timestampBuilder.toString();

                    processor.process(title, timestamp, username, contributorIp);

                    contributorIpBuilder = new StringBuilder();
                    usernameBuilder = new StringBuilder();
                    timestampBuilder = new StringBuilder();


                    Instant now = Instant.now();

                    Duration sinceUpdateDuration = Duration.between(lastUpdate, now);
                    Duration elapsedDuration = Duration.between(startInstant, now);
                    if (sinceUpdateDuration.compareTo(MINUTE) > 0) {
                        say(revisionCount + "\t" + elapsedDuration + "\t" + (revisionCount / elapsedDuration.getSeconds()) + "rev/sec", silent);
                        lastUpdate = now;
                    }

                }

                if ("page".equalsIgnoreCase(qName)) {
                    titleBuilder = new StringBuilder();
                }
                openTags.remove(qName);
            }

            public void characters(char ch[], int start, int length) throws SAXException {
                if (openTags.contains("title")) {
                    titleBuilder.append(ch, start, length);
                }

                if (openTags.contains("timestamp")) {
                    timestampBuilder.append(ch, start, length);
                }

                if (openTags.contains("contributor") && openTags.contains("username")) {
                    usernameBuilder.append(ch, start, length);
                }

                if (openTags.contains("contributor") && openTags.contains("ip")) {
                    contributorIpBuilder.append(ch, start, length);
                }

            }
        };

        saxParser.parse(f, dh);

        int tmp = revisionCount;
        revisionCount = 0;
        return tmp;
    }
}
