package uk.gov.companieshouse.efs.api.events.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import uk.gov.companieshouse.efs.api.events.service.exception.FesLoaderException;
import uk.gov.companieshouse.efs.api.events.service.fesloader.BatchDao;
import uk.gov.companieshouse.efs.api.events.service.fesloader.EnvelopeDao;
import uk.gov.companieshouse.efs.api.events.service.fesloader.FormDao;
import uk.gov.companieshouse.efs.api.events.service.fesloader.ImageDao;
import uk.gov.companieshouse.efs.api.events.service.model.FesLoaderModel;
import uk.gov.companieshouse.efs.api.events.service.model.FormModel;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class FesLoaderServiceImpl implements FesLoaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");
    private static final String FES_INSERT_TIMER_TASK_NAME = "FES_INSERT_TRANSACTION";
    public static final String DURATION_FORMAT = "m'm':s's':S'ms'";

    private BatchDao batchDao;
    private EnvelopeDao envelopeDao;
    private ImageDao imageDao;
    private CurrentTimestampGenerator timestampGenerator;
    private FormDao formDao;

    /**
     * Constructor.
     *
     * @param batchDao              dependency
     * @param envelopeDao           dependency
     * @param timestampGenerator    dependency
     * @param imageDao              dependency
     * @param formDao               dependency
     */
    @Autowired
    public FesLoaderServiceImpl(BatchDao batchDao, EnvelopeDao envelopeDao, CurrentTimestampGenerator timestampGenerator,
                                ImageDao imageDao, FormDao formDao) {
        this.batchDao = batchDao;
        this.envelopeDao = envelopeDao;
        this.timestampGenerator = timestampGenerator;
        this.imageDao = imageDao;
        this.formDao = formDao;
    }

    @Override
    @Transactional(value = "fesTransactionManager", label = {"FES", "EFS insert"},
            timeoutString = "${fes.datasource.transaction.timeout:10}")
    public void insertSubmission(FesLoaderModel model) {
        StopWatch timer = new StopWatch(getClass().getSimpleName());

        try {
            LOGGER.debug(String.format("Inserting records into FES DB for submission with barcode [%s]", model.getBarcode()));
            timer.start(FES_INSERT_TIMER_TASK_NAME);

            long nextBatchId = insertBatchRecord();
            long envelopeId = insertEnvelopeRecord(nextBatchId);
            // image - batch ID (also used in form update)
            model.getTiffFiles().forEach(file -> {
                long imageId = insertImageRecord(file.getTiffFile());
                insertFormRecord(model, envelopeId, imageId, file.getNumberOfPages());
            });

            timer.stop();
            final String timeToInsertAsString = DurationFormatUtils.formatDuration(
                    timer.getTotalTimeMillis(), DURATION_FORMAT);
            LOGGER.debug(String.format(
                    "Inserted records into FES DB for submission with barcode [%s] in %s",
                    model.getBarcode(),
                    timeToInsertAsString));
        } catch (DataAccessException | TransactionTimedOutException ex) {
            throw new FesLoaderException(String.format("Error inserting submission - message [%s]", ex.getMessage()), ex);
        } finally {
            if (timer.isRunning()) {
                timer.stop();
            }
        }
    }

    private void insertFormRecord(FesLoaderModel model, long envelopeId, long imageId, Integer numberOfPages) {
        //form
        formDao.insertForm(mapToFormModel(model, envelopeId, imageId, numberOfPages));
        LOGGER.debug("inserted form into DB");
    }

    private long insertImageRecord(byte[] file) {
        long imageId = imageDao.getNextImageId();
        LOGGER.debug("image ID " + imageId);
        imageDao.insertImage(imageId, file);
        return imageId;
    }

    private long insertEnvelopeRecord(long nextBatchId) {
        long envelopeId = envelopeDao.getNextEnvelopeId();
        LOGGER.debug("envelope ID " + envelopeId);

        envelopeDao.insertEnvelope(envelopeId, nextBatchId);
        return envelopeId;
    }

    private long insertBatchRecord() {
        long nextBatchId = batchDao.getNextBatchId();
        LOGGER.debug("next batch id " + nextBatchId);

        LocalDateTime currentDate = timestampGenerator.generateTimestamp();
        String formattedCurrentDate = currentDate.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String batchNamePrefix = String.format("EFS_%s", formattedCurrentDate);
        LOGGER.debug("Prefix " + batchNamePrefix);
        Long nextBatchNameId = batchDao.getBatchNameId(batchNamePrefix);
        LOGGER.debug("next batch name id " + nextBatchNameId);
        String batchName = String.format("%s_%04d", batchNamePrefix, nextBatchNameId);
        LOGGER.debug("generated batch name " + batchName);

        batchDao.insertBatch(nextBatchId, batchName, currentDate);
        return nextBatchId;
    }

    private FormModel mapToFormModel(FesLoaderModel model, long envelopeId, long imageId, Integer numberOfPages) {
        return FormModel.builder()
                .withBarcode(model.getBarcode())
                .withCompanyName(model.getCompanyName())
                .withCompanyNumber(model.getCompanyNumber())
                .withFormType(model.getFormType())
                .withImageId(imageId)
                .withEnvelopeId(envelopeId)
                .withFormStatus(1L)
                .withNumberOfPages(numberOfPages)
                .withBarcodeDate(model.getBarcodeDate())
                .withSameDayService(model.isSameDay())
                .build();
    }

}
