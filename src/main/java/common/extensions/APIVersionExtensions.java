package common.extensions;

import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Optional;

public class APIVersionExtensions implements ExecutionCondition {
    
    private static final String currentVersion = loadCurrentVersion();
    
    private static String loadCurrentVersion() {
        Properties config = new Properties();
        
        try {
            InputStream input = APIVersionExtensions.class
                .getClassLoader()
                .getResourceAsStream("config.properties");
            
            if (input != null) {
                config.load(input);
                String version = config.getProperty("api.version");
                input.close();
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (IOException e) {
            // ignore
        }
        
        // Проверяем переменные окружения и системные свойства
        String envVersion = System.getenv("API_VERSION");
        if (envVersion != null && !envVersion.isEmpty()) {
            return envVersion;
        }
        
        String systemVersion = System.getProperty("api.version");
        if (systemVersion != null && !systemVersion.isEmpty()) {
            return systemVersion;
        }
        
        return "with_database_with_fix"; // default
    }
    
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Проверяем аннотацию на уровне метода
        Optional<APIVersion> methodAnnotation = context.getTestMethod()
            .flatMap(method -> Optional.ofNullable(method.getAnnotation(APIVersion.class)));
        
        // Проверяем аннотацию на уровне класса (если нет на методе)
        Optional<APIVersion> classAnnotation = context.getTestClass()
            .flatMap(clazz -> Optional.ofNullable(clazz.getAnnotation(APIVersion.class)));
        
        String requiredVersion = null;
        
        // Приоритет: аннотация метода > аннотация класса
        if (methodAnnotation.isPresent()) {
            requiredVersion = methodAnnotation.get().value();
        } else if (classAnnotation.isPresent()) {
            requiredVersion = classAnnotation.get().value();
        }
        
        // Если аннотации нет - тест выполняется всегда
        if (requiredVersion == null) {
            return ConditionEvaluationResult.enabled("Тест без аннотации @APIVersion выполняется всегда");
        }
        
        // Проверяем совпадение версий
        if (requiredVersion.equals(currentVersion)) {
            return ConditionEvaluationResult.enabled(
                String.format("Тест выполняется для версии: %s", currentVersion)
            );
        } else {
            return ConditionEvaluationResult.disabled(
                String.format("Тест пропущен. Требуется версия: %s, текущая версия: %s",
                    requiredVersion, currentVersion)
            );
        }
    }
}