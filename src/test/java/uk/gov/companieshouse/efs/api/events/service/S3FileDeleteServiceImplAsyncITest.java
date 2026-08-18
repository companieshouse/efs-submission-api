package uk.gov.companieshouse.efs.api.events.service;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

/**
 * Integration test verifying that {@link S3FileDeleteServiceImpl#deleteFile} returns
 * to the caller immediately, with the S3 delete executing on a background thread.
 *
 * <p>A {@link CountDownLatch} gates the mock S3 client so it cannot complete until
 * the test releases it. This lets the test assert that the caller has already returned
 * before the S3 operation finishes, proving async dispatch.</p>
 */
@SpringJUnitConfig
class S3FileDeleteServiceImplAsyncITest {

    private static final String SUBMISSION_ID = "submission-id";
    private static final String FILE_ID = "file-id";
    private static final long TIMEOUT_MS = 5_000L;

    @Configuration
    @EnableAsync
    static class TestConfig {

        @Bean
        S3Client s3Client() {
            return mock(S3Client.class);
        }

        @Bean
        S3FileDeleteService s3FileDeleteService(final S3Client s3Client) {
            return new S3FileDeleteServiceImpl(s3Client, "test-bucket");
        }

        @Bean(name = "threadPoolTaskExecutor")
        Executor threadPoolTaskExecutor() {
            final var executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(2);
            executor.setQueueCapacity(5);
            executor.setThreadNamePrefix("test-async-s3-");
            executor.initialize();
            return executor;
        }
    }

    @Autowired
    private S3FileDeleteService s3FileDeleteService;

    @Autowired
    private S3Client s3Client;

    @Test
    void shouldReturnBeforeS3DeleteCompletesWhenAsyncIsEnabled() {
        final var s3Release = new CountDownLatch(1);
        final var s3Completed = new AtomicBoolean(false);

        doAnswer(inv -> {
            final var released = s3Release.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertThat("latch should be released before timeout", released, is(true));
            s3Completed.set(true);
            return null;
        }).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // If @Async is active, this returns before the S3 mock runs
        s3FileDeleteService.deleteFile(SUBMISSION_ID, FILE_ID);

        // Caller has returned; S3 is still blocked on the latch
        assertThat("caller should return before S3 delete completes", s3Completed.get(), is(false));

        // Release S3 and poll until the background thread completes
        s3Release.countDown();
        await("S3 delete should complete once released")
            .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .untilTrue(s3Completed);
    }

    @Test
    void shouldReturnBeforeS3ThrowsWhenAsyncIsEnabled() {
        final var s3Release = new CountDownLatch(1);
        final var s3Completed = new AtomicBoolean(false);

        doAnswer(inv -> {
            final var released = s3Release.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);

            assertThat("latch should be released before timeout", released, is(true));
            s3Completed.set(true);
            throw SdkException.builder().message("S3 error").build();
        }).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // If @Async is active, this returns before the S3 mock runs
        s3FileDeleteService.deleteFile(SUBMISSION_ID, FILE_ID);

        // Caller has returned; S3 is still blocked on the latch
        assertThat("caller should return before S3 starts", s3Completed.get(), is(false));

        // Release and poll until the async thread has handled the error
        s3Release.countDown();
        await("async thread should complete error handling without rethrowing")
            .atMost(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .untilTrue(s3Completed);
    }
}
