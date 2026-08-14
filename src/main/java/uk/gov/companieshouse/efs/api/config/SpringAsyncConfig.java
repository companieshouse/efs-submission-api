package uk.gov.companieshouse.efs.api.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configures Spring async execution for fire-and-forget operations such as S3 file deletion.
 *
 * <p>Methods annotated with {@code @Async("threadPoolTaskExecutor")} will be dispatched
 * to the thread pool defined here. Uncaught exceptions from async {@code void} methods
 * are routed to {@link #getAsyncUncaughtExceptionHandler()} so they are logged rather
 * than silently dropped by the framework.</p>
 */
@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    @Value("${async.executor.core-pool-size}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity}")
    private int queueCapacity;

    @Value("${async.executor.keep-alive-seconds}")
    private int keepAliveSeconds;

    @Value("${async.executor.allow-core-thread-timeout}")
    private boolean allowCoreThreadTimeOut;

    @Value("${async.executor.wait-for-tasks-to-complete-on-shutdown}")
    private boolean waitForTasksToCompleteOnShutdown;

    @Value("${async.executor.await-termination-seconds}")
    private int awaitTerminationSeconds;

    /**
     * Thread pool used for async task execution.
     *
     * <p>Pool sizes, queue capacity, and scale-down settings are configurable via
     * {@code application.properties}</p>
     * <p>Note: {@code @Bean} will manage bean initialization and invoke {@code executor.initialize()} automatically.</p>
     *
     * @return configured {@link ThreadPoolTaskExecutor}
     */
    @Bean(name = "threadPoolTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        final var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("async-s3-");
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        return executor;
    }

    /**
     * Logs uncaught exceptions thrown by async {@code void} methods.
     *
     * <p>Without this handler, exceptions from {@code @Async void} methods are silently
     * swallowed by the framework. This ensures they appear in application logs.</p>
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) -> {
            final Map<String, Object> logData = new HashMap<>();
            logData.put("method", method.getName());
            logData.put("error", ex.getClass().getName());
            logData.put("message", ex.getMessage());
            LOGGER.error("Uncaught exception in async method", logData);
        };
    }
}
