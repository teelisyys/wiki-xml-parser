package fi.teelisyys.tekiel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

public class WikiMetaDataDumpContributorLister {

    static String currentArticle = "";
    static long articleCount = 0;
    static Set<String> currentArticleAuthors = new HashSet<>();
    static Set<String> currentArticleIps = new HashSet<>();

    static Set<String> allContributors = new HashSet<>();

    public static void main(String[] args) {
        if (args.length == 2) {
            processXmlFile(args[0], args[1]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void processXmlFile(String inputFilePath, String outputFilePrefix) {
        try {
            File inputFile = new File(inputFilePath);
            File outputFileByArticle = new File(outputFilePrefix + "-by-article-" + Instant.now().toString().replaceAll(":", ""));
            File outputFileAllContributors = new File(outputFilePrefix + "-all-contributors-" + Instant.now().toString().replaceAll(":", ""));

            Instant startInstant = Instant.now();
            long revisionCount = new WikiMetaDataDumpSaxWalker().process((articleTitle, revisionTimestamp, contributorUsername, contributorIp) -> {

                if (isNotBlank(contributorIp)) {
                    currentArticleIps.add(contributorIp);

                }
                if (isNotBlank(contributorUsername)) {
                    currentArticleAuthors.add(contributorUsername);
                }

                if (!currentArticle.equalsIgnoreCase(articleTitle)) {
                    try {

                        String s = (currentArticleIps.size() == 0
                                ? "" :
                                format(", and %d anonymous contributors", currentArticleIps.size()));
                        FileUtils.write(outputFileByArticle, format("%s: %s%s\n", currentArticle, StringUtils.join(currentArticleAuthors, ", "), s), "utf-8", true);
                        WikiMetaDataDumpContributorLister.allContributors.addAll(currentArticleAuthors);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    articleCount++;
                    currentArticle = articleTitle;
                    currentArticleAuthors = new HashSet<>();
                    currentArticleIps = new HashSet<>();
                }

            }, inputFile, false);

            FileUtils.write(outputFileAllContributors, join(WikiMetaDataDumpContributorLister.allContributors, "\n"), "utf-8", true);

            System.out.printf("There were %d different contributors%n", WikiMetaDataDumpContributorLister.allContributors.size());
            Instant end = Instant.now();
            System.out.printf("END AT %s%n", end);
            System.out.printf("PROCESSED %d REVISION IN %s%n", revisionCount, Duration.between(startInstant, end));


        } catch (ParserConfigurationException | SAXException | IOException e) {
           throw new RuntimeException(e);
        }
    }
}
