package fi.teelisyys.tekiel;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.compile;

public class WikiTextUtils {
    public static final Function<Matcher, String> REMOVE = m -> "";

    private static String gsub(String input, Pattern p, Function<Matcher, String> function) {
        Matcher m = p.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
            sb.append(function.apply(m));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static String stripWikiMarkup(String wikiText) {

        // apply regular links to inflected form
        wikiText = gsub(
                wikiText,
                compile("\\[\\[(?<stem>[^\\]|]+)(?:\\|)?(?<alt>[^|\\]]*)\\]\\](?<suffix>[\\p{Alpha}]*)", UNICODE_CHARACTER_CLASS),
                (m -> StringUtils.isNotBlank(m.group("alt")) ? m.group("alt") : m.group("stem") + m.group("suffix"))
        );

        // strip image etc. links
        wikiText = gsub(
                wikiText,
                compile("\\[\\[([^\\]]*)\\]\\]", UNICODE_CHARACTER_CLASS),
                REMOVE
        );

        // bold text
        wikiText = gsub(wikiText, compile("'''([^']*)'''", UNICODE_CHARACTER_CLASS), m -> m.group(1));

        // italic text
        wikiText = gsub(wikiText, compile("''([^']*)''", UNICODE_CHARACTER_CLASS), m -> m.group(1));

        // refs
        wikiText = gsub(wikiText, compile("</?ref[^>]*>", UNICODE_CHARACTER_CLASS), REMOVE);

        // links
        wikiText = gsub(wikiText, compile("\\[(?<url>http[^ \\]]+) (?<text>[^\\]]+)\\]", UNICODE_CHARACTER_CLASS), m -> m.group("text"));


        Pattern templatePattern = compile("\\{\\{[^\\{\\}]*\\}\\}", (UNICODE_CHARACTER_CLASS | Pattern.MULTILINE));

        wikiText = gsub(wikiText, templatePattern, REMOVE);
        wikiText = gsub(wikiText, templatePattern, REMOVE); // Nested templates
        wikiText = gsub(wikiText, templatePattern, REMOVE); // Nested templates

        return wikiText;
    }
}
