package api.dao.comparison;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class DaoComparator {

    private final DaoComparisonConfigLoader configLoader;
    private static final double DOUBLE_COMPARISON_EPSILON = 0.01;

    public DaoComparator() {
        this.configLoader = new DaoComparisonConfigLoader("dao-comparison.properties");
    }

    public void compare(Object apiResponse, Object dao) {
        DaoComparisonConfigLoader.DaoComparisonRule rule = configLoader.getRuleFor(apiResponse.getClass());

        if (rule == null) {
            throw new RuntimeException("No comparison rule found for " + apiResponse.getClass().getSimpleName());
        }

        Map<String, String> fieldMappings = rule.getFieldMappings();

        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            String apiFieldPath = mapping.getKey();
            String daoFieldName = mapping.getValue();

            Object apiValue = getNestedFieldValue(apiResponse, apiFieldPath);
            Object daoValue = getFieldValue(dao, daoFieldName);

            if (!areEqual(apiValue, daoValue)) {
                throw new AssertionError(String.format(
                        "Field mismatch for %s: API=%s, DAO=%s",
                        apiFieldPath, apiValue, daoValue));
            }
        }
    }

    private boolean areEqual(Object a, Object b) {
        // Handle floating-point numbers with tolerance
        if (a instanceof Number && b instanceof Number) {
            double aDouble = ((Number) a).doubleValue();
            double bDouble = ((Number) b).doubleValue();
            return Math.abs(aDouble - bDouble) <= DOUBLE_COMPARISON_EPSILON;
        }

        return Objects.equals(a, b);
    }

    private Object getNestedFieldValue(Object obj, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Object current = obj;

        for (String part : parts) {
            try {
                // Handle collections (like List)
                if (current instanceof List) {
                    List<?> list = (List<?>) current;
                    if (list.isEmpty()) {
                        return null;
                    }
                    current = list.getFirst();
                }

                Field field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);

                if (current == null) {
                    return null;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to get nested field value: " + fieldPath, e);
            }
        }
        return current;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    private static class Objects {
        public static boolean equals(Object a, Object b) {
            return (a == b) || (a != null && a.equals(b));
        }
    }
}