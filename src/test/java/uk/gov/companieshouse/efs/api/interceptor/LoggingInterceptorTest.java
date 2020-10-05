package uk.gov.companieshouse.efs.api.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.LogContext;
import uk.gov.companieshouse.logging.util.LogContextProperties;

@ExtendWith(SpringExtension.class)
public class LoggingInterceptorTest {
    private LoggingInterceptor interceptor;

    @Mock
    private Logger logger;
    @Mock
    HttpSession session;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Object handler;
    @Mock
    private ModelAndView modelAndView;

    /**
     * Tests setup.
     */
    @BeforeEach
    public void setUp() {
        interceptor = new LoggingInterceptor(logger);
        when(session.getAttribute(LogContextProperties.START_TIME_KEY.value())).thenReturn(1L);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void preHandle() {
        // when
        interceptor.preHandle(request, response, handler);
        // then
        verify(logger).infoStartOfRequest(any(LogContext.class));
    }

    @Test
    public void postHandle() {
        // when
        interceptor.postHandle(request, response, handler, modelAndView);
        // then
        verify(logger).infoEndOfRequest(any(LogContext.class), anyInt(), anyLong());
    }
}
