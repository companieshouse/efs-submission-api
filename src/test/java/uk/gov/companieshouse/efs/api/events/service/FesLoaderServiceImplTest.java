package uk.gov.companieshouse.efs.api.events.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import uk.gov.companieshouse.efs.api.events.service.exception.FesLoaderException;
import uk.gov.companieshouse.efs.api.events.service.fesloader.BatchDao;
import uk.gov.companieshouse.efs.api.events.service.fesloader.EnvelopeDao;
import uk.gov.companieshouse.efs.api.events.service.fesloader.FormDao;
import uk.gov.companieshouse.efs.api.events.service.fesloader.ImageDao;
import uk.gov.companieshouse.efs.api.events.service.model.FesFileModel;
import uk.gov.companieshouse.efs.api.events.service.model.FesLoaderModel;
import uk.gov.companieshouse.efs.api.events.service.model.FormModel;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@ExtendWith(MockitoExtension.class)
class FesLoaderServiceImplTest {

    private static final String FORM_TYPE = "SH04";

    private static final String COMPANY_NUMBER = "58676784";

    private static final String COMPANY_NAME = "ACME";

    private static final String BARCODE = "Y123456";

    private static final long IMAGE_ID = 345L;

    private static final long ENVELOPE_ID = 456L;

    private static final long BATCH_NAME_ID = 432L;

    private static final long BATCH_ID = 321L;

    private FesLoaderServiceImpl fesLoaderService;

    @Mock
    private BatchDao batchDao;
    @Mock
    private EnvelopeDao envelopeDao;
    @Mock
    private ImageDao imageDao;
    @Mock
    private CurrentTimestampGenerator dateGenerator;
    @Mock
    private FormDao formDao;
    @Mock
    private FesLoaderModel model;
    @Mock
    private FormModel formModel;

    @Captor
    private ArgumentCaptor<FormModel> formModelCaptor;

    @BeforeEach
    void setup() {
        this.fesLoaderService = new FesLoaderServiceImpl(batchDao, envelopeDao, dateGenerator, imageDao, formDao);
    }

    @Test
    void testInsertSubmission() throws IOException {

        // given
        LocalDateTime someDate = LocalDateTime.of(2020, Month.MAY, 1, 12, 0);

        FesFileModel myTiff =
                new FesFileModel(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("Hello World.tiff")), 4);

        when(batchDao.getNextBatchId()).thenReturn(BATCH_ID);
        when(dateGenerator.generateTimestamp()).thenReturn(someDate);
        when(batchDao.getBatchNameId(any())).thenReturn(BATCH_NAME_ID);
        when(envelopeDao.getNextEnvelopeId()).thenReturn(ENVELOPE_ID);
        when(model.getTiffFiles()).thenReturn(Collections.singletonList(myTiff));
        when(imageDao.getNextImageId()).thenReturn(IMAGE_ID);
        when(model.getBarcode()).thenReturn(BARCODE);
        when(model.getCompanyName()).thenReturn(COMPANY_NAME);
        when(model.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(model.getFormType()).thenReturn(FORM_TYPE);

        // when
        fesLoaderService.insertSubmission(model);

        // then
        verify(batchDao).getNextBatchId();
        verify(batchDao).getBatchNameId("EFS_200501");
        verify(batchDao).insertBatch(eq(321L), eq("EFS_200501_0432"), eq(someDate));

        verify(envelopeDao).getNextEnvelopeId();
        verify(envelopeDao).insertEnvelope(ENVELOPE_ID, BATCH_ID);

        verify(imageDao).getNextImageId();
        verify(imageDao).insertImage(IMAGE_ID, myTiff.getTiffFile());

        verify(formDao).insertForm(formModelCaptor.capture());
        assertEquals(BARCODE, formModelCaptor.getValue().getBarcode());
        assertEquals(COMPANY_NAME, formModelCaptor.getValue().getCompanyName());
        assertEquals(COMPANY_NUMBER, formModelCaptor.getValue().getCompanyNumber());
        assertEquals(FORM_TYPE, formModelCaptor.getValue().getFormType());
    }

    @Test
    void testInsertSubmissionThrowsFesLoadExceptionIfDAOThrowsADataAccessException() {
        //given
        when(batchDao.getNextBatchId()).thenThrow(new DuplicateKeyException("oops"));

        //when
        Executable actual = () -> fesLoaderService.insertSubmission(model);

        //then
        FesLoaderException exception = assertThrows(FesLoaderException.class, actual);
        assertEquals("Error inserting submission - message [oops]", exception.getMessage());
    }

}
