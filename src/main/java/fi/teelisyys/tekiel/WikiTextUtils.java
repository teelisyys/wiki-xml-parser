package fi.teelisyys.tekiel;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.compile;

public class WikiTextUtils {
    public static final Function<Matcher, String> REMOVE = m -> "";
    public static final Pattern LINK_PATTERN = compile("\\[\\[(?<stem>[^\\]|]+)(?:\\|)?(?<alt>[^|\\]]*)\\]\\](?<suffix>[\\p{Alpha}]*)", UNICODE_CHARACTER_CLASS);

    public static String gsub(String input, Pattern p, Function<Matcher, String> function) {
        Matcher m = p.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
            sb.append(function.apply(m));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static void iterate(String input, Pattern p, Consumer<Matcher> function) {
        Matcher m = p.matcher(input);
        while (m.find()) {
            function.accept(m);
        }
    }

    static String stripWikiMarkup(String wikiText) {

        // apply regular links to inflected form
        wikiText = gsub(
                wikiText,
                LINK_PATTERN,
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

        wikiText = gsub(wikiText, compile("</?su[pb]>", UNICODE_CHARACTER_CLASS), REMOVE);

        // external links
        wikiText = gsub(wikiText, compile("\\[(?<url>http[^ \\]]+) (?<text>[^\\]]+)\\]", UNICODE_CHARACTER_CLASS), m -> m.group("text"));


        // Tables
        Pattern tablePattern = compile("\\{\\|[^\\}]*\\|\\}", (UNICODE_CHARACTER_CLASS | MULTILINE));
        wikiText = gsub(wikiText, tablePattern, REMOVE);


        Pattern templatePattern = compile("\\{\\{[^\\{\\}]*\\}\\}", (UNICODE_CHARACTER_CLASS | MULTILINE));

        wikiText = gsub(wikiText, templatePattern, REMOVE);
        wikiText = gsub(wikiText, templatePattern, REMOVE); // Nested templates
        wikiText = gsub(wikiText, templatePattern, REMOVE); // Nested templates

        // Tables after links
        wikiText = gsub(wikiText, tablePattern, REMOVE);

        // {.*} is not interesting
        wikiText = gsub(wikiText, compile("\\{[^\\}]*\\}", (UNICODE_CHARACTER_CLASS | MULTILINE)), REMOVE);

        // foo=bar is not interesting
        wikiText = gsub(wikiText, compile("[a-z]+=\"[a-z]+\"", (UNICODE_CHARACTER_CLASS | MULTILINE | Pattern.CASE_INSENSITIVE)), REMOVE);



        // Math markup
        wikiText = gsub(wikiText, compile("<math[^<]*</math>", (UNICODE_CHARACTER_CLASS | MULTILINE)), REMOVE);

        // html-tags
        wikiText = gsub(wikiText, compile("</?[a-z][^>]*>>", (UNICODE_CHARACTER_CLASS | MULTILINE)), REMOVE);


        wikiText = gsub(wikiText, compile("&nbsp;", UNICODE_CHARACTER_CLASS ), m -> " ");
        return wikiText;
    }
}
