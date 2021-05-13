package uk.gov.companieshouse.efs.api.email;

import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionBusinessEmailModel;
import uk.gov.companieshouse.efs.api.email.model.DelayedSubmissionSupportEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalAcceptEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalNotificationEmailModel;
import uk.gov.companieshouse.efs.api.email.model.ExternalRejectEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalAvFailedEmailModel;
import uk.gov.companieshouse.efs.api.email.model.InternalFailedConversionModel;
import uk.gov.companieshouse.efs.api.email.model.InternalSubmissionEmailModel;
import uk.gov.companieshouse.efs.api.email.model.PaymentReportEmailModel;

public interface EmailService {
    void sendExternalConfirmation(ExternalNotificationEmailModel emailModel);

    void sendExternalPaymentFailedNotification(ExternalNotificationEmailModel emailModel);

    void sendExternalAccept(ExternalAcceptEmailModel emailModel);

    void sendExternalReject(ExternalRejectEmailModel emailModel);

    void sendInternalFailedAV(InternalAvFailedEmailModel emailModel);

    void sendInternalSubmission(InternalSubmissionEmailModel emailModel);

    void sendInternalFailedConversion(InternalFailedConversionModel emailModel);

    void sendDelayedSubmissionSupportEmail(DelayedSubmissionSupportEmailModel delayedSubmissionSupportEmailModel);

    void sendDelayedSubmissionBusinessEmail(DelayedSubmissionBusinessEmailModel emailModel);

    void sendPaymentReportEmail(PaymentReportEmailModel emailModel);
}
