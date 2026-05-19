package uk.gov.companieshouse.efs.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.efs.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.efs.api.interceptor.UserAuthenticationInterceptor;

@EnableMongoRepositories
@SpringBootApplication
public class EfsApiApplication implements WebMvcConfigurer {
    private final UserAuthenticationInterceptor userAuthenticationInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    /**
     * Constructor for EfsApiApplication.
     *
     * @param userAuthenticationInterceptor responsible for validating a user is authenticated
     * @param loggingInterceptor responsible for logging the start and end of the requests
     */
    public EfsApiApplication(final UserAuthenticationInterceptor userAuthenticationInterceptor,
        final LoggingInterceptor loggingInterceptor) {
        this.userAuthenticationInterceptor = userAuthenticationInterceptor;
        this.loggingInterceptor = loggingInterceptor;
    }

    /**
     * Required spring boot application main method.
     *
     * @param args array of String arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EfsApiApplication.class, args);
    }

    /**
     * Registers the interceptors used by the application.
     *
     * @param registry the registry of interceptors
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        // all public interactions with have to be user authenticated
        registry.addInterceptor(userAuthenticationInterceptor);
        registry.addInterceptor(loggingInterceptor).excludePathPatterns("/healthcheck");
    }
}
