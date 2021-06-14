package uk.gov.companieshouse.efs.api.submissions.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Company;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class SubmissionValidatorTest {

    private static final String SUB_ID = "0000000000";
    private static final String TEST_AMOUNT = "10";
    private static final PaymentTemplate.Item TEST_ITEM =
        PaymentTemplate.Item.newBuilder().withAmount(TEST_AMOUNT).build();
    public static final String FEE_FORM = "SLPCS01";
    public static final String NON_FEE_FORM = "SH01";

    private SubmissionValidator validator;

    @Mock
    private Submission submission;

    @Mock
    private FormTemplateRepository formRepository;
    @Mock
    private PaymentTemplateRepository paymentRepository;
    @Mock
    private CategoryTemplateService categoryTemplateService;

    @Mock
    private FormTemplate formTemplate;
    @Mock
    private CategoryTemplateApi categoryTemplate;
    @Mock
    private PaymentTemplate paymentTemplate;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        this.validator = new SubmissionValidator(formRepository, paymentRepository, categoryTemplateService, logger);
    }


    @Test
    void testValidateSuccessWithNoPaymentRequired() {
        // given
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, NON_FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findByIdFormType(anyString())).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFee()).thenReturn(null);
        when(formTemplate.getFormCategory()).thenReturn("SH");
        when(formTemplate.getFormType()).thenReturn(NON_FEE_FORM);
        when(submission.getPaymentSessions()).thenReturn(new SessionListApi());
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // then
        assertDoesNotThrow(() -> validator.validate(submission));
    }

    @Test
    void testValidateSuccessWithPayment() {
        // given
        when(submission.getPresenter()).thenReturn(new Presenter("demo@ch.gov.uk"));
        when(submission.getCompany()).thenReturn(new Company("00001234", "ACME"));
        when(submission.getFormDetails())
            .thenReturn(new FormDetails(null, FEE_FORM, Collections.singletonList(FileDetails.builder().build())));
        when(formRepository.findByIdFormType(anyString())).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFee()).thenReturn("TEST_FEE");
        when(paymentRepository.findById("TEST_FEE")).thenReturn(Optional.of(paymentTemplate));
        when(paymentTemplate.getItems()).thenReturn(Collections.singletonList(TEST_ITEM));
        when(submission.getPaymentSessions()).thenReturn(new SessionListApi(
            Collections.singletonList(new SessionApi("woeirsodiflsj",
                "E_lLgj6SI8cWoEXVtGMsuB81DoEcOiWPPgSJTz4OQ0gVo0y6d_NDFP7waRQfdU1z", "paid"))));
        when(submission.getConfirmationReference()).thenReturn("123 456 789");

        // then
        assertDoesNotThrow(() -> validator.validate(submission));
    }

}

