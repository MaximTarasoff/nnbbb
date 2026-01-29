package api.models.comparison;

import java.lang.reflect.Field;
import java.util.*;

public class ModelComparator {

    public static <A, B> ComparisonResult compareFields(A request, B response, Map<String, String> fieldMappings) {
        List<Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String requestField = entry.getKey();
            String responseField = entry.getValue();

            Object requestValue = getFieldValue(request, requestField);
            Object responseValue = getFieldValue(response, responseField);

            // Если responseValue - это коллекция
            if (responseValue instanceof Collection) {
                Collection<?> collection = (Collection<?>) responseValue;
                boolean found = false;

                // Проверяем, содержится ли requestValue в коллекции
                for (Object item : collection) {
                    if (areValuesEqual(requestValue, item)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    mismatches.add(new Mismatch(
                            requestField + " -> " + responseField,
                            requestValue,
                            "Not found in collection: " + collection
                    ));
                }
            } else if (requestValue instanceof Collection) {
                // Если requestValue - коллекция, а responseValue - нет
                Collection<?> collection = (Collection<?>) requestValue;
                boolean found = false;

                // Проверяем, содержится ли responseValue в коллекции request
                for (Object item : collection) {
                    if (areValuesEqual(responseValue, item)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    mismatches.add(new Mismatch(
                            requestField + " -> " + responseField,
                            requestValue,
                            "Response value not found in request collection: " + responseValue
                    ));
                }
            } else {
                // Обычное сравнение
                if (!areValuesEqual(requestValue, responseValue)) {
                    mismatches.add(new Mismatch(requestField + " -> " + responseField, requestValue, responseValue));
                }
            }
        }

        return new ComparisonResult(mismatches);
    }

    private static List<Object> findValuesInCollection(Object collectionObj, String fieldName) {
        List<Object> values = new ArrayList<>();

        if (collectionObj == null) {
            return values;
        }

        Collection<?> collection;
        if (collectionObj instanceof Collection) {
            collection = (Collection<?>) collectionObj;
        } else if (collectionObj.getClass().isArray()) {
            collection = Arrays.asList((Object[]) collectionObj);
        } else {
            return values;
        }

        for (Object item : collection) {
            try {
                Object value = getFieldValue(item, fieldName);
                if (value != null) {
                    values.add(value);
                }
            } catch (Exception e) {
                // Пропускаем элементы с ошибками
            }
        }

        return values;
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }

        String[] parts = fieldName.split("\\.");
        Object current = obj;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (current == null) {
                return null;
            }

            current = getSimpleFieldValue(current, part);

            if (current instanceof Collection && i < parts.length - 1) {
                Collection<?> collection = (Collection<?>) current;
                List<Object> extractedValues = new ArrayList<>();

                String remainingPath = String.join(".",
                        Arrays.copyOfRange(parts, i + 1, parts.length));

                for (Object item : collection) {
                    Object value = getFieldValue(item, remainingPath);
                    if (value != null) {
                        extractedValues.add(value);
                    }
                }

                return extractedValues;
            }
        }

        return current;
    }

    private static Object getSimpleFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }

        // Обработка коллекций
        if (obj instanceof Collection) {
            return obj;
        }

        if (obj.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Object[]) obj) {
                list.add(item);
            }
            return list;
        }

        // Обработка Map
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(fieldName);
        }

        // Рефлексивный доступ к полям объекта
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        }

        throw new RuntimeException("Field not found: " + fieldName + " in class " + obj.getClass().getName());
    }

    private static boolean areValuesEqual(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }

        // Для числовых значений
        if (value1 instanceof Number && value2 instanceof Number) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            return Math.abs(num1 - num2) < 0.0001;
        }

        // Для строковых значений
        return String.valueOf(value1).equals(String.valueOf(value2));
    }

    public static class ComparisonResult {
        private final List<Mismatch> mismatches;

        public ComparisonResult(List<Mismatch> mismatches) {
            this.mismatches = mismatches;
        }

        public boolean isSuccess() {
            return mismatches.isEmpty();
        }

        public List<Mismatch> getMismatches() {
            return mismatches;
        }

        @Override
        public String toString() {
            if (isSuccess()) {
                return "All fields match.";
            }
            StringBuilder sb = new StringBuilder("Mismatched fields:\n");
            for (Mismatch m : mismatches) {
                sb.append("- ").append(m.fieldName)
                        .append(": expected=").append(m.expected)
                        .append(", actual=").append(m.actual).append("\n");
            }
            return sb.toString();
        }
    }

    public static class Mismatch {
        public final String fieldName;
        public final Object expected;
        public final Object actual;

        public Mismatch(String fieldName, Object expected, Object actual) {
            this.fieldName = fieldName;
            this.expected = expected;
            this.actual = actual;
        }
    }
}