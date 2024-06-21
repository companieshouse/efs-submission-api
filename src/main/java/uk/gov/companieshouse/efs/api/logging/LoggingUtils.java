package uk.gov.companieshouse.efs.api.logging;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class LoggingUtils {

    private LoggingUtils() {}

    public static final String APPLICATION_NAME_SPACE = "efs-submission-api";


    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public static Logger getLogger() {
        return LOGGER;
    }

}