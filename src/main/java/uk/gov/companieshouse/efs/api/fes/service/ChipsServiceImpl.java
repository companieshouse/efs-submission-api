package uk.gov.companieshouse.efs.api.fes.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.efs.api.fes.repository.ChipsDao;
import uk.gov.companieshouse.efs.api.fes.service.exception.ChipsServiceException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class ChipsServiceImpl implements ChipsService {

    private ChipsDao chipsDao;
    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    public ChipsServiceImpl(ChipsDao chipsDao) {
        this.chipsDao = chipsDao;
    }

    @Override
    public List<String> getRejectReasons(String barcode) {
        try {
            LOGGER.debug("Attempting to read reject reasons for barcode [%s]".formatted(barcode));
            final var rejectReasons = chipsDao.readRejectReasonsForBarcode(barcode);
            if (rejectReasons.isEmpty()) {
                LOGGER.error("No reject reasons found for barcode [%s]".formatted(barcode));
            } else {
                rejectReasons.forEach(LOGGER::debug);
                LOGGER.debug("Successfully read reject reasons for barcode [%s]".formatted(barcode));
            }
            return rejectReasons;
        } catch (DataAccessException ex) {
            throw new ChipsServiceException("Unable to read reject reasons from CHIPS");
        }
    }
}
