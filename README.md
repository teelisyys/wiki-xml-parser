# wikidump-processing-tool

A small SAX-parser based utility to go through a mediawiki xml-dump and to process the the titles and
wiki-markup-contents of the article therein. The example processor counts some statistics about the text.

## Usage

    mvn clean install
    WIKIDUMP_PATH='path/to/dump.xml' java -jar target/wikidump-processing-tool-0.0.1-SNAPSHOT-jar-with-dependencies.jar

