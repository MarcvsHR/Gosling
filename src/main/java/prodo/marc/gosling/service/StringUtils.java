package prodo.marc.gosling.service;

public class StringUtils {
    /**
     * Check if two strings are equal
     * */
    public static boolean compareStrings(String text1, String text2) {
        if (text1 == null || text1.isEmpty()) {
            if (text2 == null) {
                return true;
            } else {
                return false;
            }
        }
        return text1.equals(text2);
    }

}
