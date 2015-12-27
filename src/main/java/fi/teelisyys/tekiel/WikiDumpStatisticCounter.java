package fi.teelisyys.tekiel;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiDumpStatisticCounter {

    public static long articleCount = 0;
    public static long charCount = 0;
    public static long wordCount = 0;
    public static long linkCount = 0;
    public static long titleNotMatchingTextLinkCount = 0;


    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        boolean silent = false;

        Instant startInstant = Instant.now();
        if (!silent) {
            System.out.printf("START AT %s%n", startInstant);
        }

        File f = new File(System.getenv().get("WIKIDUMP_PATH"));
        long articleCount = new WikiDumpSaxWalker().process((title, wikiText) -> {
            WikiDumpStatisticCounter.articleCount++;
            charCount += wikiText.length();
            wordCount += wikiText.split("\\s+").length;

            ArrayList<String> strings = new ArrayList<>();
            Pattern p = Pattern.compile(
                    "\\[\\[([^\\]]+)\\]\\]([\\p{Alpha}]*)",
                    Pattern.UNICODE_CHARACTER_CLASS);
            Pattern p2 = Pattern.compile(
                    "\\[\\[([^\\]|]+)(|[^\\]]*)?\\]\\]([\\p{Alpha}]*)",
                    Pattern.UNICODE_CHARACTER_CLASS);
            Matcher m = p.matcher(wikiText);
            while (m.find()) {
                strings.add(m.group());
            }
            for (String string : strings) {
                linkCount++;

                Matcher m2 = p2.matcher(string);
                if (m2.matches()) {
                    String stem = m2.group(1);
                    String inflectedForm = m2.group(2);
                    inflectedForm = inflectedForm.replaceAll("\\|", "");
                    String suffix = m2.group(3);

                    if (StringUtils.isNotBlank(inflectedForm) || StringUtils.isNotBlank(suffix)) {
                        titleNotMatchingTextLinkCount++;
                    }
                }
            }
        }, f, silent);

        if (!silent) {
            System.out.printf("in file %s there were%n", f.getCanonicalPath());
            System.out.printf("* %d articles and in them%n", articleCount);

            System.out.printf("* %d characters%n", charCount);
            System.out.printf("* %d words%n", wordCount);
            System.out.printf("* %d links with %d mismaches in article title/link text%n",
                    linkCount, titleNotMatchingTextLinkCount);

            Instant end = Instant.now();
            System.out.printf("END AT %s%n", end);
            System.out.printf("PROCESSED %d ARTICLES IN %s%n", articleCount, Duration.between(startInstant, end));
        }
    }
}
