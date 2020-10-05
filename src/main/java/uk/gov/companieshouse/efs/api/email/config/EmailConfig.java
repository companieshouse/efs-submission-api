package uk.gov.companieshouse.efs.api.email.config;

import java.util.Objects;

public abstract class EmailConfig {

    private String appId;
    private String subject;
    private String topic;
    private String messageType;
    private String dateFormat;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EmailConfig that = (EmailConfig) o;
        return Objects.equals(getAppId(), that.getAppId())
               && Objects.equals(getSubject(), that.getSubject())
               && Objects.equals(getTopic(), that.getTopic())
               && Objects.equals(getMessageType(), that.getMessageType())
               && Objects.equals(getDateFormat(), that.getDateFormat());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getAppId(), getSubject(), getTopic(), getMessageType(), getDateFormat());
    }
}
