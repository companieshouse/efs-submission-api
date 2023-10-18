package uk.gov.companieshouse.efs.api.submissions.validator;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.submissions.validator.exception.SubmissionValidationException;
import uk.gov.companieshouse.logging.Logger;

@Component
public class SubmissionValidator extends ValidatorImpl<Submission> implements Validator<Submission> {

    private FormTemplateRepository formRepository;
    private PaymentTemplateRepository paymentRepository;
    private CategoryTemplateService categoryTemplateService;
    private final Clock clock;
    private final Logger logger;


    @Autowired
    public SubmissionValidator(FormTemplateRepository formRepository, PaymentTemplateRepository paymentRepository,
        CategoryTemplateService categoryTemplateService, final Clock clock, final Logger logger) {
        this.formRepository = formRepository;
        this.categoryTemplateService = categoryTemplateService;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
        this.logger = logger;
    }

    @Override
    public void validate(Submission input) throws SubmissionValidationException {

        logger.infoContext(input.getId(), "About to validate submission", null);

        final Validator<Submission> val1 = new FormDetailsValidator();

        val1.setNext(new FormTemplateValidator(formRepository))
            .setNext(new ConfirmAuthorisedValidator(formRepository, categoryTemplateService))
            .setNext(new FileDetailsValidator())
            .setNext(new PaymentSessionsValidator(formRepository, paymentRepository, clock))
            .setNext(new FesAttachmentValidator(formRepository))
            .setNext(new PresenterValidator())
            .setNext(new CompanyDetailsValidator())
            .setNext(new ConfirmationReferenceValidator());
        val1.validate(input);
        logger.infoContext(input.getId(), "Successfully validated submission", null);

    }

}
