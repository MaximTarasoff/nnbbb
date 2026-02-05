package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TimingExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(NAMESPACE)
                .put("startTime", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Long startTime = context.getStore(NAMESPACE)
                .get("startTime", Long.class);

        if (startTime == null) {
            return; // для template-контекста
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.printf(
                "Thread %s: Test finished %s, duration %d ms%n",
                Thread.currentThread().getName(),
                context.getDisplayName(),
                duration
        );
    }
}
