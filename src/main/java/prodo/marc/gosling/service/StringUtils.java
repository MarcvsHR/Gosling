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

}
