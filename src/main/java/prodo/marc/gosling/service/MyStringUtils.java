package prodo.marc.gosling.service;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.id3Header;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.regex.Pattern;

public class MyStringUtils {

    private static final Logger logger = LogManager.getLogger(MyStringUtils.class);

    /**
     * Check if two strings are equal
     */
    public static boolean compareStrings(String text1, String text2) {
        if (text1 == null) {
            text1 = "";
        }
        if (text2 == null) {
            text2 = "";
        }
        if (text1.equals("<?>") || text2.equals("<?>")) {
            return true;
        }
        return text1.equalsIgnoreCase(text2);
    }

    //TODO: this should be an in-app thing where you pick a filter for renaming stuff
    public static String replaceCroChars(String text, String field) {
        if (text == null) {
            text = "";
        } else {
            if (field.equals(id3Header.COMPOSER)) {
                text = text.replaceAll("(\\p{Ll})(\\p{Lu})", "$1,$2");
            }
            text = text.replace(" ,", ",");
            text = text.replace(",", ", ");
            text = text.trim().replaceAll(" +", " ");
            text = text.replace(" .", ".");
            text = text.toLowerCase();
            text = text.trim();

            text = text.replace("ć", "c");
            text = text.replace("č", "c");
            text = text.replace("đ", "dj");
            text = text.replace("š", "s");
            text = text.replace("ž", "z");

            text = text.replace("ñ", "n");
            text = text.replace("á", "a");
            text = text.replace("é", "e");
            text = text.replace("ó", "o");
            text = text.replace("í", "i");
            text = text.replace("ú", "u");
            text = text.replace("ø", "o");
            text = text.replace("ä", "a");
            text = text.replace("ö", "o");
            text = text.replace("ü", "u");
            text = text.replace("ß", "ss");
            text = text.replace("æ", "ae");


            text = capitalizeAfter(" ", text);
            text = capitalizeAfter(".", text);
            text = capitalizeAfter("/", text);
            text = capitalizeAfter("(", text);
            text = capitalizeAfter("-", text);
            text = text.replace("Mc, ", "Mc");


            for (char c : text.toCharArray())
                if (c > 127)
                    System.out.println("---Weird Char Found--- " + c + ", ASCII: " + (int) c);
            text = text.replaceAll("[^\\x00-\\x7F]", "%%");

            text = text.replace("Feat", "Ft");
            text = text.replace("Ft.", "Ft");
            text = text.replace("Ft", "ft");
        }
        return text;
    }

    private static String capitalizeAfter(String s, String text) {
        if (text.isBlank()) {
            return text;
        }
        String[] array = text.split(Pattern.quote(s));
        StringBuilder returnText = new StringBuilder();
        int length = array.length - 1;
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                returnText.append(capWord(array[i])).append(s);
            }
        } else {
            return capWord(text);
        }
        returnText.append(capWord(array[length]));
        return returnText.toString();
    }

    public static Year parseYear(String year) {
        Year yearOut = Year.now();
        if (year != null && !year.isBlank()) {
            try {
                yearOut = Year.parse(year.trim());
            } catch (Exception exception) {
                logger.error("could not parse year: " + year, exception);
            }
        }
        return yearOut;
    }

    private static String capWord(String s) {
        int minCapSize = 0;
        if (s.length() > minCapSize) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        } else {
            return s;
        }
    }

}
