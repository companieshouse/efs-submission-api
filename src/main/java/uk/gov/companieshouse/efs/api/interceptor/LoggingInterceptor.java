package uk.gov.companieshouse.efs.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

/**
 * This class manages the logging of the start request and end request.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor, RequestLogger {
    private Logger logger;

    /**
     * Sets the logger used to log out request information.
     * @param logger the configured logger
     */
    public LoggingInterceptor(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response,
        final @NonNull Object handler) {
        logStartRequestProcessing(request, logger);
        return true;
    }

    @Override
    public void postHandle(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response, final @NonNull Object handler,
        final ModelAndView modelAndView) {
        logEndRequestProcessing(request, response, logger);
    }

} 