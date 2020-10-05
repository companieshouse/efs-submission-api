package uk.gov.companieshouse.efs.api;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import uk.gov.companieshouse.efs.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.efs.api.interceptor.UserAuthenticationInterceptor;

@ExtendWith(MockitoExtension.class)
class EfsApiApplicationTests {
    private EfsApiApplication testApp;

    @Mock
    private UserAuthenticationInterceptor userAuthInterceptor;
    @Mock
    private LoggingInterceptor loggingInterceptor;
    @Mock
    private InterceptorRegistry registry;
    @Mock
    private InterceptorRegistration interceptorRegistration;

    @BeforeEach
    void setUp() {
        testApp = new EfsApiApplication(userAuthInterceptor, loggingInterceptor);
    }

    @Test
    void addInterceptors() {
        doReturn(interceptorRegistration).when(registry).addInterceptor(userAuthInterceptor);
        doReturn(interceptorRegistration).when(registry).addInterceptor(loggingInterceptor);

        testApp.addInterceptors(registry);

        verify(registry).addInterceptor(userAuthInterceptor);
        verify(interceptorRegistration).excludePathPatterns("/healthcheck");
    }

}
