package api.generators;

//погуглить эту библиотеку
import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RandomModelGenerator {

    private static final Random random = new Random();

    public static  <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);

                Object value;
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                if (rule != null) {
                    value = generateFromRule(rule, field.getType());
                } else {
                    value = generateRandomValue(field);
                }
                field.set(instance, value);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity", e);
        }
    }

    public static <T> T generateAnnotatedFieldsOnly(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                if (rule != null) {
                    Object value = generateFromRule(rule, field.getType());
                    field.set(instance, value);
                }
                // Поля без аннотации остаются со значениями по умолчанию/null
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity with annotated fields only", e);
        }
    }

    // Заполнение конкретного поля переданного объекта
    public static <T> T generateFieldValue(Class<T> clazz, String fieldName) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field field = findField(clazz, fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Field '" + fieldName + "' not found in class " + clazz);
            }

            field.setAccessible(true);
            Object value = generateValueForField(field);
            field.set(instance, value);
            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to populate field '" + fieldName + "'", e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        for (Field field : getAllFields(clazz)) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    private static Object generateValueForField(Field field) {
        GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
        if (rule != null) {
            return generateFromRule(rule, field.getType());
        } else {
            return generateRandomValue(field);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Object generateRandomValue(Field field) {
        Class<?> type = field.getType();
        if (type.equals(String.class)) {
            return UUID.randomUUID().toString().substring(0, 8);
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return random.nextInt(1000);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return random.nextLong();
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return random.nextDouble() * 100;
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return random.nextBoolean();
        } else if (type.equals(List.class)) {
            return generateRandomList(field);
        } else if (type.equals(Date.class)) {
            return new Date(System.currentTimeMillis() - random.nextInt(1000000000));
        } else {
            // Вложенный объект
            return generate(type);
        }
    }

    private static Object generateFromRule(GeneratingRule rule, Class<?> type) {
        // Если задан regex — генерируем по нему
        if (!rule.regex().isEmpty()) {
            return generateFromRegex(rule.regex(), type);
        }
        // Иначе используем min/max
        return generateFromMinMax(rule, type);
    }

    private static Object generateFromRegex(String regex, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regex);
        String result = rgxGen.generate();
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(result);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(result);
        } else {
            return result;
        }
    }

    private static Object generateFromMinMax(GeneratingRule rule, Class<?> type) {
        double min = Double.isNaN(rule.min()) ? 0 : rule.min();
        double max = Double.isNaN(rule.max()) ? 100 : rule.max();

        if (type.equals(Integer.class) || type.equals(int.class)) {
            return (int) (min + random.nextDouble() * (max - min + 1));
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return (long) (min + random.nextDouble() * (max - min + 1));
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            double value = min + random.nextDouble() * (max - min);
            // Округляем до двух знаков после запятой
            return Math.round(value * 100.0) / 100.0;
        } else {
            throw new IllegalArgumentException("Min/max rules only supported for numeric types");
        }
    }

    private static List<String> generateRandomList(Field field) {
        // Пытаемся определить generic-параметр списка
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type actualType = pt.getActualTypeArguments()[0];
            if (actualType == String.class) {
                return List.of(UUID.randomUUID().toString().substring(0, 5),
                        UUID.randomUUID().toString().substring(0, 5));
            }
        }
        return Collections.emptyList();
    }
}