package uk.gov.companieshouse.efs.api.events.service.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormModelTest {
    private static final LocalDateTime BARCODE_DATE = LocalDateTime.now();
    private static final Long FORM_STATUS = 1L;
    private static final Long FORM_ID = 3000L;
    private static final Long ENVELOPE_ID = 4000L;
    private static final Long IMAGE_ID = 5000L;

    private FormModel testModel;

    @BeforeEach
    void setUp() {
        testModel = FormModel.builder()
            .withFormId(FORM_ID)
            .withFormType("form")
            .withFormStatus(FORM_STATUS)
            .withBarcode("barcode")
            .withBarcodeDate(BARCODE_DATE)
            .withCompanyName("company-name")
            .withCompanyNumber("company-number")
            .withEnvelopeId(ENVELOPE_ID)
            .withImageId(IMAGE_ID)
            .withNumberOfPages(2)
            .withSameDayService(true)
            .build();
    }

    @Test
    void getEnvelopeId() {
        assertThat(testModel.getEnvelopeId(), is(ENVELOPE_ID));
    }

    @Test
    void getBarcode() {
        assertThat(testModel.getBarcode(), is("barcode"));
    }

    @Test
    void getCompanyName() {
        assertThat(testModel.getCompanyName(), is("company-name"));
    }

    @Test
    void getCompanyNumber() {
        assertThat(testModel.getCompanyNumber(), is("company-number"));
    }

    @Test
    void getFormType() {
        assertThat(testModel.getFormType(), is("form"));
    }

    @Test
    void getFormId() {
        assertThat(testModel.getFormId(), is(FORM_ID));
    }

    @Test
    void getImageId() {
        assertThat(testModel.getImageId(), is(IMAGE_ID));
    }

    @Test
    void getNumberOfPages() {
        assertThat(testModel.getNumberOfPages(), is(2));
    }

    @Test
    void getFormStatus() {
        assertThat(testModel.getFormStatus(), is(FORM_STATUS));
    }

    @Test
    void getBarcodeDate() {
        assertThat(testModel.getBarcodeDate(), is(BARCODE_DATE));
    }

    @Test
    void getSameDayIndicator() {
        assertThat(testModel.getSameDayIndicator(), is("Y"));
    }

    @Test
    void GetSameDayIndicatorWhenFalse() {
        assertThat(FormModel.builder().build().getSameDayIndicator(), is("N"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(FormModel.class)
            .usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }
}