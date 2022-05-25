package prodo.marc.gosling.service.util;

import java.lang.reflect.Field;
import java.time.Year;

public class TruncatedUtil {
    /**
     * Check if all attributes in given Data Object which are marked as truncated are filled correctly
     */
    public static boolean isTruncated(Object input) throws IllegalAccessException {
        for (Field field : input.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Truncated.class)) {
                field.setAccessible(true);

                if (field.get(input) == null) {
                    return true;
                }

                if (field.get(input) instanceof String) {
                    if (field.get(input).toString().isEmpty()) {
                        return true;
                    }
                }

                if (field.get(input) instanceof Year) {
                    if (((Year) field.get(input)).compareTo(Year.of(0)) == 0) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

}
