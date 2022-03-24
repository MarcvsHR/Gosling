package prodo.marc.gosling.service;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.dao.id3Header;

import java.time.Year;
import java.util.Arrays;
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
        return text1.equalsIgnoreCase(text2);
    }

    //TODO: this should be an in-app thing where you pick a filter for renaming stuff
    public static String replaceCroChars(String text, String field) {
        if (text != null) {

            if (field.equals(id3Header.COMPOSER)) {
                text = text.replaceAll("(\\p{Ll})(\\p{Lu})", "$1,$2");
            }
            text = text.replace(" ,", ",");
            text = text.replace(",", ", ");
            text = text.trim().replaceAll(" +", " ");
            text = text.replace("Mc, ", "Mc");
            text = text.replace(" .", ".");
            //text = text.replace("&","i");
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

            text = text.replaceAll("[^\\x00-\\x7F]", "%%");


            text = text.replace("Feat", "Ft");
            text = text.replace("Ft.", "Ft");
            text = text.replace("Ft", "ft");

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
        Year yearOut = null;
        if (year != null) {
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


    public static int calculateSimilarity(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + ( x.charAt(i - 1) == y.charAt(j - 1) ? 0:1 ),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

}
