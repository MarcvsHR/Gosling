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

            text = text.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");
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

            text = text.replace("&", "i");
            text = text.replace(" I ", " i ");
            text = text.replace(" Of ", " of ");
            text = text.replace("Feat.","ft");
            text = text.replace("Ft","ft");

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
            returnText.append(array[i].substring(0, 1).toUpperCase()).append(array[i].substring(1)).append(s);
        } } else {
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
        returnText.append(array[length].substring(0, 1).toUpperCase()).append(array[length].substring(1));
        return returnText.toString();
    }

    public static Year parseYear(String year) {
        Year yearOut = null;
        if (year != null) {yearOut = Year.parse(year);}
        return yearOut;
    }
}
