package prodo.marc.gosling.service;

public class StringUtils {
    /**
     * Check if two strings are equal
     * */
    public static boolean compareStrings(String text1, String text2) {
        if (text1 == null || text1.isEmpty()) {
            return text2 == null;
        }
        return text1.equals(text2);
    }

    public static String replaceCroChars(String text) {
        if (text != null) {
        text = text.replace("ć","c");
        text = text.replace("č","c");
        text = text.replace("š","s");
        text = text.replace("đ","dj");
        text = text.replace("ž","z");

        text = text.replace("Ć","C");
        text = text.replace("Č","C");
        text = text.replace("Š","S");
        text = text.replace("Đ","Dj");
        text = text.replace("Ž","Z");

        return text;}
        else {
            return "";
        }
    }

}
