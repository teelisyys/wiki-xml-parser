# wikidump-processing-tool

A SAX-parser based utility to go through a mediawiki xml-dump and to process the the titles and
wiki-markup-contents of the article therein. There are three processors: one that converts the
wikimarkup to plaintext, one that lists the interwiki-links, and one that that lists the contributors.

## Usage

    mvn clean install
    java -jar target/wikidump-processing-tool-0.0.1-SNAPSHOT-jar-with-dependencies.jar COMMAND path/to/dump.xml path/to/output/prefix

where `COMMAND` is one of `plaintext`, `links`, and `contributors`.


Released under MIT-license, see LICENSE.txt