package prodo.marc.gosling.service.util;

import java.lang.reflect.Field;
import java.time.Year;

public class TruncatedUtil {
    /**
     * Check if all attributes in given Data Object which are marked as truncated are filled correctly
     */
    public static boolean isTruncated(Object input) throws IllegalAccessException {
        for (Field f : input.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Truncated.class)) {
                f.setAccessible(true);
                //TODO: Ovdje staviti dodatno kontrole za provjeru datuma/godina ili čega već treba

                if (f.get(input) == null) {
                    return true;
                }

                if (f.get(input) instanceof String) {
                    if (f.get(input).equals("")) {
                        return true;
                    }
                }

                if (f.get(input) instanceof Year) {
                    if (((Year) f.get(input)).compareTo(Year.of(0)) == 0) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

}
