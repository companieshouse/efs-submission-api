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
import uk.gov.companieshouse.efs.api.events.service.fesloader.*;
import uk.gov.companieshouse.efs.api.events.service.model.FesFileModel;
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

    public static final String IN01_FORM = "IN01";

    private BatchDao batchDao;
    private EnvelopeDao envelopeDao;
    private ImageDao imageDao;
    private CurrentTimestampGenerator timestampGenerator;
    private FormDao formDao;
    private AttachmentDao attachmentDao;
    private CoveringLetterDao coveringLetterDao;


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
                                ImageDao imageDao, FormDao formDao, AttachmentDao attachmentDao, CoveringLetterDao coveringLetterDao) {
        this.batchDao = batchDao;
        this.envelopeDao = envelopeDao;
        this.timestampGenerator = timestampGenerator;
        this.imageDao = imageDao;
        this.formDao = formDao;
        this.attachmentDao = attachmentDao;
        this.coveringLetterDao = coveringLetterDao;
    }

    @Override
    @Transactional(value = "fesTransactionManager", label = {"FES", "EFS insert"},
            timeoutString = "${fes.datasource.transaction.timeout:10}")
    public void insertSubmission(FesLoaderModel model) {
        long formImageId = 0;
        long memArtsImageId = 0 ;
        long suppNameAuthImageId = 0;
        long coveringLetterImageId = 0;
        long coveringLetterId = 0;
        int formPageCount = 0;

        StopWatch timer = new StopWatch(getClass().getSimpleName());

        try {
            LOGGER.debug(String.format("Inserting records into FES DB for submission with barcode [%s]", model.getBarcode()));
            timer.start(FES_INSERT_TIMER_TASK_NAME);

            long nextBatchId = insertBatchRecord();
            long envelopeId = insertEnvelopeRecord(nextBatchId);

            // image - batch ID (also used in form update)
            for(FesFileModel file : model.getTiffFiles()) {
                //TODO consider whether this should be a switch statement
                //if memandarts exist store the image
               if(file.getAttachmentType()!=null && file.getAttachmentType().equalsIgnoreCase("MEMARTS")) {
                    memArtsImageId = insertImageRecord(file.getTiffFile());
                } else if(file.getAttachmentType() !=null && file.getAttachmentType().equalsIgnoreCase("SUPPNAMEAUTH")) {
                   //if supplementary evidence exists store the image
                    suppNameAuthImageId = insertImageRecord(file.getTiffFile());
                } else if(file.getAttachmentType()!= null && file.getAttachmentType().equalsIgnoreCase("COVLETTER")) {
                   //if a covering letter has been included then store image and update database
                    coveringLetterImageId = insertImageRecord(file.getTiffFile());
                    coveringLetterId = insertCoveringLetter(envelopeId, coveringLetterImageId, file.getNumberOfPages());
                } else {
                   //otherwise this is the form itself which needs to be stored.
                    formImageId = insertImageRecord(file.getTiffFile());
                    formPageCount = file.getNumberOfPages();
                }
            }
            if(formImageId > 0) {
                long formId = insertFormRecord(model, envelopeId, formImageId, formPageCount, coveringLetterId);
                if(memArtsImageId > 0){
                    insertAttachment(formId,1L,memArtsImageId);
                }
                if(suppNameAuthImageId > 0) {
                    insertAttachment(formId,2L, suppNameAuthImageId);
                }
            } else {
                //throw an exception as something went really wrong
            }

                // image - batch ID (also used in form update)c
//                model.getTiffFiles().forEach(file -> {
//                    long imageId = insertImageRecord(file.getTiffFile());
//                    insertFormRecord(model, envelopeId, imageId, file.getNumberOfPages());
//                });

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

    private long insertFormRecord(FesLoaderModel model, long envelopeId, long imageId, Integer numberOfPages, long coveringLetterId) {
        //form
        long formId = formDao.getNextFormId();
        LOGGER.debug("Form ID " + formId);
        formDao.insertForm(formId,mapToFormModel(model, envelopeId, imageId, numberOfPages, coveringLetterId));
        LOGGER.debug("inserted form into DB");
        return formId;
    }

    private void insertAttachment(long formId, long attachmentTypeId, long imageId) {
        long attachmentId = attachmentDao.getNextAttachmentId();
        LOGGER.debug("attachment ID " + attachmentId);
        attachmentDao.insertAttachment(attachmentId, formId, attachmentTypeId, imageId);
        LOGGER.debug("inserted attachment into DB");
    }

    private long insertCoveringLetter(long envelopeId, long imageId, int pageCount) {
        long coveringLetterId = coveringLetterDao.getNextCoveringLetterId();
        LOGGER.debug("coveringLetterID " + coveringLetterId);
        coveringLetterDao.insertCoveringLetter(coveringLetterId, envelopeId, imageId, pageCount);
        LOGGER.debug("inserted covering letter into DB");
        return coveringLetterId;
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

    private FormModel mapToFormModel(FesLoaderModel model, long envelopeId, long imageId, Integer numberOfPages, long coveringLetterId) {
        return FormModel.builder()
                .withBarcode(model.getBarcode())
                .withCompanyName(model.getCompanyName())
                .withCompanyNumber(model.getCompanyNumber())
                .withFormType(model.getFormType())
                .withCoveringLetterId(coveringLetterId)
                .withImageId(imageId)
                .withEnvelopeId(envelopeId)
                .withFormStatus(1L)
                .withNumberOfPages(numberOfPages)
                .withBarcodeDate(model.getBarcodeDate())
                .withSameDayService(model.isSameDay())
                .build();
    }

}
