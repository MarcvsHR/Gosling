package prodo.marc.gosling.service;


import java.time.Year;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * Check if two strings are equal
     */
    public static boolean compareStrings(String text1, String text2) {
        if (text1 == null) {text1 = "";}
        if (text2 == null) {text2 = "";}
        return text1.equalsIgnoreCase(text2);
    }

    //TODO: this should be an in-app thing where you pick a filter for renaming stuff
    public static String replaceCroChars(String text) {
        if (text != null) {

            text = text.replaceAll("(\\p{Ll})(\\p{Lu})","$1,$2");
            text = text.replace(",", ", ");
            text = text.trim().replaceAll(" +", " ");
            text = text.replace("Mc ", "Mc");
            text = text.replace(" .", ".");
            text = text.toLowerCase();
            text = text.trim();
            text = capitalizeAfter(" ", text);
            text = capitalizeAfter(".", text);
            text = capitalizeAfter("/", text);
            text = capitalizeAfter("(", text);
            text = capitalizeAfter("-", text);


            text = text.replace("ć", "c");
            text = text.replace("č", "c");
            text = text.replace("š", "s");
            text = text.replace("đ", "dj");
            text = text.replace("ž", "z");

            text = text.replace("Ć", "C");
            text = text.replace("Č", "C");
            text = text.replace("Š", "S");
            text = text.replace("Đ", "Dj");
            text = text.replace("Ž", "Z");

            text = text.replace("Feat","ft");
            text = text.replace("ft.","ft");

            return text;
        } else {
            return "";
        }
    }

    private static String capitalizeAfter(String s, String text) {
        if (text.isBlank()) {
            return text;
        }
        String[] array = text.split(Pattern.quote(s));
        StringBuilder returnText = new StringBuilder();
        int length = array.length-1;
        if (length>0) {
        for (int i = 0; i < length; i++) {
            returnText.append(capWord(array[i])).append(s);
        } } else {
            return capWord(text);
        }
        returnText.append(capWord(array[length]));
        return returnText.toString();
    }

    public static Year parseYear(String year) {
        Year yearOut = null;
        if (year != null) {yearOut = Year.parse(year);}
        return yearOut;
    }

    private static String capWord(String s) {
        if (s.length() > 2) {
            return s.substring(0,1).toUpperCase() + s.substring(1); }
        else { return s; }
    }
}
