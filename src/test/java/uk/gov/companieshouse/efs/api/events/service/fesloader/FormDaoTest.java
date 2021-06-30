package uk.gov.companieshouse.efs.api.events.service.fesloader;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.companieshouse.efs.api.events.service.model.FormModel;

@ExtendWith(MockitoExtension.class)
class FormDaoTest {

    private static final String BARCODE = "Y1234XYZ";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "ACME";
    private static final String FORM_TYPE = "SH01";
    private static final long IMAGE_ID = 1L;
    private static final long ENVELOPE_ID = 2L;
    private static final long FORM_STATUS = 1L;
    private static final int NUMBER_OF_PAGES = 2;

    private FormDao formDao;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        this.formDao = new FormDao(jdbcTemplate);
    }

    @ParameterizedTest(name = "isSameDay: {0}")
    @ValueSource(booleans = {false, true})
    void testFormDaoInsertsNewFormWhen(final boolean isSameDay) {
        //given
        LocalDateTime now = LocalDateTime.now();

        //when
        this.formDao.insertForm(getFormModel(now, isSameDay));

        //then
        verify(jdbcTemplate).update(anyString(), eq(BARCODE), eq(COMPANY_NUMBER), eq(COMPANY_NAME),
            eq(FORM_TYPE), eq(IMAGE_ID), eq(ENVELOPE_ID), eq(FORM_STATUS), eq(NUMBER_OF_PAGES),
            eq(FORM_TYPE), eq(COMPANY_NAME), eq(COMPANY_NUMBER), eq(BARCODE),
            eq(Timestamp.valueOf(now)), eq(isSameDay ? "Y" : "N"));
    }

    private FormModel getFormModel(LocalDateTime now, final boolean isSameDay) {
        return FormModel.builder()
                .withBarcode(BARCODE)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withFormType(FORM_TYPE)
                .withImageId(IMAGE_ID)
                .withEnvelopeId(ENVELOPE_ID)
                .withFormStatus(FORM_STATUS)
                .withNumberOfPages(NUMBER_OF_PAGES)
                .withBarcodeDate(now)
                .withSameDayService(isSameDay)
                .build();
    }
}
