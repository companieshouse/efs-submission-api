package uk.gov.companieshouse.efs.api.email.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.efs.api.email.config.ExternalConfirmationEmailConfig;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.email.model.EmailFileDetails;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailData;
import uk.gov.companieshouse.efs.api.email.model.ExternalConfirmationEmailModel;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;
import uk.gov.companieshouse.efs.api.util.TimestampGenerator;

@Component
public class ExternalConfirmationEmailMapper {

    private ExternalConfirmationEmailConfig config;
    private IdentifierGeneratable idGenerator;
    private TimestampGenerator<LocalDateTime> timestampGenerator;

    public ExternalConfirmationEmailMapper(ExternalConfirmationEmailConfig config, IdentifierGeneratable idGenerator,
                                           TimestampGenerator<LocalDateTime> timestampGenerator) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.timestampGenerator = timestampGenerator;
    }

    public EmailDocument<ExternalConfirmationEmailData> map(ExternalConfirmationEmailModel model) {
        return EmailDocument.<ExternalConfirmationEmailData>builder()
                .withTopic(config.getTopic())
                .withMessageId(idGenerator.generateId())
                .withRecipientEmailAddress(model.getSubmission().getPresenter().getEmail())
                .withEmailTemplateAppId(config.getAppId())
                .withEmailTemplateMessageType(config.getMessageType())
                .withData(fromSubmission(model))
                .withCreatedAt(timestampGenerator.generateTimestamp()
                        .format(DateTimeFormatter.ofPattern(config.getDateFormat()))).build();
    }

    private ExternalConfirmationEmailData fromSubmission(ExternalConfirmationEmailModel model) {
        return ExternalConfirmationEmailData.builder()
                .withTo(model.getSubmission().getPresenter().getEmail())
                .withPresenter(model.getSubmission().getPresenter())
                .withSubject(config.getSubject())
                .withCompany(model.getSubmission().getCompany())
                .withConfirmationReference(model.getSubmission().getConfirmationReference())
                .withFormType(model.getSubmission().getFormDetails().getFormType())
                .withEmailFileDetailsList(createEmailFileDetailsList(model.getSubmission().getFormDetails().getFileDetailsList()))
                .build();
    }

    private List<EmailFileDetails> createEmailFileDetailsList(final List<FileDetails> fileDetailsList) {
        return fileDetailsList.stream().map(this::emailFileDetails).collect(Collectors.toList());
    }

    private EmailFileDetails emailFileDetails(FileDetails fileDetails) {
        return new EmailFileDetails(fileDetails, null);
    }

}
